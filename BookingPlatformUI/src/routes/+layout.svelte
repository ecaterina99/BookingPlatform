<script>
    import '../app.css';
    import Navbar from '$lib/components/Navbar.svelte';
    import { page } from '$app/stores';
</script>

<Navbar/>
{#if $page.url.pathname.startsWith('/admin') || $page.url.pathname.startsWith('/global-admin')}
    <slot/>
{:else if $page.url.pathname === '/login' || $page.url.pathname === '/register'}
    <slot/>
{:else}
    <main class="max-w-6xl mx-auto px-6 py-10">
        <slot/>
    </main>
{/if}
