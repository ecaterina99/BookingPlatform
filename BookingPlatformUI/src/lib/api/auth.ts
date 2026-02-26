import {api} from './client';
import type {AuthResponse, UserDTO} from '$lib/types';

export const authApi = {
    login: (email: string, password: string) =>
        api.post<AuthResponse>('/api/auth/login', {email, password}),
    register: (email: string, password: string, fullName: string) =>
        api.post<AuthResponse>('/api/auth/register', {email, password, fullName}),
    getCurrent: () => api.get<UserDTO>('/api/auth/get/current')
};
