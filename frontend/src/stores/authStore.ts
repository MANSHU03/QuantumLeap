import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../api/authApi';

export interface User {
    id: string;
    email: string;
    name: string;
    createdAt: string;
}

interface AuthState {
    user: User | null;
    token: string | null;
    isLoading: boolean;
    error: string | null;
    isBackendAvailable: boolean;
}

interface AuthActions {
    login: (email: string, password: string) => Promise<void>;
    register: (email: string, password: string, name: string) => Promise<void>;
    logout: () => void;
    clearError: () => void;
    checkBackendAvailability: () => Promise<void>;
}

export const useAuthStore = create<AuthState & AuthActions>()(
    persist(
        (set, get) => ({
            user: null,
            token: null,
            isLoading: false,
            error: null,
            isBackendAvailable: true,

            login: async (email: string, password: string) => {
                set({ isLoading: true, error: null });
                try {
                    const response = await authApi.login(email, password);
                    set({
                        user: response.user,
                        token: response.accessToken,
                        isLoading: false,
                        error: null,
                        isBackendAvailable: true
                    });
                } catch (error: any) {
                    const errorMessage = error.response?.data?.message || error.message || 'Login failed';
                    set({
                        isLoading: false,
                        error: errorMessage,
                        isBackendAvailable: error.response ? true : false
                    });
                }
            },

            register: async (email: string, password: string, name: string) => {
                set({ isLoading: true, error: null });
                try {
                    await authApi.register(email, password, name);
                    set({ isLoading: false, error: null, isBackendAvailable: true });
                } catch (error: any) {
                    const errorMessage = error.response?.data?.message || error.message || 'Registration failed';
                    set({
                        isLoading: false,
                        error: errorMessage,
                        isBackendAvailable: error.response ? true : false
                    });
                }
            },

            logout: () => {
                set({ user: null, token: null, error: null });
            },

            clearError: () => {
                set({ error: null });
            },

            checkBackendAvailability: async () => {
                try {
                    // Use the correct backend health endpoint from env
                    const healthUrl = `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')}/actuator/health`;
                    await fetch(healthUrl);
                    set({ isBackendAvailable: true });
                } catch (error) {
                    set({ isBackendAvailable: false });
                }
            },
        }),
        {
            name: 'auth-storage',
            partialize: (state) => ({ user: state.user, token: state.token }),
        }
    )
);
