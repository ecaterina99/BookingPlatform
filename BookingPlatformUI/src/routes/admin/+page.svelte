<script lang="ts">
    import {onMount, getContext} from 'svelte';
    import type {Writable} from 'svelte/store';
    import {bookingsApi} from '$lib/api/bookings';
    import {usersApi} from '$lib/api/users';
    import type {OrganizationDTO, SpecialistDTO, ServiceDTO, BookingDTO} from '$lib/types';

    const {org, specialists, services} = getContext<{
        org: Writable<OrganizationDTO | null>;
        specialists: Writable<SpecialistDTO[]>;
        services: Writable<ServiceDTO[]>;
    }>('admin');

    function serviceName(id: number): string {
        return $services.find(s => s.id === id)?.name ?? `#${id}`;
    }

    let allBookings: BookingDTO[] = [];
    let clientNames = new Map<number, string>();
    let loading = true;

    onMount(async () => {
        try {
            const allResults = await Promise.all($specialists.map(s => bookingsApi.getBySpecialist(s.userId)));
            allBookings = allResults.flat().sort((a, b) => b.start.localeCompare(a.start));

            const uniqueClientIds = [...new Set(allBookings.map(b => b.clientId))];
            const users = await Promise.all(uniqueClientIds.map(id => usersApi.getById(id)));
            clientNames = new Map(users.map(u => [u.id, u.fullName]));
        } finally {
            loading = false;
        }
    });

    function formatTime(start: string, end: string): string {
        const [datePart, startTime] = start.split('T');
        const [year, month, day] = datePart.split('-');
        const startHHMM = startTime.substring(0, 5);
        const endHHMM = end.split('T')[1].substring(0, 5);
        return `${day}.${month}.${year} ${startHHMM} - ${endHHMM}`;
    }

    function specialistName(id: number): string {
        return $specialists.find(s => s.userId === id)?.fullName ?? `#${id}`;
    }

    $: recentBookings = allBookings.slice(0, 10);
</script>

{#if $org}
    <div class="mb-8">
        <h1 class="text-3xl font-bold mb-1">{$org.name}</h1>
        <p class="text-gray-500">{$org.city} · {$org.address}</p>
        <p class="text-gray-500">{$org.phone} · {$org.email}</p>
    </div>

    <div class="grid grid-cols-3 gap-4 mb-8">
        <div class="border rounded-lg p-4 text-center">
            <p class="text-3xl font-bold">{$services.length}</p>
            <p class="text-gray-500 text-sm mt-1">Services</p>
        </div>
        <div class="border rounded-lg p-4 text-center">
            <p class="text-3xl font-bold">{$specialists.length}</p>
            <p class="text-gray-500 text-sm mt-1">Specialists</p>
        </div>
        <div class="border rounded-lg p-4 text-center">
            <p class="text-3xl font-bold">{allBookings.length}</p>
            <p class="text-gray-500 text-sm mt-1">Total Bookings</p>
        </div>
    </div>

    <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold">Recent Bookings</h2>
        <a href="/admin/bookings" class="text-blue-600 text-sm hover:underline">View all bookings →</a>
    </div>

    {#if loading}
        <p class="text-gray-500">Loading bookings...</p>
    {:else if recentBookings.length === 0}
        <p class="text-gray-500">No bookings yet.</p>
    {:else}
        <div class="border rounded-lg overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium">ID</th>
                        <th class="text-left px-4 py-3 font-medium">Client</th>
                        <th class="text-left px-4 py-3 font-medium">Specialist</th>
                        <th class="text-left px-4 py-3 font-medium">Service</th>
                        <th class="text-left px-4 py-3 font-medium">Date / Time</th>
                        <th class="text-left px-4 py-3 font-medium">Status</th>
                    </tr>
                </thead>
                <tbody>
                    {#each recentBookings as booking}
                        <tr class="border-b last:border-0">
                            <td class="px-4 py-3">{booking.id}</td>
                            <td class="px-4 py-3">{clientNames.get(booking.clientId) ?? `#${booking.clientId}`}</td>
                            <td class="px-4 py-3">{specialistName(booking.specialistId)}</td>
                            <td class="px-4 py-3">{serviceName(booking.serviceId)}</td>
                            <td class="px-4 py-3">{formatTime(booking.start, booking.end)}</td>
                            <td class="px-4 py-3">
                                <span class="text-xs font-semibold px-2 py-0.5 rounded"
                                      class:bg-yellow-100={booking.status === 'PENDING'}
                                      class:bg-green-100={booking.status === 'CONFIRMED'}
                                      class:bg-red-100={booking.status === 'CANCELLED'}>
                                    {booking.status}
                                </span>
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}
{/if}
