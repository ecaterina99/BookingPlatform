<script lang="ts">
    import { onMount } from 'svelte';
    // SvelteKit store that contains information about the current route.
    import { page } from '$app/stores';
    import { organizationsApi } from '$lib/api/organizations';
    import { servicesApi } from '$lib/api/services';
    import type { OrganizationDTO, ServiceDTO } from '$lib/types';

    // SvelteKit store that contains information about the current route.
    const id = Number($page.params.id);

    // Holds the loaded organization data.
    // Initially null because data is not yet fetched.
    let org: OrganizationDTO | null = null;
    let services: ServiceDTO[] = [];
    let loading = true;
    let error = '';

    // Fetch data when component is mounted.
    onMount(async () => {
        try {
            const [fetchedOrg, allServices] = await Promise.all([
                organizationsApi.getById(id),
                servicesApi.getAll()
            ]);
            org = fetchedOrg;
            services = allServices.filter(s => s.organizationId === id);
        } catch (e) {
            error = 'Failed to load organization.';
        } finally {
            loading = false;
        }
    });
</script>

{#if loading}
    <p>Loading...</p>
{:else if error}
    <p class="text-red-600">{error}</p>
{:else if org}
    <div class="mb-8">
        <h1 class="text-3xl font-bold mb-1">{org.name}</h1>
        <p class="text-gray-500">{org.city} · {org.address}</p>
        <p class="text-gray-500">{org.phone} · {org.email}</p>
    </div>

    <h2 class="text-xl font-semibold mb-4">Services</h2>

    {#if services.length === 0}
        <p class="text-gray-500">No services available for this organization.</p>
    {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            {#each services as serv}
                <div class="border rounded-lg p-4">
                    <h3 class="font-semibold text-lg">{serv.name}</h3>
                    <p class="text-gray-500 text-sm mb-2">{serv.description}</p>
                    <p class="text-sm">Duration: {serv.durationMinutes} min</p>
                    <p class="text-sm">Price: {serv.price}</p>
                </div>
            {/each}
        </div>
    {/if}
{/if}
