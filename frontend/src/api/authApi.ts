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

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    name: string;
}

export interface LoginResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    user: {
        id: string;
        email: string;
        name: string;
        createdAt: string;
    };
}

export const authApi = {
    login: async (email: string, password: string): Promise<LoginResponse> => {
        const response = await api.post('/auth/login', { email, password });
        return response.data;
    },

    register: async (email: string, password: string, name: string): Promise<void> => {
        await api.post('/auth/register', { email, password, name });
    },
};
