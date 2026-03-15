<script lang="ts">
    import {goto} from '$app/navigation';
    import {authApi} from '$lib/api/auth';
    import {auth} from '$lib/stores/auth';
    import {ApiError} from '$lib/api/client';
    import {organizationsApi} from '$lib/api/organizations';
    import {Mail, Lock, ArrowRight} from 'lucide-svelte';

    let email = '';
    let password = '';
    let error = '';
    let loading = false;

    const testAccounts = [
        { label: 'User', email: 'user@test.com', password: 'test123' },
        { label: 'Specialist', email: 'specialist@test.com', password: 'test123' },
        { label: 'Org Admin', email: 'admin@test.com', password: 'test123' },
        { label: 'Global Admin', email: 'globaladmin@test.com', password: 'test123' },
    ];

    function fillCredentials(account: typeof testAccounts[0]) {
        email = account.email;
        password = account.password;
    }

    async function handleSubmit() {
        error = '';
        loading = true;
        try {
            const res = await authApi.login(email, password);
            auth.login(res.token, {id: 0, email: '', fullName: '', globalRole: 'USER', accountStatus: 'ACTIVE'},[]);
            const [user, memberships] = await Promise.all([
                authApi.getCurrent(),
                organizationsApi.getMembership()
            ])
            auth.login(res.token, user,memberships);
            goto('/dashboard');
        } catch (e) {
            if (e instanceof ApiError) error = e.message;
            else error = 'Cannot reach the server. Is Spring Boot running on port 8085?';
        } finally {
            loading = false;
        }
    }
</script>

<div class="min-h-screen flex items-center justify-center bg-brand-50 px-4">
    <div class="w-full max-w-sm">
        <!-- Logo -->
        <div class="text-center mb-8">
            <a href="/dashboard" class="inline-block">
                <span class="font-serif text-3xl font-bold tracking-wide text-brand-800">TIME</span>
            </a>
            <p class="text-brand-400 text-xs uppercase tracking-[0.2em] mt-1">Booking & Scheduling</p>
        </div>

        <div class="bg-white rounded-xl border border-brand-200 shadow-sm p-8">
            <h1 class="text-xl font-serif font-semibold text-brand-800 mb-6">Sign in</h1>

            {#if error}
                <div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-5">
                    {error}
                </div>
            {/if}

            <form on:submit|preventDefault={handleSubmit} class="flex flex-col gap-4">
                <div class="flex flex-col gap-1.5">
                    <label class="text-sm font-medium text-brand-700">Email</label>
                    <div class="relative">
                        <div class="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400"><Mail size={16} /></div>
                        <input bind:value={email} type="email" placeholder="your@email.com"
                               class="w-full border border-brand-200 rounded-lg pl-10 pr-4 py-2.5 text-sm bg-brand-50/50 focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition" required/>
                    </div>
                </div>

                <div class="flex flex-col gap-1.5">
                    <label class="text-sm font-medium text-brand-700">Password</label>
                    <div class="relative">
                        <div class="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400"><Lock size={16} /></div>
                        <input bind:value={password} type="password" placeholder="Enter password"
                               class="w-full border border-brand-200 rounded-lg pl-10 pr-4 py-2.5 text-sm bg-brand-50/50 focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition" required/>
                    </div>
                </div>

                <button type="submit" disabled={loading}
                        class="flex items-center justify-center gap-2 bg-brand-800 text-brand-100 rounded-lg px-4 py-2.5 text-sm font-medium hover:bg-brand-700 disabled:opacity-50 transition-colors mt-2">
                    {#if loading}
                        Signing in...
                    {:else}
                        Sign in <ArrowRight size={16} />
                    {/if}
                </button>
            </form>
        </div>

        <p class="text-center mt-5 text-sm text-brand-500">
            Don't have an account?
            <a href="/register" class="font-medium text-gold-500 hover:text-gold-600 transition-colors">Create one</a>
        </p>

        <!-- Test accounts -->
        <div class="mt-6 bg-white rounded-xl border border-brand-200 shadow-sm p-5">
            <p class="text-xs font-medium text-brand-500 uppercase tracking-wide mb-3">Test Accounts</p>
            <div class="grid grid-cols-2 gap-2">
                {#each testAccounts as account}
                    <button on:click={() => fillCredentials(account)}
                            class="text-left border border-brand-100 rounded-lg px-3 py-2.5 hover:border-gold-400 hover:bg-gold-50/50 transition-all">
                        <span class="block text-xs font-semibold text-brand-700">{account.label}</span>
                        <span class="block text-[11px] text-brand-400 mt-0.5">{account.email}</span>
                    </button>
                {/each}
            </div>
            <p class="text-[11px] text-brand-300 mt-2.5 text-center">Click to fill credentials. Password: <span class="font-mono">test123</span></p>
        </div>
    </div>
</div>
