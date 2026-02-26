<script>
    import '../app.css';
    import Navbar from '$lib/components/Navbar.svelte';
</script>

<Navbar/>
<main class="max-w-5xl mx-auto px-4 py-8">
    <slot/>
</main>
