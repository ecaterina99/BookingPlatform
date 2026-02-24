<script lang="ts">
    import { onMount } from 'svelte';
    // SvelteKit store that contains information about the current route.
    import { page } from '$app/stores';
    import { organizationsApi } from '$lib/api/organizations';
    import { servicesApi } from '$lib/api/services';
    import type { OrganizationDTO, ServiceDTO } from '$lib/types';
    import {get} from "svelte/store";
    import {currentUser} from "$lib/stores/auth";
    import {bookingsApi} from "$lib/api/bookings";
    import {goto} from "$app/navigation";
    import {ApiError} from "$lib/api/client";

    // SvelteKit store that contains information about the current route.
    const id = Number($page.params.id);

    // Holds the loaded organization data.
    // Initially null because data is not yet fetched.
    let org: OrganizationDTO | null = null;
    let services: ServiceDTO[] = [];
    let loading = true;
    let error = '';

    let selectedService: ServiceDTO | null = null;
    let specialistId: number | null = null;
    let startDateTime: string = '';
    let endDateTime: string = '';
    let submitting = false;
    let bookingError = '';

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

    function selectService(service: ServiceDTO) {
        selectedService = service;
        bookingError = '';
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
                start: startDateTime,
                end: endDateTime
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
    {:else if selectedService}
        <div class="border rounded-lg p-6 max-w-md">
            <h2 class="text-xl font-semibold mb-4">
                Booking: {selectedService.name}
            </h2>

            {#if bookingError}
                <p class="text-red-600 text-sm mb-4">{bookingError}</p>
            {/if}

            <div class="flex flex-col gap-4">
                <div class="flex flex-col gap-1">
                    <label class="text-sm font-medium">Specialist ID</label>
                    <input bind:value={specialistId} type="number"
                           placeholder="Enter specialist ID"
                           class="border rounded px-3 py-2" required />
                </div>

                <div class="flex flex-col gap-1">
                    <label class="text-sm font-medium">Start Date and time</label>
                    <input bind:value={startDateTime} type="datetime-local"
                           class="border rounded px-3 py-2" required />
                </div>

                <div class="flex flex-col gap-1">
                    <label class="text-sm font-medium">End Date and time</label>
                    <input bind:value={endDateTime} type="datetime-local"
                           class="border rounded px-3 py-2" required />
                </div>

                <div class="flex gap-3">
                    <button on:click={create} disabled={submitting}
                            class="bg-blue-600 text-white rounded px-4 py-2 disabled:opacity-50">
                        {submitting ? 'Booking...' : 'Confirm booking'}
                    </button>
                    <button on:click={() => selectedService = null}
                            class="border rounded px-4 py-2 text-sm">
                        ← Back to services
                    </button>
                </div>
            </div>
        </div>
        {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            {#each services as serv}
                <div class="border rounded-lg p-4">
                    <h3 class="font-semibold text-lg">{serv.name}</h3>
                    <p class="text-gray-500 text-sm mb-2">{serv.description}</p>
                    <p class="text-sm">Duration: {serv.durationMinutes} min</p>
                    <p class="text-sm">Price: {serv.price}</p>
                    <button on:click={() => selectService(serv)}
                            class="mt-3 bg-blue-600 text-white text-sm px-3 py-1 rounded">
                        Book
                    </button>
                </div>
            {/each}
        </div>
    {/if}
{/if}
