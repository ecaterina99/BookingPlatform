<script lang="ts">
    import {onMount, getContext} from 'svelte';
    import type {Writable} from 'svelte/store';
    import {bookingsApi} from '$lib/api/bookings';
    import {usersApi} from '$lib/api/users';
    import type {OrganizationDTO, SpecialistDTO, ServiceDTO, BookingDTO} from '$lib/types';
    import {MapPin, Phone, Mail, Scissors, Users, CalendarDays, ArrowRight} from 'lucide-svelte';

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
        } finally { loading = false; }
    });

    function formatTime(start: string, end: string): string {
        const [datePart, startTime] = start.split('T');
        const [year, month, day] = datePart.split('-');
        return `${day}.${month}.${year} ${startTime.substring(0, 5)} - ${end.split('T')[1].substring(0, 5)}`;
    }

    function specialistName(id: number): string {
        return $specialists.find(s => s.userId === id)?.fullName ?? `#${id}`;
    }

    function statusColor(status: string): string {
        if (status === 'PENDING') return 'bg-amber-50 text-amber-700 border-amber-200';
        if (status === 'CONFIRMED') return 'bg-green-50 text-green-700 border-green-200';
        return 'bg-red-50 text-red-600 border-red-200';
    }

    $: recentBookings = allBookings.slice(0, 10);
</script>

{#if $org}
    <div class="mb-8">
        <h1 class="text-2xl font-serif font-semibold text-brand-800 mb-2">{$org.name}</h1>
        <div class="flex flex-wrap gap-4 text-sm text-brand-500">
            <span class="flex items-center gap-1.5"><MapPin size={14} /> {$org.city} &middot; {$org.address}</span>
            <span class="flex items-center gap-1.5"><Phone size={14} /> {$org.phone}</span>
            <span class="flex items-center gap-1.5"><Mail size={14} /> {$org.email}</span>
        </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-3 gap-4 mb-10">
        <div class="bg-white border border-brand-200 rounded-xl p-5 text-center">
            <div class="w-10 h-10 rounded-lg bg-brand-100 flex items-center justify-center mx-auto mb-3">
                <Scissors size={20} class="text-brand-600" />
            </div>
            <p class="text-2xl font-bold text-brand-800">{$services.length}</p>
            <p class="text-brand-500 text-sm mt-0.5">Services</p>
        </div>
        <div class="bg-white border border-brand-200 rounded-xl p-5 text-center">
            <div class="w-10 h-10 rounded-lg bg-brand-100 flex items-center justify-center mx-auto mb-3">
                <Users size={20} class="text-brand-600" />
            </div>
            <p class="text-2xl font-bold text-brand-800">{$specialists.length}</p>
            <p class="text-brand-500 text-sm mt-0.5">Specialists</p>
        </div>
        <div class="bg-white border border-brand-200 rounded-xl p-5 text-center">
            <div class="w-10 h-10 rounded-lg bg-brand-100 flex items-center justify-center mx-auto mb-3">
                <CalendarDays size={20} class="text-brand-600" />
            </div>
            <p class="text-2xl font-bold text-brand-800">{allBookings.length}</p>
            <p class="text-brand-500 text-sm mt-0.5">Total Bookings</p>
        </div>
    </div>

    <div class="flex items-center justify-between mb-5">
        <h2 class="text-lg font-serif font-semibold text-brand-800">Recent Bookings</h2>
        <a href="/admin/bookings" class="flex items-center gap-1 text-sm font-medium text-gold-500 hover:text-gold-600 transition-colors">
            View all <ArrowRight size={14} />
        </a>
    </div>

    {#if loading}
        <p class="text-brand-400">Loading bookings...</p>
    {:else if recentBookings.length === 0}
        <p class="text-brand-500">No bookings yet.</p>
    {:else}
        <div class="bg-white border border-brand-200 rounded-xl overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-brand-50 border-b border-brand-200">
                    <tr>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">ID</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Client</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Specialist</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Service</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Date / Time</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Status</th>
                    </tr>
                </thead>
                <tbody>
                    {#each recentBookings as booking}
                        <tr class="border-b border-brand-100 last:border-0 hover:bg-brand-50/50 transition-colors">
                            <td class="px-5 py-3.5 text-brand-700">{booking.id}</td>
                            <td class="px-5 py-3.5 text-brand-700">{clientNames.get(booking.clientId) ?? `#${booking.clientId}`}</td>
                            <td class="px-5 py-3.5 text-brand-700">{specialistName(booking.specialistId)}</td>
                            <td class="px-5 py-3.5 text-brand-700">{serviceName(booking.serviceId)}</td>
                            <td class="px-5 py-3.5 text-brand-500">{formatTime(booking.start, booking.end)}</td>
                            <td class="px-5 py-3.5">
                                <span class="text-xs font-medium px-2.5 py-1 rounded-full border {statusColor(booking.status)}">{booking.status}</span>
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}
{/if}
