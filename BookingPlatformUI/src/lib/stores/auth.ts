import {writable, derived} from 'svelte/store';
import type {UserDTO} from '$lib/types';

interface AuthState {
    token: string | null;
    user: UserDTO | null;
}

function createAuthStore() {
    const stored = typeof localStorage !== 'undefined' ? localStorage.getItem('auth') : null;
    const initial: AuthState = stored ? JSON.parse(stored) : {token: null, user: null};

    const {subscribe, set} = writable<AuthState>(initial);

    return {
        subscribe,
        login(token: string, user: UserDTO) {
            const state = {token, user};
            localStorage.setItem('auth', JSON.stringify(state));
            set(state);
        },
        logout() {
            localStorage.removeItem('auth');
            set({token: null, user: null});
        }
    };

}

export const auth = createAuthStore();
export const isLoggedIn = derived(auth, $a => $a.token !== null);
export const currentUser = derived(auth, $a => $a.user);
export const isGlobalAdmin = derived(auth, $a => $a.user?.globalRole === 'GLOBAL_ADMIN');
