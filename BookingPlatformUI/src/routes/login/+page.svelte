<script lang="ts">
    import {goto} from '$app/navigation';
    import {authApi} from '$lib/api/auth';
    import {auth} from '$lib/stores/auth';
    import {ApiError} from '$lib/api/client';

    let email = '';
    let password = '';
    let error = '';
    let loading = false;

    async function handleSubmit() {
        error = '';
        loading = true;
        try {
            const res = await authApi.login(email, password);
            auth.login(res.token, {id: 0, email: '', fullName: '', globalRole: 'USER'});
            const user = await authApi.getCurrent();
            auth.login(res.token, user);
            goto('/');
        } catch (e) {
            if (e instanceof ApiError) error = e.message;
            else error = 'Cannot reach the server. Is Spring Boot running on port 8085?';
        } finally {
            loading = false;
        }
    }
</script>

<div class="min-h-screen flex items-center justify-center">
    <form on:submit|preventDefault={handleSubmit} class="flex flex-col gap-4 w-80">
        <h1 class="text-2xl font-bold">Login</h1>

        {#if error}
            <p class="text-red-600 text-sm">{error}</p>
        {/if}

        <input bind:value={email} type="email" placeholder="Email"
               class="border rounded px-3 py-2" required/>

        <input bind:value={password} type="password" placeholder="Password"
               class="border rounded px-3 py-2" required/>

        <button type="submit" disabled={loading}
                class="bg-blue-600 text-white rounded px-4 py-2 disabled:opacity-50">
            {loading ? 'Logging in...' : 'Login'}
        </button>

        <a href="/register" class="text-sm text-center text-blue-600">No account? Register</a>
    </form>
</div>
