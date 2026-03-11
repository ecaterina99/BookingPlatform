<script lang="ts">
    import {onMount} from "svelte";
    import {bookingsApi} from "$lib/api/bookings";
    import {usersApi} from "$lib/api/users";
    import {servicesApi} from "$lib/api/services";
    import {currentUser} from "$lib/stores/auth";
    import type {BookingDTO} from "$lib/types";
    import {requireAuth} from "$lib/guards";
    import {get} from 'svelte/store';

    requireAuth();

    let bookings: BookingDTO[] = [];
    let specialistNames = new Map<number, string>();
    let serviceNames = new Map<number, string>();
    let loading = true;
    let loadError = '';
    let cancelError = '';

    onMount(async () => {
        const user = get(currentUser);
        if (!user) { loading = false; return; }
        try {
            bookings = await bookingsApi.getByClient(user.id);

            const uniqueSpecialistIds = [...new Set(bookings.map(b => b.specialistId))];
            const uniqueServiceIds = [...new Set(bookings.map(b => b.serviceId))];

            const [specialists, services] = await Promise.all([
                Promise.all(uniqueSpecialistIds.map(id => usersApi.getById(id))),
                Promise.all(uniqueServiceIds.map(id => servicesApi.getById(id).catch(() => null)))
            ]);

            specialistNames = new Map(specialists.map(u => [u.id, u.fullName]));
            serviceNames = new Map(services.filter(Boolean).map(s => [s!.id, s!.name]));
        } catch (e: any) {
            loadError = e.message;
        } finally {
            loading = false;
        }
    });

    async function cancel(id: number) {
        if (!confirm('Cancel this booking?')) return;
        cancelError = '';
        try {
            await bookingsApi.cancelAsClient(id);
            bookings = bookings.map(b => b.id === id ? {...b, status: 'CANCELLED'} : b);
        } catch (e: any) {
            cancelError = e.message;
        }
    }

    function formatTime(start: string, end: string): string {
        const [datePart, startTime] = start.split('T');
        const [year, month, day] = datePart.split('-');
        return `${day}.${month}.${year} ${startTime.substring(0, 5)} – ${end.split('T')[1].substring(0, 5)}`;
    }
</script>

<div class="flex items-center justify-between mb-6">
    <h1 class="text-2xl font-bold">My Bookings</h1>
    <a href="/bookings/new" class="bg-blue-600 text-white px-4 py-2 rounded text-sm">+ New Booking</a>
</div>

{#if cancelError}
    <p class="text-red-600 text-sm mb-4">{cancelError}</p>
{/if}

{#if loading}
    <p class="text-gray-500">Loading...</p>
{:else if loadError}
    <p class="text-red-600">{loadError}</p>
{:else if bookings.length === 0}
    <p class="text-gray-500">You have no bookings yet.</p>
{:else}
    <div class="flex flex-col gap-3">
        {#each bookings as booking}
            <div class="border rounded-lg p-4 flex justify-between items-center">
                <div class="flex flex-col gap-1">
                    <p class="font-medium">{serviceNames.get(booking.serviceId) ?? `Service #${booking.serviceId}`}</p>
                    <p class="text-sm text-gray-500">{specialistNames.get(booking.specialistId) ?? `Specialist #${booking.specialistId}`}</p>
                    <p class="text-sm text-gray-500">{formatTime(booking.start, booking.end)}</p>
                    <span class="text-xs font-semibold px-2 py-0.5 rounded w-fit"
                          class:bg-yellow-100={booking.status === 'PENDING'}
                          class:bg-green-100={booking.status === 'CONFIRMED'}
                          class:bg-red-100={booking.status === 'CANCELLED'}>
                        {booking.status}
                    </span>
                </div>
                {#if booking.status !== 'CANCELLED'}
                    <button on:click={() => cancel(booking.id)}
                            class="text-sm text-red-600 hover:underline shrink-0">
                        Cancel
                    </button>
                {/if}
            </div>
        {/each}
    </div>
{/if}
