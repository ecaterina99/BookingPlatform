<script>
    import '../app.css';
    import Navbar from '$lib/components/Navbar.svelte';
    import Footer from '$lib/components/Footer.svelte';
    import { page } from '$app/stores';

    $: isAdminPage = $page.url.pathname.startsWith('/admin') || $page.url.pathname.startsWith('/global-admin');
</script>

<Navbar/>
{#if isAdminPage}
    <slot/>
{:else if $page.url.pathname === '/login' || $page.url.pathname === '/register' || $page.url.pathname === '/dashboard' || $page.url.pathname === '/'}
    <slot/>
{:else}
    <main class="max-w-6xl mx-auto px-6 py-10">
        <slot/>
    </main>
{/if}

{#if !isAdminPage}
    <Footer />
{/if}
