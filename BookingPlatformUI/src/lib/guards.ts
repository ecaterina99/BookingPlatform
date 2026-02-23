import { get } from 'svelte/store';
import { goto } from '$app/navigation';
import { auth } from '$lib/stores/auth';

export function requireAuth() {
    if (!get(auth).token) goto('/login');
}

