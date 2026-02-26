<script lang="ts">
    import {goto} from '$app/navigation';
    import {authApi} from '$lib/api/auth';
    import {auth} from '$lib/stores/auth';
    import {ApiError} from '$lib/api/client';

    let fullName = '';
    let email = '';
    let password = '';
    let error = '';
    let fieldErrors: Record<string, string> = {};
    let loading = false;

    async function handleSubmit() {
        error = '';
        fieldErrors = {};
        loading = true;
        try {
            const res = await authApi.register(email, password, fullName);
            // Seed the token first so getCurrent() can attach it to the request
            auth.login(res.token, {id: 0, email: '', fullName: '', globalRole: 'USER'});
            const user = await authApi.getCurrent();
            auth.login(res.token, user);
            goto('/');
        } catch (e) {
            if (e instanceof ApiError) {
                if (e.fieldErrors) fieldErrors = e.fieldErrors;
                else error = e.message;
            } else {
                error = 'Cannot reach the server. Is Spring Boot running on port 8085?';
            }
        } finally {
            loading = false;
        }
    }
</script>

<div class="min-h-screen flex items-center justify-center">
    <form on:submit|preventDefault={handleSubmit} class="flex flex-col gap-4 w-80">
        <h1 class="text-2xl font-bold">Create account</h1>

        {#if error}
            <p class="text-red-600 text-sm">{error}</p>
        {/if}

        <div class="flex flex-col gap-1">
            <input bind:value={fullName} type="text" placeholder="Full name"
                   class="border rounded px-3 py-2" required/>
            {#if fieldErrors.fullName}
                <p class="text-red-500 text-xs">{fieldErrors.fullName}</p>
            {/if}
        </div>

        <div class="flex flex-col gap-1">
            <input bind:value={email} type="email" placeholder="Email"
                   class="border rounded px-3 py-2" required/>
            {#if fieldErrors.email}
                <p class="text-red-500 text-xs">{fieldErrors.email}</p>
            {/if}
        </div>

        <div class="flex flex-col gap-1">
            <input bind:value={password} type="password" placeholder="Password (min. 6 characters)"
                   class="border rounded px-3 py-2" required minlength="6"/>
            {#if fieldErrors.password}
                <p class="text-red-500 text-xs">{fieldErrors.password}</p>
            {/if}
        </div>

        <button type="submit" disabled={loading}
                class="bg-blue-600 text-white rounded px-4 py-2 disabled:opacity-50">
            {loading ? 'Creating account...' : 'Register'}
        </button>

        <a href="/login" class="text-sm text-center text-blue-600">Already have an account? Login</a>
    </form>
</div>
