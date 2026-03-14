<script lang="ts">
    import {onMount} from 'svelte';
    import {bookingsApi} from '$lib/api/bookings';
    import {scheduleApi} from '$lib/api/schedule';
    import {usersApi} from '$lib/api/users';
    import {currentUser} from '$lib/stores/auth';
    import type {BookingDTO, DayOfWeek} from '$lib/types';
    import {requireAuth} from '$lib/guards';
    import {get} from 'svelte/store';
    import {CalendarDays, Clock, Check, X, Save} from 'lucide-svelte';
    import ConfirmModal from '$lib/components/ConfirmModal.svelte';

    let modalOpen = false;
    let modalAction: (() => void) | null = null;

    requireAuth();

    type Tab = 'schedule' | 'bookings';
    let activeTab: Tab = 'bookings';

    let bookings: BookingDTO[] = [];
    let clientNames = new Map<number, string>();
    let bookingsLoading = true;
    let cancelError = '';
    let confirmError = '';
    let filterStatus: 'all' | 'PENDING' | 'CONFIRMED' | 'CANCELLED' = 'all';

    const DAYS: DayOfWeek[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
    const DAY_LABELS: Record<DayOfWeek, string> = {
        MONDAY: 'Monday', TUESDAY: 'Tuesday', WEDNESDAY: 'Wednesday',
        THURSDAY: 'Thursday', FRIDAY: 'Friday', SATURDAY: 'Saturday', SUNDAY: 'Sunday'
    };

    type DayEntry = { enabled: boolean; start: string; end: string };
    let schedule: Record<DayOfWeek, DayEntry> = Object.fromEntries(
        DAYS.map(d => [d, {enabled: false, start: '09:00', end: '17:00'}])
    ) as Record<DayOfWeek, DayEntry>;

    let scheduleLoading = true;
    let scheduleSaving = false;
    let scheduleError = '';
    let scheduleSuccess = false;

    function toTimeInput(t: string): string { return t.substring(0, 5); }

    onMount(async () => {
        const user = get(currentUser);
        if (!user) return;
        try {
            bookings = await bookingsApi.getBySpecialist(user.id);
            const uniqueClientIds = [...new Set(bookings.map(b => b.clientId))];
            const users = await Promise.all(uniqueClientIds.map(id => usersApi.getById(id)));
            clientNames = new Map(users.map(u => [u.id, u.fullName]));
        } finally { bookingsLoading = false; }

        try {
            const existing = await scheduleApi.getBySpecialist(user.id);
            for (const day of existing.workingDays) {
                schedule[day.dayOfWeek] = { enabled: true, start: toTimeInput(day.start), end: toTimeInput(day.end) };
            }
        } catch {} finally { scheduleLoading = false; }
    });

    async function saveSchedule() {
        const user = get(currentUser);
        if (!user) return;
        scheduleSaving = true;
        scheduleError = '';
        scheduleSuccess = false;
        try {
            await scheduleApi.save({
                specialistId: user.id,
                workingDays: DAYS.filter(d => schedule[d].enabled)
                    .map(d => ({ dayOfWeek: d, start: schedule[d].start + ':00', end: schedule[d].end + ':00' }))
            });
            scheduleSuccess = true;
        } catch (e: any) { scheduleError = e.message ?? 'Failed to save schedule.'; }
        finally { scheduleSaving = false; }
    }

    function cancelBooking(id: number) {
        modalAction = async () => {
            cancelError = '';
            try {
                await bookingsApi.cancelAsSpecialist(id);
                bookings = bookings.map(b => b.id === id ? {...b, status: 'CANCELLED'} : b);
            } catch (e: any) { cancelError = e.message ?? 'Failed to cancel booking.'; }
        };
        modalOpen = true;
    }

    async function confirmBooking(id: number) {
        confirmError = '';
        try {
            await bookingsApi.confirmBooking(id);
            bookings = bookings.map(b => b.id === id ? {...b, status: 'CONFIRMED'} : b);
        } catch (e: any) { confirmError = e.message ?? 'Failed to confirm booking.'; }
    }

    function formatTime(start: string, end: string): string {
        const [datePart, startTime] = start.split('T');
        const [year, month, day] = datePart.split('-');
        return `${day}.${month}.${year} ${startTime.substring(0, 5)} - ${end.split('T')[1].substring(0, 5)}`;
    }

    function statusColor(status: string): string {
        if (status === 'PENDING') return 'bg-amber-50 text-amber-700 border-amber-200';
        if (status === 'CONFIRMED') return 'bg-green-50 text-green-700 border-green-200';
        return 'bg-red-50 text-red-600 border-red-200';
    }

    $: filteredBookings = filterStatus === 'all' ? bookings : bookings.filter(b => b.status === filterStatus);
</script>

<ConfirmModal bind:open={modalOpen} title="Cancel Booking" message="Are you sure you want to cancel this booking?" confirmText="Cancel Booking" variant="danger" on:confirm={() => modalAction?.()} />

<!-- Tab bar -->
<div class="flex gap-1 mb-8 border-b border-brand-200">
    <button on:click={() => activeTab = 'bookings'}
        class="flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 -mb-px transition-colors
            {activeTab === 'bookings' ? 'border-gold-500 text-brand-800' : 'border-transparent text-brand-400 hover:text-brand-600'}">
        <CalendarDays size={16} /> My Schedule
    </button>
    <button on:click={() => activeTab = 'schedule'}
        class="flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 -mb-px transition-colors
            {activeTab === 'schedule' ? 'border-gold-500 text-brand-800' : 'border-transparent text-brand-400 hover:text-brand-600'}">
        <Clock size={16} /> Working Hours
    </button>
</div>

{#if activeTab === 'bookings'}
    <!-- Status filter -->
    <div class="flex gap-2 mb-6">
        {#each ['all', 'PENDING', 'CONFIRMED', 'CANCELLED'] as status}
            <button on:click={() => filterStatus = status as typeof filterStatus}
                class="px-4 py-2 rounded-lg text-sm font-medium border transition-all
                    {filterStatus === status
                        ? 'bg-brand-800 text-white border-brand-800'
                        : 'bg-white text-brand-600 border-brand-200 hover:border-brand-400'}">
                {status === 'all' ? 'All' : status.charAt(0) + status.slice(1).toLowerCase()}
            </button>
        {/each}
    </div>

    {#if cancelError}<div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">{cancelError}</div>{/if}
    {#if confirmError}<div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">{confirmError}</div>{/if}

    {#if bookingsLoading}
        <p class="text-brand-400">Loading...</p>
    {:else if filteredBookings.length === 0}
        <p class="text-brand-500">{filterStatus === 'all' ? 'You have no bookings yet.' : `No ${filterStatus.toLowerCase()} bookings.`}</p>
    {:else}
        <div class="bg-white border border-brand-200 rounded-xl overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-brand-50 border-b border-brand-200">
                    <tr>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">ID</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Client</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Date / Time</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Status</th>
                        <th class="text-left px-5 py-3.5 font-medium text-brand-600">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {#each filteredBookings as booking}
                        <tr class="border-b border-brand-100 last:border-0 hover:bg-brand-50/50 transition-colors">
                            <td class="px-5 py-3.5 text-brand-700">{booking.id}</td>
                            <td class="px-5 py-3.5 text-brand-700">{clientNames.get(booking.clientId) ?? `#${booking.clientId}`}</td>
                            <td class="px-5 py-3.5 text-brand-500">{formatTime(booking.start, booking.end)}</td>
                            <td class="px-5 py-3.5">
                                <span class="text-xs font-medium px-2.5 py-1 rounded-full border {statusColor(booking.status)}">{booking.status}</span>
                            </td>
                            <td class="px-5 py-3.5 flex gap-2">
                                {#if booking.status === 'PENDING'}
                                    <button on:click={() => confirmBooking(booking.id)}
                                            class="flex items-center gap-1 text-sm text-green-600 hover:bg-green-50 px-2.5 py-1.5 rounded-lg transition-colors">
                                        <Check size={14} /> Confirm
                                    </button>
                                {/if}
                                {#if booking.status !== 'CANCELLED'}
                                    <button on:click={() => cancelBooking(booking.id)}
                                            class="flex items-center gap-1 text-sm text-red-500 hover:bg-red-50 px-2.5 py-1.5 rounded-lg transition-colors">
                                        <X size={14} /> Cancel
                                    </button>
                                {/if}
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}

{:else}
    {#if scheduleLoading}
        <p class="text-brand-400">Loading schedule...</p>
    {:else}
        <p class="text-sm text-brand-500 mb-6">Set the days and hours you are available for bookings.</p>

        <div class="flex flex-col gap-3 mb-6">
            {#each DAYS as day}
                <div class="flex items-center gap-4 bg-white border border-brand-200 rounded-lg px-4 py-3">
                    <label class="flex items-center gap-3 w-36">
                        <input type="checkbox" bind:checked={schedule[day].enabled}
                               class="rounded border-brand-300 text-gold-500 focus:ring-gold-400" />
                        <span class="text-sm font-medium text-brand-700">{DAY_LABELS[day]}</span>
                    </label>
                    {#if schedule[day].enabled}
                        <div class="flex items-center gap-2 text-sm">
                            <input type="time" bind:value={schedule[day].start}
                                   class="border border-brand-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:border-gold-400" />
                            <span class="text-brand-400">to</span>
                            <input type="time" bind:value={schedule[day].end}
                                   class="border border-brand-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:border-gold-400" />
                        </div>
                    {:else}
                        <span class="text-sm text-brand-300">Day off</span>
                    {/if}
                </div>
            {/each}
        </div>

        {#if scheduleError}<div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">{scheduleError}</div>{/if}
        {#if scheduleSuccess}<div class="flex items-center gap-2 bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg px-4 py-3 mb-4"><Check size={16} /> Schedule saved.</div>{/if}

        <button on:click={saveSchedule} disabled={scheduleSaving}
                class="flex items-center gap-2 bg-brand-800 text-brand-100 px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-brand-700 disabled:opacity-50 transition-colors">
            <Save size={16} /> {scheduleSaving ? 'Saving...' : 'Save schedule'}
        </button>
    {/if}
{/if}
