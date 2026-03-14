<script lang="ts">
    import {goto} from '$app/navigation';
    import {authApi} from '$lib/api/auth';
    import {auth} from '$lib/stores/auth';
    import {ApiError} from '$lib/api/client';
    import {organizationsApi} from "$lib/api/organizations";
    import {Mail, Lock, User, ArrowRight} from 'lucide-svelte';

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
            auth.login(res.token, {id: 0, email: '', fullName: '', globalRole: 'USER', accountStatus: 'ACTIVE'},[]);
            const [user, memberships] = await Promise.all([
                authApi.getCurrent(),
                organizationsApi.getMembership()
            ])
            auth.login(res.token, user,memberships);
            goto('/dashboard');
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

<div class="min-h-screen flex items-center justify-center bg-brand-50 px-4">
    <div class="w-full max-w-sm">
        <div class="text-center mb-8">
            <a href="/dashboard" class="inline-block">
                <span class="font-serif text-3xl font-bold tracking-wide text-brand-800">TIME</span>
            </a>
            <p class="text-brand-400 text-xs uppercase tracking-[0.2em] mt-1">Booking & Scheduling</p>
        </div>

        <div class="bg-white rounded-xl border border-brand-200 shadow-sm p-8">
            <h1 class="text-xl font-serif font-semibold text-brand-800 mb-6">Create account</h1>

            {#if error}
                <div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-5">
                    {error}
                </div>
            {/if}

            <form on:submit|preventDefault={handleSubmit} class="flex flex-col gap-4">
                <div class="flex flex-col gap-1.5">
                    <label class="text-sm font-medium text-brand-700">Full name</label>
                    <div class="relative">
                        <div class="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400"><User size={16} /></div>
                        <input bind:value={fullName} type="text" placeholder="Your full name"
                               class="w-full border border-brand-200 rounded-lg pl-10 pr-4 py-2.5 text-sm bg-brand-50/50 focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition" required/>
                    </div>
                    {#if fieldErrors.fullName}
                        <p class="text-red-500 text-xs">{fieldErrors.fullName}</p>
                    {/if}
                </div>

                <div class="flex flex-col gap-1.5">
                    <label class="text-sm font-medium text-brand-700">Email</label>
                    <div class="relative">
                        <div class="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400"><Mail size={16} /></div>
                        <input bind:value={email} type="email" placeholder="your@email.com"
                               class="w-full border border-brand-200 rounded-lg pl-10 pr-4 py-2.5 text-sm bg-brand-50/50 focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition" required/>
                    </div>
                    {#if fieldErrors.email}
                        <p class="text-red-500 text-xs">{fieldErrors.email}</p>
                    {/if}
                </div>

                <div class="flex flex-col gap-1.5">
                    <label class="text-sm font-medium text-brand-700">Password</label>
                    <div class="relative">
                        <div class="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400"><Lock size={16} /></div>
                        <input bind:value={password} type="password" placeholder="Min. 6 characters"
                               class="w-full border border-brand-200 rounded-lg pl-10 pr-4 py-2.5 text-sm bg-brand-50/50 focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition" required minlength="6"/>
                    </div>
                    {#if fieldErrors.password}
                        <p class="text-red-500 text-xs">{fieldErrors.password}</p>
                    {/if}
                </div>

                <button type="submit" disabled={loading}
                        class="flex items-center justify-center gap-2 bg-brand-800 text-brand-100 rounded-lg px-4 py-2.5 text-sm font-medium hover:bg-brand-700 disabled:opacity-50 transition-colors mt-2">
                    {#if loading}
                        Creating account...
                    {:else}
                        Create account <ArrowRight size={16} />
                    {/if}
                </button>
            </form>
        </div>

        <p class="text-center mt-5 text-sm text-brand-500">
            Already have an account?
            <a href="/login" class="font-medium text-gold-500 hover:text-gold-600 transition-colors">Sign in</a>
        </p>
    </div>
</div>
