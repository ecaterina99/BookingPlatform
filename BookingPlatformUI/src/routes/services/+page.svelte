<script lang="ts">
    import { onMount } from 'svelte';
    import { servicesApi } from '$lib/api/services';
    import type { ServiceDTO } from '$lib/types';

    let services: ServiceDTO[] = [];
    let loading = true;

    onMount(async () => {
        services = await servicesApi.getAll();
        loading = false;
    });
</script>

<h1 class="text-3xl font-bold mb-6">Find a service</h1>

{#if loading}
    <p>Loading...</p>
{:else}
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        {#each services as serv}
            <a href="/services/{serv.id}"
               class="border rounded-lg p-4 hover:shadow transition">
                <h2 class="font-semibold text-lg">{serv.name}</h2>
                <p class="text-gray-500 text-sm">{serv.description}</p>
                <p class="text-gray-500 text-sm">Organization id:{serv.organizationId}</p>
                <p class="text-gray-500 text-sm">Duration: {serv.durationMinutes}</p>
                <p class="text-gray-500 text-sm">Price: {serv.price}</p>
            </a>
        {/each}
    </div>
{/if}
