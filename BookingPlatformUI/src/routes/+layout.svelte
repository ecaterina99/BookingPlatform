<script>
    import '../app.css';
    import Navbar from '$lib/components/Navbar.svelte';
    import { page } from '$app/stores';
</script>

<Navbar/>
{#if $page.url.pathname.startsWith('/admin') || $page.url.pathname.startsWith('/global-admin')}
    <slot/>
{:else}
    <main class="max-w-5xl mx-auto px-4 py-8">
        <slot/>
    </main>
{/if}
