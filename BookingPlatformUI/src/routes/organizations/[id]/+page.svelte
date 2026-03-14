<script lang="ts">
    import {onMount} from 'svelte';
    import {page} from '$app/stores';
    import {organizationsApi} from '$lib/api/organizations';
    import {servicesApi} from '$lib/api/services';
    import type {OrganizationDTO, ServiceDTO, SpecialistDTO, ServiceCategoryType} from '$lib/types';

    const CATEGORY_LABELS: Record<ServiceCategoryType, string> = {
        MAKEUP: 'Makeup', NAILS: 'Nails', BARBER: 'Barber', MASSAGE: 'Massage',
        TATTOO: 'Tattoo', HAIR: 'Hair', HEALTH_AND_FITNESS: 'Health & Fitness', SKIN_CARE: 'Skin Care', OTHER: 'Other'
    };
    import {get} from "svelte/store";
    import {currentUser} from "$lib/stores/auth";
    import {bookingsApi} from "$lib/api/bookings";
    import {goto} from "$app/navigation";
    import {ApiError} from "$lib/api/client";
    import {MapPin, Phone, Mail, Clock, ArrowLeft} from 'lucide-svelte';

    const id = Number($page.params.id);

    let org: OrganizationDTO | null = null;
    let services: ServiceDTO[] = [];
    let loading = true;
    let specialists: SpecialistDTO[] = [];
    let selectedService: ServiceDTO | null = null;
    let specialistId: number | null = null;
    let startDateTime: string = '';
    let submitting = false;
    let bookingError = '';
    let error = '';

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

    async function selectService(service: ServiceDTO) {
        selectedService = service;
        const allSpecialists = await organizationsApi.getSpecialists(service.organizationId);
        const user = get(currentUser);
        specialists = user ? allSpecialists.filter(s => s.userId !== user.id) : allSpecialists;
        error = '';
    }

    async function create() {
        if (!selectedService || !specialistId || !startDateTime) return;
        const user = get(currentUser);
        if (!user) return;
        submitting = true;
        bookingError = '';
        try {
            await bookingsApi.create({
                clientId: user.id,
                specialistId: specialistId,
                serviceId: selectedService.id,
                start: startDateTime
            });
            goto('/bookings');
        } catch (e) {
            if (e instanceof ApiError) bookingError = e.message;
            else bookingError = 'Something went wrong.';
        } finally {
            submitting = false;
        }
    }
</script>

{#if loading}
    <p class="text-brand-400">Loading...</p>
{:else if error}
    <div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">{error}</div>
{:else if org}
    <div class="mb-10">
        <h1 class="text-2xl font-serif font-semibold text-brand-800 mb-2">{org.name}</h1>
        <div class="flex flex-wrap gap-4 text-sm text-brand-500">
            <span class="flex items-center gap-1.5"><MapPin size={14} /> {org.city} &middot; {org.address}</span>
            <span class="flex items-center gap-1.5"><Phone size={14} /> {org.phone}</span>
            <span class="flex items-center gap-1.5"><Mail size={14} /> {org.email}</span>
        </div>
    </div>

    <h2 class="text-lg font-serif font-semibold text-brand-800 mb-5">Services</h2>

    {#if services.length === 0}
        <p class="text-brand-500">No services available for this organization.</p>
    {:else if selectedService}
        <div class="bg-white border border-brand-200 rounded-xl p-6 max-w-md">
            <h2 class="text-lg font-serif font-semibold text-brand-800 mb-5">
                Booking: {selectedService.name}
            </h2>

            {#if bookingError}
                <div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-5">{bookingError}</div>
            {/if}

            <div class="flex flex-col gap-4">
                <div class="flex flex-col gap-1.5">
                    <label class="text-sm font-medium text-brand-700">Specialist</label>
                    <select bind:value={specialistId}
                            class="border border-brand-200 rounded-lg px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition">
                        <option value={null} disabled>Select a specialist</option>
                        {#each specialists as s}
                            <option value={s.userId}>{s.fullName}</option>
                        {/each}
                    </select>
                </div>

                <div class="flex flex-col gap-1.5">
                    <label class="text-sm font-medium text-brand-700">Start Date and Time</label>
                    <input bind:value={startDateTime} type="datetime-local"
                           class="border border-brand-200 rounded-lg px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition" required/>
                </div>

                <div class="flex gap-3 mt-2">
                    <button on:click={create} disabled={submitting}
                            class="bg-brand-800 text-brand-100 rounded-lg px-5 py-2.5 text-sm font-medium hover:bg-brand-700 disabled:opacity-50 transition-colors">
                        {submitting ? 'Booking...' : 'Confirm booking'}
                    </button>
                    <button on:click={() => selectedService = null}
                            class="flex items-center gap-1.5 border border-brand-200 rounded-lg px-4 py-2.5 text-sm text-brand-600 hover:bg-brand-50 transition-colors">
                        <ArrowLeft size={14} /> Back
                    </button>
                </div>
            </div>
        </div>
    {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            {#each services as serv}
                <div class="bg-white border border-brand-200 rounded-xl p-5 hover:border-gold-400 hover:shadow-sm transition-all">
                    <div class="flex items-center gap-2 mb-2">
                        <h3 class="font-semibold text-brand-800">{serv.name}</h3>
                        <span class="text-xs font-medium px-2.5 py-0.5 rounded-full bg-brand-100 text-brand-600">
                            {CATEGORY_LABELS[serv.category] ?? serv.category}
                        </span>
                    </div>
                    <p class="text-brand-500 text-sm mb-2">{serv.description}</p>
                    <p class="text-sm text-brand-500 flex items-center gap-3 mb-3">
                        <span class="flex items-center gap-1"><Clock size={14} /> {serv.durationMinutes} min</span>
                        <span>{serv.price} RON</span>
                    </p>
                    <button on:click={() => selectService(serv)}
                            class="bg-brand-800 text-brand-100 text-sm px-4 py-2 rounded-lg font-medium hover:bg-brand-700 transition-colors">
                        Book
                    </button>
                </div>
            {/each}
        </div>
    {/if}
{/if}
