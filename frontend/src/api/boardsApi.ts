import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add auth interceptor
api.interceptors.request.use((config) => {
    const token = useAuthStore.getState().token;
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export interface Whiteboard {
    id: string;
    name: string;
    ownerName: string;
    createdAt: string;
    owner: boolean;
    isPublic: boolean;
}

export interface CreateWhiteboardRequest {
    name: string;
}

export const boardsApi = {
    getBoards: async (): Promise<Whiteboard[]> => {
        const response = await api.get('/whiteboards');
        return response.data;
    },

    createBoard: async (name: string): Promise<Whiteboard> => {
        const response = await api.post('/whiteboards', { name });
        return response.data;
    },

    joinBoard: async (boardId: string): Promise<void> => {
        await api.post(`/whiteboards/${boardId}/join`);
    },

    getBoardById: async (boardId: string): Promise<Whiteboard> => {
        const response = await api.get(`/whiteboards/${boardId}`);
        return response.data;
    },

    deleteBoard: async (boardId: string): Promise<void> => {
        await api.delete(`/whiteboards/${boardId}`);
    },
};
