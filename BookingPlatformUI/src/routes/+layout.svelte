<script>
    import '../app.css';
    import Navbar from '$lib/components/Navbar.svelte';
    import Footer from '$lib/components/Footer.svelte';
    import { page } from '$app/stores';

    $: isAdminPage = $page.url.pathname.startsWith('/admin') || $page.url.pathname.startsWith('/global-admin');
</script>

<div class="flex flex-col min-h-screen">
    <Navbar/>
    {#if isAdminPage}
        <div class="flex-1">
            <slot/>
        </div>
    {:else if $page.url.pathname === '/login' || $page.url.pathname === '/register' || $page.url.pathname === '/dashboard' || $page.url.pathname === '/'}
        <div class="flex-1">
            <slot/>
        </div>
    {:else}
        <main class="flex-1 max-w-6xl mx-auto px-6 py-10 w-full">
            <slot/>
        </main>
    {/if}

    {#if !isAdminPage}
        <Footer />
    {/if}
</div>
