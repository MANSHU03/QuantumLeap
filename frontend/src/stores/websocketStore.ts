import { create } from 'zustand';
import { useEventsStore } from './eventsStore';

export type ConnectionStatus = 'disconnected' | 'connecting' | 'connected' | 'reconnecting';

interface WebSocketState {
    status: ConnectionStatus;
    connection: WebSocket | null;
    reconnectAttempts: number;
    boardId: string | null;
    token: string | null;
    socketError: string | null;
    onCursorUpdate?: (cursorData: any) => void;
    onCursorsInit?: (cursorsData: any) => void;
}

interface WebSocketActions {
    connect: (boardId: string, token: string) => void;
    disconnect: () => void;
    sendEvent: (event: any) => void;
    setStatus: (status: ConnectionStatus) => void;
    clearSocketError: () => void;
    setCursorCallbacks: (onCursorUpdate?: (cursorData: any) => void, onCursorsInit?: (cursorsData: any) => void) => void;
}

const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 1000;

export const useWebSocketStore = create<WebSocketState & WebSocketActions>((set, get) => ({
    status: 'disconnected',
    connection: null,
    reconnectAttempts: 0,
    boardId: null,
    token: null,
    socketError: null,

    connect: (boardId: string, token: string) => {
        const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080';
        // Connect to the WebSocket endpoint with the full path including context
        // Backend context path: /api/v1
        // Backend handler registered at: /api/v1/ws/whiteboard/{boardId}
        const wsUrl = `${wsBaseUrl}/api/v1/ws/whiteboard/${boardId}?token=${token}`;

        set({ status: 'connecting', boardId, token, socketError: null });
        console.log('[WebSocket] Connecting to', wsUrl);

        const ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            set({ status: 'connected', connection: ws, reconnectAttempts: 0 });
            console.log('[WebSocket] Connected');
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                const { addEvent } = useEventsStore.getState();
                
                console.log('[WebSocket] Received message:', data);

                if (data.type === 'EVENT_REPLAY_CHUNK' && data.data?.events) {
                    console.log('[WebSocket] Processing event replay chunk with', data.data.events.length, 'events');
                    data.data.events.forEach((event: any) => {
                        console.log('[WebSocket] Adding event from replay:', event.type, event.id);
                        addEvent(event);
                    });
                } else if (data.type === 'EVENT_APPEND' && data.data?.event) {
                    console.log('[WebSocket] Processing new event:', data.data.event.type, data.data.event.id);
                    addEvent(data.data.event);
                } else if (data.type === 'CONNECTION_ESTABLISHED') {
                    console.log('[WebSocket] Connection established:', data.data?.message);
                } else if (data.type === 'CURSOR_UPDATE' && data.data?.cursor) {
                    console.log('[WebSocket] Processing cursor update:', data.data.cursor);
                    const { onCursorUpdate } = get();
                    if (onCursorUpdate) {
                        onCursorUpdate(data.data.cursor);
                    }
                } else if (data.type === 'CURSORS_INIT' && data.data?.cursors) {
                    console.log('[WebSocket] Processing cursors init:', data.data.cursors);
                    const { onCursorsInit } = get();
                    if (onCursorsInit) {
                        onCursorsInit(data.data.cursors);
                    }
                } else if (data.type === 'ERROR') {
                    console.error('[WebSocket] Backend error:', data.data?.message);
                    set({ socketError: data.data?.message, status: 'disconnected', connection: null });
                    ws.close();
                } else {
                    console.log('[WebSocket] Unknown message type:', data.type);
                }
            } catch (error) {
                console.error('[WebSocket] Failed to parse message:', error);
            }
        };

        ws.onclose = (event) => {
            const { reconnectAttempts, socketError } = get();
            console.warn('[WebSocket] Closed', event);
            if (!socketError && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                set({ status: 'reconnecting', connection: null, reconnectAttempts: reconnectAttempts + 1 });
                setTimeout(() => {
                    const { boardId, token } = get();
                    if (boardId && token) {
                        get().connect(boardId, token);
                    }
                }, RECONNECT_DELAY * Math.pow(2, reconnectAttempts));
            } else {
                set({ status: 'disconnected', connection: null, reconnectAttempts: 0 });
            }
        };

        ws.onerror = (error) => {
            console.error('[WebSocket] Error:', error);
            set({ status: 'disconnected', connection: null, socketError: 'WebSocket connection error' });
        };
    },

    disconnect: () => {
        const { connection } = get();
        if (connection) {
            connection.close();
        }
        set({ status: 'disconnected', connection: null, boardId: null, token: null, reconnectAttempts: 0, socketError: null });
    },

    sendEvent: (event: any) => {
        const { connection, status } = get();
        console.log('[WebSocket] Attempting to send event:', event.type, event.id, 'Status:', status, 'Connection:', !!connection);
        
        if (connection && status === 'connected') {
            try {
                const eventJson = JSON.stringify(event);
                console.log('[WebSocket] Sending event:', eventJson);
                connection.send(eventJson);
                console.log('[WebSocket] Event sent successfully');
            } catch (error) {
                console.error('[WebSocket] Error sending event:', error);
            }
        } else {
            console.warn('[WebSocket] Cannot send event - connection not ready. Status:', status, 'Connection:', !!connection);
        }
    },

    setStatus: (status: ConnectionStatus) => {
        set({ status });
    },

    clearSocketError: () => {
        set({ socketError: null });
    },

    setCursorCallbacks: (onCursorUpdate?: (cursorData: any) => void, onCursorsInit?: (cursorsData: any) => void) => {
        set({ onCursorUpdate, onCursorsInit });
    },
}));
