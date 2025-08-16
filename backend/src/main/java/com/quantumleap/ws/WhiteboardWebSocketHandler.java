package com.quantumleap.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantumleap.config.JwtTokenProvider;
import com.quantumleap.dto.ws.EventEnvelope;
import com.quantumleap.dto.ws.CursorUpdateEvent;
import com.quantumleap.entity.CursorPosition;
import com.quantumleap.service.EventService;
import com.quantumleap.service.WhiteboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.util.HashMap;

@Component
public class WhiteboardWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WhiteboardWebSocketHandler.class);
    
    @Value("${quantumleap.websocket.event-replay-chunk-size:200}")
    private int eventReplayChunkSize;
    
    private final JwtTokenProvider jwtTokenProvider;
    private final WhiteboardService whiteboardService;
    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    
    // In-memory session management (for local development)
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, UUID> sessionBoardIds = new ConcurrentHashMap<>();
    private final Map<String, UUID> sessionUserIds = new ConcurrentHashMap<>();
    
    // Board sessions mapping for broadcasting
    private final Map<UUID, Map<String, WebSocketSession>> boardSessions = new ConcurrentHashMap<>();
    
    // Cursor tracking for each board
    private final Map<UUID, Map<UUID, CursorPosition>> boardCursors = new ConcurrentHashMap<>();
    
    // User colors for cursor identification
    private final Map<UUID, String> userColors = new ConcurrentHashMap<>();
    
    // Predefined colors for cursors
    private static final String[] CURSOR_COLORS = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"
    };

    public WhiteboardWebSocketHandler(JwtTokenProvider jwtTokenProvider,
                                    WhiteboardService whiteboardService,
                                    EventService eventService,
                                    ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.whiteboardService = whiteboardService;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
        
        log.info("[WS] WhiteboardWebSocketHandler initialized successfully");
        log.info("[WS] JWT Token Provider: {}", jwtTokenProvider != null ? "OK" : "NULL");
        log.info("[WS] Whiteboard Service: {}", whiteboardService != null ? "OK" : "NULL");
        log.info("[WS] Event Service: {}", eventService != null ? "OK" : "NULL");
        log.info("[WS] Object Mapper: {}", objectMapper != null ? "OK" : "NULL");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            log.info("[WS] New connection attempt: {}", session.getId());
            log.info("[WS] Connection URI: {}", session.getUri());
            log.info("[WS] Connection headers: {}", session.getHandshakeHeaders());
            log.info("[WS] Remote address: {}", session.getRemoteAddress());
            
            // Extract JWT token from query parameters
            String query = session.getUri().getQuery();
            log.info("[WS] Query string: {}", query);
            String token = extractTokenFromQuery(query);
            if (token == null) {
                log.warn("[WS] Missing token for session {}", session.getId());
                session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"data\":{\"message\":\"Missing token\"}}"));
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            log.info("[WS] Token extracted successfully for session {}", session.getId());
            
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("[WS] Invalid token for session {}", session.getId());
                session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"data\":{\"message\":\"Invalid token\"}}"));
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            log.info("[WS] Token validated successfully for session {}", session.getId());
            
            // Extract board ID from URI path
            String path = session.getUri().getPath();
            log.info("[WS] Path: {}", path);
            // Handle the full path structure: /api/v1/ws/whiteboard/{boardId}
            int whiteboardIndex = path.indexOf("/ws/whiteboard/");
            if (whiteboardIndex == -1) {
                log.error("[WS] Invalid path format: {}", path);
                session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"data\":{\"message\":\"Invalid path format\"}}"));
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            String boardIdStr = path.substring(whiteboardIndex + "/ws/whiteboard/".length());
            log.info("[WS] Board ID extracted: {}", boardIdStr);
            UUID boardId = UUID.fromString(boardIdStr);
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
            log.info("[WS] User {} attempting to join board {}", userId, boardId);
            
            // Validate user access to board
            if (!whiteboardService.hasAccessToWhiteboard(boardId, userId)) {
                log.warn("[WS] User {} has no access to board {}", userId, boardId);
                session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"data\":{\"message\":\"No access to board\"}}"));
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            
            // Store session information
            String sessionId = session.getId();
            sessions.put(sessionId, session);
            sessionBoardIds.put(sessionId, boardId);
            sessionUserIds.put(sessionId, userId);
            
            // Add to board sessions for broadcasting
            boardSessions.computeIfAbsent(boardId, k -> new ConcurrentHashMap<>())
                    .put(sessionId, session);
            
            // Initialize cursor tracking for this board if not exists
            boardCursors.computeIfAbsent(boardId, k -> new ConcurrentHashMap<>());
            
            // Assign or get user color
            String userColor = userColors.computeIfAbsent(userId, k -> CURSOR_COLORS[random.nextInt(CURSOR_COLORS.length)]);
            
            // Add user cursor to board
            CursorPosition cursor = CursorPosition.builder()
                    .userId(userId)
                    .userName(jwtTokenProvider.getUsernameFromToken(token))
                    .userColor(userColor)
                    .x(100)
                    .y(100)
                    .lastUpdated(OffsetDateTime.now())
                    .isActive(true)
                    .build();
            
            boardCursors.get(boardId).put(userId, cursor);
            
            log.info("[WS] User {} joined board {} (session {})", userId, boardId, sessionId);
            
            // Send event replay to new connection
            sendEventReplay(session, boardId);
            
            // Send connection confirmation
            EventEnvelope connectionEvent = new EventEnvelope();
            connectionEvent.setType("CONNECTION_ESTABLISHED");
            connectionEvent.setBoardId(boardId.toString());
            connectionEvent.setUserId(userId.toString());
            connectionEvent.setTs(OffsetDateTime.now());
            
            Map<String, Object> connectionData = new HashMap<>();
            connectionData.put("message", "Connected to whiteboard");
            connectionEvent.setData(connectionData);
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(connectionEvent)));
            
            // Broadcast user joined event with cursor info
            broadcastCursorUpdate(boardId, cursor, "JOIN");
            
        } catch (Exception e) {
            log.error("[WS] Internal error for session {}: {}", session.getId(), e.getMessage(), e);
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"data\":{\"message\":\"Internal error: " + e.getMessage() + "\"}}"));
            session.close(new CloseStatus(1011, "Internal Error"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String sessionId = session.getId();
            UUID boardId = sessionBoardIds.get(sessionId);
            UUID userId = sessionUserIds.get(sessionId);

            if (boardId == null || userId == null) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            // Parse incoming event
            EventEnvelope eventEnvelope = objectMapper.readValue(message.getPayload(), EventEnvelope.class);
            eventEnvelope.setBoardId(boardId.toString());
            eventEnvelope.setUserId(userId.toString());
            eventEnvelope.setTs(OffsetDateTime.now());

            // Handle cursor movement events
            if ("CURSOR_MOVE".equals(eventEnvelope.getType())) {
                handleCursorMove(boardId, userId, eventEnvelope);
                return;
            }

            if ("CLEAR_CANVAS".equals(eventEnvelope.getType())) {
                // Delete all events for this board
                eventService.deleteEventsForBoard(boardId);
                // Broadcast CLEAR_CANVAS event to all users
                EventEnvelope clearEvent = new EventEnvelope();
                clearEvent.setType("CLEAR_CANVAS");
                clearEvent.setBoardId(boardId.toString());
                clearEvent.setUserId(userId.toString());
                clearEvent.setTs(OffsetDateTime.now());
                
                Map<String, Object> clearData = new HashMap<>();
                clearEvent.setData(clearData);
                
                broadcastToBoard(boardId, clearEvent);
                return;
            }

            // Process and persist event
            EventEnvelope processedEvent = eventService.processEvent(eventEnvelope, userId);

            // Broadcast to all sessions in the same board
            broadcastToBoard(boardId, processedEvent);

        } catch (Exception e) {
            // Send error message back to client
            EventEnvelope errorEvent = new EventEnvelope();
            errorEvent.setType("ERROR");
            errorEvent.setData(Map.of("message", "Failed to process event: " + e.getMessage()));
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorEvent)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        UUID boardId = sessionBoardIds.remove(sessionId);
        UUID userId = sessionUserIds.remove(sessionId);
        sessions.remove(sessionId);

        if (boardId != null && userId != null) {
            // Remove from board sessions
            Map<String, WebSocketSession> boardSessionMap = boardSessions.get(boardId);
            if (boardSessionMap != null) {
                boardSessionMap.remove(sessionId);
                if (boardSessionMap.isEmpty()) {
                    boardSessions.remove(boardId);
                }
            }
            
            // Remove user cursor and broadcast leave event
            Map<UUID, CursorPosition> boardCursorMap = boardCursors.get(boardId);
            if (boardCursorMap != null) {
                CursorPosition cursor = boardCursorMap.remove(userId);
                if (cursor != null) {
                    broadcastCursorUpdate(boardId, cursor, "LEAVE");
                }
                if (boardCursorMap.isEmpty()) {
                    boardCursors.remove(boardId);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[WS] Transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
        try {
            session.close(CloseStatus.PROTOCOL_ERROR);
        } catch (Exception e) {
            log.error("[WS] Error closing session {}: {}", session.getId(), e.getMessage());
        }
    }

    private void handleCursorMove(UUID boardId, UUID userId, EventEnvelope eventEnvelope) {
        try {
            Object dataObj = eventEnvelope.getData();
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                if (data.containsKey("x") && data.containsKey("y")) {
                    double x = Double.parseDouble(data.get("x").toString());
                    double y = Double.parseDouble(data.get("y").toString());
                    
                    // Update cursor position
                    Map<UUID, CursorPosition> boardCursorMap = boardCursors.get(boardId);
                    if (boardCursorMap != null) {
                        CursorPosition cursor = boardCursorMap.get(userId);
                        if (cursor != null) {
                            cursor.setX(x);
                            cursor.setY(y);
                            cursor.setLastUpdated(OffsetDateTime.now());
                            
                            // Broadcast cursor update to all users
                            broadcastCursorUpdate(boardId, cursor, "MOVE");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[WS] Error handling cursor move: {}", e.getMessage());
        }
    }

    private void broadcastCursorUpdate(UUID boardId, CursorPosition cursor, String eventType) {
        try {
            CursorUpdateEvent cursorEvent = CursorUpdateEvent.builder()
                    .userId(cursor.getUserId())
                    .userName(cursor.getUserName())
                    .userColor(cursor.getUserColor())
                    .x(cursor.getX())
                    .y(cursor.getY())
                    .timestamp(cursor.getLastUpdated())
                    .eventType(eventType)
                    .build();
            
            EventEnvelope envelope = new EventEnvelope();
            envelope.setType("CURSOR_UPDATE");
            envelope.setBoardId(boardId.toString());
            envelope.setUserId(cursor.getUserId().toString());
            envelope.setTs(OffsetDateTime.now());
            
            Map<String, Object> data = new HashMap<>();
            data.put("cursor", cursorEvent);
            envelope.setData(data);
            
            // Send cursor updates directly to all sessions without EVENT_APPEND wrapper
            Map<String, WebSocketSession> boardSessionMap = boardSessions.get(boardId);
            if (boardSessionMap != null) {
                try {
                    String eventJson = objectMapper.writeValueAsString(envelope);
                    TextMessage message = new TextMessage(eventJson);
                    
                    log.info("[WS] Broadcasting cursor update {} to {} sessions in board {}", 
                        eventType, boardSessionMap.size(), boardId);
                    
                    boardSessionMap.values().forEach(session -> {
                        try {
                            if (session.isOpen()) {
                                session.sendMessage(message);
                            }
                        } catch (IOException e) {
                            log.error("[WS] Error sending cursor update to session {}: {}", 
                                session.getId(), e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    log.error("[WS] Error broadcasting cursor update: {}", e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("[WS] Error broadcasting cursor update: {}", e.getMessage());
        }
    }

    private void broadcastToBoard(UUID boardId, EventEnvelope event) {
        Map<String, WebSocketSession> boardSessionMap = boardSessions.get(boardId);
        if (boardSessionMap != null) {
            try {
                // Create an EVENT_APPEND wrapper that the frontend expects
                EventEnvelope appendEvent = new EventEnvelope();
                appendEvent.setType("EVENT_APPEND");
                appendEvent.setBoardId(boardId.toString());
                
                Map<String, Object> appendData = new HashMap<>();
                appendData.put("event", event);
                appendEvent.setData(appendData);
                
                String eventJson = objectMapper.writeValueAsString(appendEvent);
                TextMessage message = new TextMessage(eventJson);
                
                log.info("[WS] Broadcasting event {} to {} sessions in board {}", 
                    event.getType(), boardSessionMap.size(), boardId);
                
                boardSessionMap.values().forEach(session -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(message);
                        }
                    } catch (IOException e) {
                        log.error("[WS] Error sending message to session {}: {}", 
                            session.getId(), e.getMessage());
                    }
                });
            } catch (Exception e) {
                log.error("[WS] Error broadcasting event: {}", e.getMessage(), e);
            }
        }
    }

    private void sendEventReplay(WebSocketSession session, UUID boardId) throws IOException {
        try {
            // Get events in chunks for replay
            var events = eventService.getEventsForBoard(boardId, eventReplayChunkSize);
            
            if (!events.isEmpty()) {
                EventEnvelope replayEvent = new EventEnvelope();
                replayEvent.setType("EVENT_REPLAY_CHUNK");
                replayEvent.setBoardId(boardId.toString());
                
                Map<String, Object> replayData = new HashMap<>();
                replayData.put("events", events);
                replayEvent.setData(replayData);
                
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(replayEvent)));
            }
            
            // Send current cursor positions for all users
            Map<UUID, CursorPosition> boardCursorMap = boardCursors.get(boardId);
            if (boardCursorMap != null && !boardCursorMap.isEmpty()) {
                EventEnvelope cursorsEvent = new EventEnvelope();
                cursorsEvent.setType("CURSORS_INIT");
                cursorsEvent.setBoardId(boardId.toString());
                
                Map<String, Object> cursorsData = new HashMap<>();
                cursorsData.put("cursors", boardCursorMap.values());
                cursorsEvent.setData(cursorsData);
                
                // Send cursors init directly without EVENT_APPEND wrapper
                String cursorsJson = objectMapper.writeValueAsString(cursorsEvent);
                session.sendMessage(new TextMessage(cursorsJson));
            }
            
        } catch (Exception e) {
            // Log error but don't fail connection
            log.error("[WS] Error sending event replay: {}", e.getMessage());
        }
    }

    private String extractTokenFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

    public int getActiveSessionsCount() {
        return sessions.size();
    }
}
