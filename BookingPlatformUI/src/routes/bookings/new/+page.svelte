<script lang="ts">
    import {onMount} from 'svelte';
    import {goto} from '$app/navigation';
    import {servicesApi} from '$lib/api/services';
    import {bookingsApi} from '$lib/api/bookings';
    import {organizationsApi} from '$lib/api/organizations';
    import {currentUser} from '$lib/stores/auth';
    import {requireAuth} from '$lib/guards';
    import {ApiError} from '$lib/api/client';
    import {get} from 'svelte/store';
    import type {ServiceDTO, SpecialistDTO} from '$lib/types';

    requireAuth();
    let services: ServiceDTO[] = [];
    let orgNames: Map<number, string> = new Map();
    let loading = true;
    let selectedService: ServiceDTO | null = null;
    let specialistId: number | null = null;
    let startDateTime: string = '';
    let submitting = false;
    let error = '';
    let specialists: SpecialistDTO[] = [];


    onMount(async () => {
        const [allServices, allOrgs] = await Promise.all([
            servicesApi.getAll(),
            organizationsApi.getAll()
        ]);
        services = allServices;
        orgNames = new Map(allOrgs.map(o => [o.id, o.name]));
        loading = false;
    });

    async function selectService(service: ServiceDTO) {
        selectedService = service;
        specialists = await organizationsApi.getSpecialists(service.organizationId);
        error = '';
    }

    async function create() {
        if (!selectedService || !specialistId || !startDateTime) return;

        const user = get(currentUser);
        if (!user) return;

        submitting = true;
        error = '';
        try {
            await bookingsApi.create({
                    clientId: user.id,
                    specialistId: specialistId,
                    serviceId: selectedService.id,
                    start: startDateTime
                }
            );
            goto('/bookings');
        } catch (e) {
            if (e instanceof ApiError) error = e.message;
            else error = 'Something went wrong.';
        } finally {
            submitting = false;
        }
    }
</script>

<h1 class="text-3xl font-bold mb-6">Booking a service</h1>

{#if loading}
    <p>Loading...</p>
{:else if selectedService}
    <div class="border rounded-lg p-6 max-w-md">
        <h2 class="text-xl font-semibold mb-4">
            Booking: {selectedService.name}

            <select bind:value={specialistId} class="border rounded px-3 py-3">
                <option value={null} disabled>Select a specialist</option>
                {#each specialists as s}
                    <option value={s.userId}>{s.fullName}</option>
                {/each}
            </select>

        </h2>

        {#if error}
            <p class="text-red-600 text-sm mb-4">{error}</p>
        {/if}

        <div class="flex flex-col gap-4">
            <div class="flex flex-col gap-1">
                <label for="id" class="text-sm font-medium">Specialist ID</label>
                <input bind:value={specialistId} type="number"
                       placeholder="Enter specialist ID"
                       class="border rounded px-3 py-2" required/>
            </div>

            <div class="flex flex-col gap-1">
                <label for="id" class="text-sm font-medium">Start Date and time</label>
                <input bind:value={startDateTime} type="datetime-local"
                       class="border rounded px-3 py-2" required/>
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
            <div class="border rounded-lg p-4 hover:shadow transition">
                <h2 class="font-semibold text-lg">{serv.name}</h2>
                <p class="text-gray-500 text-sm">{serv.description}</p>
                <p class="text-gray-500 text-sm">{orgNames.get(serv.organizationId) ?? 'Unknown'}</p>
                <p class="text-gray-500 text-sm">Duration: {serv.durationMinutes} min</p>
                <p class="text-gray-500 text-sm">Price: {serv.price}</p>
                <button on:click={() => selectService(serv)}
                        class="mt-3 bg-blue-600 text-white text-sm px-3 py-1 rounded">
                    Book
                </button>
            </div>
        {/each}
    </div>
{/if}


