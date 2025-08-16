import { create } from 'zustand';
import { boardsApi } from '../api/boardsApi';

export interface Whiteboard {
    id: string;
    name: string;
    ownerId: string;
    createdAt: string;
    isOwner: boolean;
}

interface BoardsState {
    whiteboards: Whiteboard[];
    isLoading: boolean;
    error: string | null;
    isBackendAvailable: boolean;
}

interface BoardsActions {
    fetchBoards: () => Promise<void>;
    createBoard: (name: string) => Promise<void>;
    joinBoard: (boardId: string) => Promise<void>;
    clearError: () => void;
    checkBackendAvailability: () => Promise<void>;
}

export const useBoardsStore = create<BoardsState & BoardsActions>((set, get) => ({
    whiteboards: [],
    isLoading: false,
    error: null,
    isBackendAvailable: true,

    fetchBoards: async () => {
        set({ isLoading: true, error: null });
        try {
            const boards = await boardsApi.getBoards();
            set({ whiteboards: boards, isLoading: false, isBackendAvailable: true });
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || error.message || 'Failed to fetch whiteboards';
            set({
                isLoading: false,
                error: errorMessage,
                isBackendAvailable: error.response ? true : false
            });
        }
    },

    createBoard: async (name: string) => {
        set({ isLoading: true, error: null });
        try {
            const newBoard = await boardsApi.createBoard(name);
            set((state) => ({
                whiteboards: [...state.whiteboards, newBoard],
                isLoading: false,
                isBackendAvailable: true
            }));
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || error.message || 'Failed to create whiteboard';
            set({
                isLoading: false,
                error: errorMessage,
                isBackendAvailable: error.response ? true : false
            });
        }
    },

    joinBoard: async (boardId: string) => {
        set({ isLoading: true, error: null });
        try {
            await boardsApi.joinBoard(boardId);
            set({ isLoading: false, isBackendAvailable: true });
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || error.message || 'Failed to join whiteboard';
            set({
                isLoading: false,
                error: errorMessage,
                isBackendAvailable: error.response ? true : false
            });
        }
    },

    clearError: () => {
        set({ error: null });
    },

    checkBackendAvailability: async () => {
        try {
            await fetch('http://localhost:8080/actuator/health');
            set({ isBackendAvailable: true });
        } catch (error) {
            set({ isBackendAvailable: false });
        }
    },
}));
