import {get} from 'svelte/store';
import {auth} from '$lib/stores/auth';
import {goto} from '$app/navigation';

// Base API URL from environment variables
const BASE = import.meta.env.VITE_API_BASE;

// Custom error class for API errors
export class ApiError extends Error {
    constructor(
        public status: number,                 // HTTP status code (e.g. 400, 401)
        public code: string,                   // Error code from backend
        message: string,                      // Error message
        public fieldErrors?: Record<string, string> // Validation errors (optional)
    ) {
        super(message); // Call parent Error constructor
    }
}

// Generic request function used by all API methods
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {

    // Get JWT token from auth store
    const token = get(auth).token;

    // Prepare request headers
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(options.headers as Record<string, string>)
    };

    // Add Authorization header if token exists
    if (token) headers['Authorization'] = `Bearer ${token}`;

    // Send HTTP request
    const res = await fetch(`${BASE}${path}`, {...options, headers});

    // If unauthorized → logout and redirect to login
    if (res.status === 401) {
        auth.logout();
        goto('/login');
        throw new ApiError(401, 'UNAUTHORIZED', 'Session expired');
    }

    // If response is not successful → throw ApiError
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new ApiError(
            res.status,
            body.error ?? 'UNKNOWN',
            body.message ?? 'Something went wrong',
            body.fieldErrors
        );
    }

    // If no content, return undefined (handles both 204 and 200 with empty body)
    const text = await res.text();
    if (!text) return undefined as T;

    // Otherwise parse and return JSON response
    return JSON.parse(text);
}

// Public API helper methods
export const api = {

    get: <T>(path: string) => request<T>(path),
    post: <T>(path: string, body: unknown) =>
        request<T>(path, {method: 'POST', body: JSON.stringify(body)}),
    patch: <T>(path: string, body: unknown) =>
        request<T>(path, {method: 'PATCH', body: JSON.stringify(body)}),
    delete: <T>(path: string) =>
        request<T>(path, {method: 'DELETE'})
};