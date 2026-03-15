<script lang="ts">
    import {onMount} from "svelte";
    import {page} from '$app/stores';
    import {organizationsApi} from "$lib/api/organizations";
    import {servicesApi} from "$lib/api/services";
    import type {OrganizationDTO, ServiceCategoryType} from "$lib/types";
    import {MapPin, ArrowRight, Search} from 'lucide-svelte';

    const categoryImages: Record<ServiceCategoryType, string[]> = {
        MASSAGE: ['/pexels-anntarazevich-6560304.jpg', '/pexels-cottonbro-3997982.jpg'],
        HAIR: ['/pexels-cottonbro-3993453.jpg'],
        BARBER: ['/pexels-cottonbro-3998414.jpg', '/pexels-hikaique-331989.jpg', '/pexels-thgusstavo-2076930.jpg'],
        TATTOO: ['/pexels-cottonbro-4125659.jpg', '/pexels-vanyaoboleninov-3671414.jpg'],
        SKIN_CARE: ['/pexels-shiny-diamond-3762664.jpg', '/pexels-shiny-diamond-3762871.jpg', '/pexels-cottonbro-6635922 (1).jpg'],
        NAILS: ['/pexels-element5-973405.jpg', '/pexels-leeloothefirst-4677845.jpg'],
        MAKEUP: ['/pexels-pnw-prod-9219006.jpg', '/pexels-george-milton-6954214.jpg', '/pexels-shkrabaanthony-5178001.jpg'],
        OTHER: ['/pexels-shiny-diamond-3762441.jpg', '/pexels-pnw-prod-9219006 2.jpg'],
    };

    const categoryCounts: Record<string, number> = {};

    const nameOverrides: Record<string, string> = {
        'SmileDent': '/pexels-shiny-diamond-3762441.jpg',
        'BodySculptClub': '/pexels-cottonbro-3997982.jpg',
        'FitnessPro': '/pexels-olly-866027.jpg',
        'FitLife Center': '/pexels-olly-3768593 (1).jpg',
        'SkinGlow': '/pexels-cottonbro-6635922 (1).jpg',
        'BrowBar': '/pexels-shiny-diamond-3762664.jpg',
        'SkinEssence': '/pexels-shiny-diamond-3762871.jpg',
    };

    let allOrganizations: OrganizationDTO[] = [];
    let orgImages = new Map<number, string>();
    let loading = true;
    let searchQuery = '';
    let activeCity: string = 'ALL';

    onMount(async () => {
        const cityParam = $page.url.searchParams.get('city');
        if (cityParam) activeCity = cityParam;

        const [orgs, allServices] = await Promise.all([
            organizationsApi.getAll(),
            servicesApi.getAll()
        ]);
        allOrganizations = orgs;

        for (const org of orgs) {
            if (nameOverrides[org.name]) {
                orgImages.set(org.id, nameOverrides[org.name]);
            } else {
                const orgServices = allServices.filter(s => s.organizationId === org.id);
                const primary = orgServices.length > 0 ? orgServices[0].category : 'OTHER';
                const images = categoryImages[primary] ?? categoryImages.OTHER;
                const idx = categoryCounts[primary] ?? 0;
                orgImages.set(org.id, images[idx % images.length]);
                categoryCounts[primary] = idx + 1;
            }
        }

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
                   class="group bg-white border border-brand-200 rounded-xl overflow-hidden hover:border-gold-400 hover:shadow-sm transition-all flex">
                    <img src={orgImages.get(org.id) ?? categoryImages.OTHER[0]}
                         alt={org.name}
                         class="w-28 h-28 object-cover shrink-0" />
                    <div class="flex items-start justify-between flex-1 p-5">
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
