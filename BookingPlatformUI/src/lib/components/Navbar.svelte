<script lang="ts">
    import {auth, isLoggedIn, currentUser, isGlobalAdmin} from '$lib/stores/auth';
    import {goto} from '$app/navigation';

    function logout() {
        auth.logout();
        goto('/login');
    }
</script>

<nav class="flex items-center gap-6 px-6 py-3 border-b">
    <a href="/dashboard" class="font-bold">BookingApp</a>
    <a href="/organizations">Organizations</a>
    <a href="/bookings/new">Book a service</a>

    {#if $isLoggedIn}
        <a href="/bookings">My Bookings</a>
        <a href="/profile">Profile</a>
        {#if $isGlobalAdmin}
            <a href="/admin/users">Admin</a>
        {/if}
        <span class="ml-auto text-sm text-gray-500">Hi, {$currentUser?.fullName}</span>
        <button on:click={logout} class="text-sm text-red-600">Logout</button>
    {:else}
        <a href="/login" class="ml-auto">Login</a>
        <a href="/register">Register</a>
    {/if}
</nav>
