<script lang="ts">
    import {onMount} from 'svelte';
    import {get} from 'svelte/store';
    import {auth, currentUser} from '$lib/stores/auth';
    import {requireAuth} from '$lib/guards';
    import {api, ApiError} from '$lib/api/client';

    let fullName: string = '';
    let email: string = '';
    let success: boolean = false;
    let error = '';
    let loading: boolean = false;

    onMount(async () => {
        requireAuth();
        const user = get(currentUser);
        if (user) {
            fullName = user.fullName;
            email = user.email;
        }
    });

    async function handleSubmit() {
        error = '';
        success = false;
        loading = true;
        try {
            const user = get(currentUser);
            await api.patch(`/api/users/${user?.id}`, {fullName, email});
            const current = get(auth);
            auth.login(current.token!, {...current.user!, fullName, email});
            success = true;
        } catch (e) {
            if (e instanceof ApiError) error = e.message;
            else error = 'Something went wrong.';
        } finally {
            loading = false;
        }
    }
</script>

<div class="max-w-md">
    <h1 class="text-2xl font-bold mb-6">My Profile</h1>

    {#if success}
        <p class="text-green-600 text-sm mb-4">Profile updated successfully.</p>
    {/if}

    {#if error}
        <p class="text-red-600 text-sm mb-4">{error}</p>
    {/if}

    <form on:submit|preventDefault={handleSubmit} class="flex flex-col gap-4">
        <div class="flex flex-col gap-1">
            <label for="id" class="text-sm font-medium">Full name</label>
            <input bind:value={fullName} type="text"
                   class="border rounded px-3 py-2" required/>
        </div>

        <div class="flex flex-col gap-1">
            <label for="id" class="text-sm font-medium">Email</label>
            <input bind:value={email} type="email"
                   class="border rounded px-3 py-2" required/>
        </div>

        <button type="submit" disabled={loading}
                class="bg-blue-600 text-white rounded px-4 py-2 disabled:opacity-50 w-fit">
            {loading ? 'Saving...' : 'Save changes'}
        </button>
    </form>
</div>