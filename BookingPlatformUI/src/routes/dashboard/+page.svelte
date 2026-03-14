<script lang="ts">
    import {isLoggedIn, currentUser} from '$lib/stores/auth';
    import {CalendarDays, Building2, Scissors, ArrowRight} from 'lucide-svelte';
</script>

<div class="flex flex-col items-center text-center pt-8 pb-16">
    <!-- Logo area -->
    <div class="mb-12">
        <div class="bg-brand-900 rounded-2xl px-12 py-10 shadow-lg">
            <h1 class="font-serif text-5xl font-bold tracking-wide text-transparent bg-clip-text bg-gradient-to-b from-gold-300 to-brand-400">TIME</h1>
            <p class="text-brand-300 text-xs uppercase tracking-[0.3em] mt-2 font-medium">Booking & Scheduling Platform</p>
        </div>
    </div>

    {#if $isLoggedIn}
        <h2 class="text-2xl font-serif font-semibold text-brand-800 mb-2">
            Welcome back, {$currentUser?.fullName}
        </h2>
        <p class="text-brand-500 mb-10 max-w-md">
            Manage your appointments, explore services, and stay on schedule.
        </p>
    {:else}
        <h2 class="text-2xl font-serif font-semibold text-brand-800 mb-2">
            Welcome to TIME
        </h2>
        <p class="text-brand-500 mb-10 max-w-md">
            Your premium booking and scheduling platform. Discover services, book appointments, and manage your time effortlessly.
        </p>
    {/if}

    <!-- Quick actions -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-5 w-full max-w-3xl">
        <a href="/bookings/new"
           class="group bg-white rounded-xl border border-brand-200 p-6 hover:border-gold-400 hover:shadow-md transition-all">
            <div class="w-10 h-10 rounded-lg bg-brand-100 flex items-center justify-center mb-4 group-hover:bg-gold-400/10 transition-colors">
                <Scissors size={20} class="text-brand-600 group-hover:text-gold-500" />
            </div>
            <h3 class="font-semibold text-brand-800 mb-1">Book a Service</h3>
            <p class="text-sm text-brand-500 mb-3">Browse and book from a wide range of services</p>
            <span class="text-sm font-medium text-gold-500 flex items-center gap-1 group-hover:gap-2 transition-all">
                Get started <ArrowRight size={14} />
            </span>
        </a>

        <a href="/organizations"
           class="group bg-white rounded-xl border border-brand-200 p-6 hover:border-gold-400 hover:shadow-md transition-all">
            <div class="w-10 h-10 rounded-lg bg-brand-100 flex items-center justify-center mb-4 group-hover:bg-gold-400/10 transition-colors">
                <Building2 size={20} class="text-brand-600 group-hover:text-gold-500" />
            </div>
            <h3 class="font-semibold text-brand-800 mb-1">Organizations</h3>
            <p class="text-sm text-brand-500 mb-3">Explore salons, studios, and wellness centers</p>
            <span class="text-sm font-medium text-gold-500 flex items-center gap-1 group-hover:gap-2 transition-all">
                Browse <ArrowRight size={14} />
            </span>
        </a>

        {#if $isLoggedIn}
            <a href="/bookings"
               class="group bg-white rounded-xl border border-brand-200 p-6 hover:border-gold-400 hover:shadow-md transition-all">
                <div class="w-10 h-10 rounded-lg bg-brand-100 flex items-center justify-center mb-4 group-hover:bg-gold-400/10 transition-colors">
                    <CalendarDays size={20} class="text-brand-600 group-hover:text-gold-500" />
                </div>
                <h3 class="font-semibold text-brand-800 mb-1">My Bookings</h3>
                <p class="text-sm text-brand-500 mb-3">View and manage your appointments</p>
                <span class="text-sm font-medium text-gold-500 flex items-center gap-1 group-hover:gap-2 transition-all">
                    View all <ArrowRight size={14} />
                </span>
            </a>
        {:else}
            <a href="/register"
               class="group bg-white rounded-xl border border-brand-200 p-6 hover:border-gold-400 hover:shadow-md transition-all">
                <div class="w-10 h-10 rounded-lg bg-brand-100 flex items-center justify-center mb-4 group-hover:bg-gold-400/10 transition-colors">
                    <CalendarDays size={20} class="text-brand-600 group-hover:text-gold-500" />
                </div>
                <h3 class="font-semibold text-brand-800 mb-1">Get Started</h3>
                <p class="text-sm text-brand-500 mb-3">Create an account to book appointments</p>
                <span class="text-sm font-medium text-gold-500 flex items-center gap-1 group-hover:gap-2 transition-all">
                    Register <ArrowRight size={14} />
                </span>
            </a>
        {/if}
    </div>
</div>
