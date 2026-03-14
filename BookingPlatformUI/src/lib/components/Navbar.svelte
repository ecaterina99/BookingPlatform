<script lang="ts">
    import {auth, isLoggedIn, currentUser, isGlobalAdmin, isSpecialist, isOrgAdmin} from '$lib/stores/auth';
    import {goto} from '$app/navigation';
    import {page} from '$app/stores';
    import {CalendarDays, Building2, Scissors, User, LogOut, LogIn, UserPlus, LayoutDashboard, Shield, Clock} from 'lucide-svelte';

    function logout() {
        auth.logout();
        goto('/login');
    }

    let mobileOpen = false;

    function isActive(path: string): boolean {
        return $page.url.pathname === path || $page.url.pathname.startsWith(path + '/');
    }
</script>

<nav class="bg-white/80 backdrop-blur-md border-b border-brand-200 sticky top-0 z-40">
    <div class="max-w-7xl mx-auto px-6">
        <div class="flex items-center justify-between h-16">
            <!-- Logo -->
            <a href="/dashboard" class="flex items-center gap-2 group">
                <span class="font-serif text-2xl font-bold tracking-wide text-brand-800 group-hover:text-gold-500 transition-colors">TIME</span>
                <span class="hidden sm:block text-[10px] uppercase tracking-[0.2em] text-brand-400 font-medium leading-tight mt-1">Booking &<br/>Scheduling</span>
            </a>

            <!-- Desktop nav -->
            <div class="hidden md:flex items-center gap-1">
                <a href="/organizations"
                   class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all {isActive('/organizations') ? 'bg-brand-100 text-brand-800' : 'text-brand-600 hover:bg-brand-50 hover:text-brand-800'}">
                    <Building2 size={16} />
                    Organizations
                </a>
                <a href="/bookings/new"
                   class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all {isActive('/bookings/new') ? 'bg-brand-100 text-brand-800' : 'text-brand-600 hover:bg-brand-50 hover:text-brand-800'}">
                    <Scissors size={16} />
                    Book a Service
                </a>

                {#if $isLoggedIn}
                    <a href="/bookings"
                       class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all {isActive('/bookings') && !isActive('/bookings/new') ? 'bg-brand-100 text-brand-800' : 'text-brand-600 hover:bg-brand-50 hover:text-brand-800'}">
                        <CalendarDays size={16} />
                        My Bookings
                    </a>

                    {#if $isGlobalAdmin}
                        <a href="/global-admin"
                           class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all {isActive('/global-admin') ? 'bg-brand-100 text-brand-800' : 'text-brand-600 hover:bg-brand-50 hover:text-brand-800'}">
                            <Shield size={16} />
                            Platform
                        </a>
                    {/if}
                    {#if $isSpecialist}
                        <a href="/specialist"
                           class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all {isActive('/specialist') ? 'bg-brand-100 text-brand-800' : 'text-brand-600 hover:bg-brand-50 hover:text-brand-800'}">
                            <Clock size={16} />
                            Specialist
                        </a>
                    {/if}
                    {#if $isOrgAdmin}
                        <a href="/admin"
                           class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all {isActive('/admin') ? 'bg-brand-100 text-brand-800' : 'text-brand-600 hover:bg-brand-50 hover:text-brand-800'}">
                            <LayoutDashboard size={16} />
                            Admin
                        </a>
                    {/if}

                    <div class="w-px h-6 bg-brand-200 mx-2"></div>

                    <a href="/profile"
                       class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all {isActive('/profile') ? 'bg-brand-100 text-brand-800' : 'text-brand-600 hover:bg-brand-50 hover:text-brand-800'}">
                        <User size={16} />
                        {$currentUser?.fullName ?? 'Profile'}
                    </a>
                    <button on:click={logout}
                            class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-brand-500 hover:bg-red-50 hover:text-red-600 transition-all">
                        <LogOut size={16} />
                    </button>
                {:else}
                    <div class="w-px h-6 bg-brand-200 mx-2"></div>
                    <a href="/login"
                       class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-50 hover:text-brand-800 transition-all">
                        <LogIn size={16} />
                        Login
                    </a>
                    <a href="/register"
                       class="flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm font-medium bg-brand-800 text-brand-100 hover:bg-brand-700 transition-all">
                        <UserPlus size={16} />
                        Register
                    </a>
                {/if}
            </div>

            <!-- Mobile toggle -->
            <button on:click={() => mobileOpen = !mobileOpen} class="md:hidden p-2 text-brand-600">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    {#if mobileOpen}
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                    {:else}
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                    {/if}
                </svg>
            </button>
        </div>

        <!-- Mobile menu -->
        {#if mobileOpen}
            <div class="md:hidden border-t border-brand-200 py-3 flex flex-col gap-1">
                <a href="/organizations" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">Organizations</a>
                <a href="/bookings/new" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">Book a Service</a>
                {#if $isLoggedIn}
                    <a href="/bookings" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">My Bookings</a>
                    <a href="/profile" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">Profile</a>
                    {#if $isGlobalAdmin}<a href="/global-admin" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">Platform</a>{/if}
                    {#if $isSpecialist}<a href="/specialist" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">Specialist</a>{/if}
                    {#if $isOrgAdmin}<a href="/admin" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">Admin</a>{/if}
                    <button on:click={logout} class="px-3 py-2 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 text-left">Logout</button>
                {:else}
                    <a href="/login" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">Login</a>
                    <a href="/register" class="px-3 py-2 rounded-lg text-sm font-medium text-brand-600 hover:bg-brand-100">Register</a>
                {/if}
            </div>
        {/if}
    </div>
</nav>
