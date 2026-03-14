<script lang="ts">
    import {onMount} from "svelte";
    import {bookingsApi} from "$lib/api/bookings";
    import {usersApi} from "$lib/api/users";
    import {servicesApi} from "$lib/api/services";
    import {currentUser} from "$lib/stores/auth";
    import type {BookingDTO} from "$lib/types";
    import {requireAuth} from "$lib/guards";
    import {get} from 'svelte/store';
    import {CalendarDays, Clock, User, X, Plus} from 'lucide-svelte';
    import ConfirmModal from '$lib/components/ConfirmModal.svelte';

    let modalOpen = false;
    let modalAction: (() => void) | null = null;

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

    function cancel(id: number) {
        modalAction = async () => {
            cancelError = '';
            try {
                await bookingsApi.cancelAsClient(id);
                bookings = bookings.map(b => b.id === id ? {...b, status: 'CANCELLED'} : b);
            } catch (e: any) {
                cancelError = e.message;
            }
        };
        modalOpen = true;
    }

    function formatTime(start: string, end: string): string {
        const [datePart, startTime] = start.split('T');
        const [year, month, day] = datePart.split('-');
        return `${day}.${month}.${year} ${startTime.substring(0, 5)} – ${end.split('T')[1].substring(0, 5)}`;
    }

    function statusColor(status: string): string {
        if (status === 'PENDING') return 'bg-amber-50 text-amber-700 border-amber-200';
        if (status === 'CONFIRMED') return 'bg-green-50 text-green-700 border-green-200';
        return 'bg-red-50 text-red-600 border-red-200';
    }
</script>

<ConfirmModal bind:open={modalOpen} title="Cancel Booking" message="Are you sure you want to cancel this booking?" confirmText="Cancel Booking" variant="danger" on:confirm={() => modalAction?.()} />

<div class="flex items-center justify-between mb-8">
    <h1 class="text-2xl font-serif font-semibold text-brand-800">My Bookings</h1>
    <a href="/bookings/new"
       class="flex items-center gap-2 bg-brand-800 text-brand-100 px-4 py-2.5 rounded-lg text-sm font-medium hover:bg-brand-700 transition-colors">
        <Plus size={16} /> New Booking
    </a>
</div>

{#if cancelError}
    <div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-5">{cancelError}</div>
{/if}

{#if loading}
    <p class="text-brand-400">Loading...</p>
{:else if loadError}
    <div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">{loadError}</div>
{:else if bookings.length === 0}
    <div class="text-center py-16">
        <CalendarDays size={48} class="mx-auto text-brand-300 mb-4" />
        <p class="text-brand-500 mb-4">You have no bookings yet.</p>
        <a href="/bookings/new" class="text-sm font-medium text-gold-500 hover:text-gold-600">Book your first service</a>
    </div>
{:else}
    <div class="flex flex-col gap-3">
        {#each bookings as booking}
            <div class="bg-white border border-brand-200 rounded-xl p-5 flex justify-between items-center hover:border-brand-300 transition-colors">
                <div class="flex flex-col gap-1.5">
                    <p class="font-semibold text-brand-800">{serviceNames.get(booking.serviceId) ?? `Service #${booking.serviceId}`}</p>
                    <div class="flex items-center gap-4 text-sm text-brand-500">
                        <span class="flex items-center gap-1.5"><User size={14} /> {specialistNames.get(booking.specialistId) ?? `Specialist #${booking.specialistId}`}</span>
                        <span class="flex items-center gap-1.5"><Clock size={14} /> {formatTime(booking.start, booking.end)}</span>
                    </div>
                    <span class="text-xs font-medium px-2.5 py-1 rounded-full border w-fit mt-1 {statusColor(booking.status)}">
                        {booking.status}
                    </span>
                </div>
                {#if booking.status !== 'CANCELLED'}
                    <button on:click={() => cancel(booking.id)}
                            class="flex items-center gap-1.5 text-sm text-red-500 hover:text-red-600 hover:bg-red-50 px-3 py-2 rounded-lg transition-colors shrink-0">
                        <X size={14} /> Cancel
                    </button>
                {/if}
            </div>
        {/each}
    </div>
{/if}
