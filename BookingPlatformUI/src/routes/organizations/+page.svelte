<script lang="ts">
    import {onMount} from "svelte";
    import {organizationsApi} from "$lib/api/organizations";
    import type {OrganizationDTO} from "$lib/types";
    import {MapPin, ArrowRight, Search} from 'lucide-svelte';

    let allOrganizations: OrganizationDTO[] = [];
    let loading = true;
    let searchQuery = '';
    let activeCity: string = 'ALL';

    onMount(async () => {
        allOrganizations = await organizationsApi.getAll();
        loading = false;
    });

    $: cities = [...new Set(allOrganizations.map(o => o.city))].sort();

    $: filtered = allOrganizations.filter(org => {
        const matchesCity = activeCity === 'ALL' || org.city === activeCity;
        const q = searchQuery.trim().toLowerCase();
        const matchesSearch = !q || org.name.toLowerCase().includes(q) || org.city.toLowerCase().includes(q) || org.address.toLowerCase().includes(q);
        return matchesCity && matchesSearch;
    });
</script>

<div class="flex items-center justify-between mb-8">
    <h1 class="text-2xl font-serif font-semibold text-brand-800">Find an Organization</h1>
    {#if !loading && cities.length > 1}
        <div class="flex items-center gap-4">
            <div class="relative">
                <div class="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400"><Search size={16} /></div>
                <input bind:value={searchQuery} type="text" placeholder="Search..."
                       class="border border-brand-200 rounded-lg pl-10 pr-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition w-48" />
            </div>
            <div class="flex flex-col gap-1">
                <select bind:value={activeCity}
                        class="border border-brand-200 rounded-lg px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition">
                    <option value="ALL">All cities</option>
                    {#each cities as city}
                        <option value={city}>{city}</option>
                    {/each}
                </select>
            </div>
        </div>
    {/if}
</div>

{#if loading}
    <p class="text-brand-400">Loading...</p>
{:else}

    {#if filtered.length === 0}
        <p class="text-brand-500">No organizations match your search.</p>
    {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            {#each filtered as org}
                <a href="/organizations/{org.id}"
                   class="group bg-white border border-brand-200 rounded-xl p-5 hover:border-gold-400 hover:shadow-sm transition-all">
                    <div class="flex items-start justify-between">
                        <div>
                            <h2 class="font-semibold text-brand-800 group-hover:text-gold-600 transition-colors">{org.name}</h2>
                            <p class="text-brand-500 text-sm mt-1 flex items-center gap-1.5">
                                <MapPin size={14} /> {org.city} &middot; {org.address}
                            </p>
                        </div>
                        <ArrowRight size={18} class="text-brand-300 group-hover:text-gold-500 mt-1 transition-colors" />
                    </div>
                </a>
            {/each}
        </div>
    {/if}
{/if}
