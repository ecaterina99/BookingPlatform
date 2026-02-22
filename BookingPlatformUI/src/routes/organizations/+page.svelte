<script lang="ts">
    import {onMount} from "svelte";
    import {organizationsApi} from "$lib/api/organizations";
    import type {OrganizationDTO} from "$lib/types";


    let organizations: OrganizationDTO[] = [];
    let loading = true;
    onMount(async () => {
        organizations = await organizationsApi.getAll();
        loading = false;
    });
</script>

<h1 class="text-3xl font-bold mb-6">Find an organization</h1>

{#if loading}
    <p>Loading...</p>
{:else}
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        {#each organizations as org}
            <a href="/organizations/{org.id}"
               class="border rounded-lg p-4 hover:shadow transition">
                <h2 class="font-semibold text-lg">{org.name}</h2>
                <p class="text-gray-500 text-sm">{org.city} : {org.address} </p>
            </a>
        {/each}
    </div>
{/if}
