<script lang="ts">
    import {onMount} from 'svelte';
    import {get} from 'svelte/store';
    import {auth, currentUser} from '$lib/stores/auth';
    import {requireAuth} from '$lib/guards';
    import {api, ApiError} from '$lib/api/client';
    import {User, Mail, Check} from 'lucide-svelte';

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
            auth.login(current.token!, {...current.user!, fullName, email}, current.memberships);
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
    <h1 class="text-2xl font-serif font-semibold text-brand-800 mb-8">My Profile</h1>

    {#if success}
        <div class="flex items-center gap-2 bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg px-4 py-3 mb-5">
            <Check size={16} /> Profile updated successfully.
        </div>
    {/if}

    {#if error}
        <div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-5">
            {error}
        </div>
    {/if}

    <form on:submit|preventDefault={handleSubmit} class="flex flex-col gap-5">
        <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-brand-700">Full name</label>
            <div class="relative">
                <div class="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400"><User size={16} /></div>
                <input bind:value={fullName} type="text"
                       class="w-full border border-brand-200 rounded-lg pl-10 pr-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition" required/>
            </div>
        </div>

        <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-brand-700">Email</label>
            <div class="relative">
                <div class="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400"><Mail size={16} /></div>
                <input bind:value={email} type="email"
                       class="w-full border border-brand-200 rounded-lg pl-10 pr-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition" required/>
            </div>
        </div>

        <button type="submit" disabled={loading}
                class="bg-brand-800 text-brand-100 rounded-lg px-5 py-2.5 text-sm font-medium hover:bg-brand-700 disabled:opacity-50 transition-colors w-fit">
            {loading ? 'Saving...' : 'Save changes'}
        </button>
    </form>
</div>
