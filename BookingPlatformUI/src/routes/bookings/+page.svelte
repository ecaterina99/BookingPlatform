<script lang="ts">
    import {onMount} from "svelte";
    import {bookingsApi} from "$lib/api/bookings";
    import {currentUser} from "$lib/stores/auth";
    import type {BookingDTO} from "$lib/types";
    import {requireAuth} from "$lib/guards";
    import {get} from 'svelte/store'

    requireAuth();

    let bookings: BookingDTO[] = [];
    let loading = true;

    onMount(async () => {
        const user = get(currentUser);
        if (!user) return;
        bookings = await bookingsApi.getByClient(user.id)
        loading = false;
    });

    let cancelError: string | null = null;

    async function cancel(id: number) {
        cancelError = null;
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
        const startHHMM = startTime.substring(0, 5);
        const endHHMM = end.split('T')[1].substring(0, 5);
        return `${day}.${month}.${year} ${startHHMM} - ${endHHMM}`;
    }

</script>

<h1 class="text-2xl font-bold mb-4">My Bookings</h1>
<a href="/bookings/new" class="bg-blue-600 text-white px-4 py-2 rounded mb-6 inline-block">
    New Booking
</a>

{#if loading}
    <p>Loading...</p>
{:else if bookings.length === 0}
    <p class="text-gray-800">You have no bookings yet.</p>
{:else}
    <div class="flex flex-col gap-3">
        {#each bookings as booking}
            <div class="border rounded-lg p-4 flex justify-between items-center">
                <div>
                    <p class="font-medium">Booking id: {booking.id}</p>
                    <p class="text-sm text-gray-500">{formatTime(booking.start, booking.end)}</p>
                    <p class="text-sm text-gray-500">Specialist id: {booking.specialistId}</p>
                    <span class="text-xs font-semibold px-2 py-0.5 rounded"
                          class:bg-yellow-100={booking.status === 'PENDING'}
                          class:bg-green-100={booking.status === 'CONFIRMED'}
                          class:bg-red-100={booking.status === 'CANCELLED'}>
              {booking.status}
            </span>
                </div>
                {#if booking.status !== 'CANCELLED'}
                    <button on:click={() => cancel(booking.id)}
                            class="text-sm text-red-600 hover:underline">
                        Cancel
                    </button>
                {/if}
                {#if cancelError}
                    <p class="text-red-600 text-sm mb-3">{cancelError}</p>
                {/if}
            </div>
        {/each}
    </div>
{/if}
