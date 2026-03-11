<script lang="ts">
    import {onMount} from 'svelte';
    import {bookingsApi} from '$lib/api/bookings';
    import {scheduleApi} from '$lib/api/schedule';
    import {usersApi} from '$lib/api/users';
    import {currentUser} from '$lib/stores/auth';
    import type {BookingDTO, DayOfWeek} from '$lib/types';
    import {requireAuth} from '$lib/guards';
    import {get} from 'svelte/store';

    requireAuth();

    type Tab = 'schedule' | 'bookings';
    let activeTab: Tab = 'bookings';

    // ── Bookings ────────────────────────────────────────────────
    let bookings: BookingDTO[] = [];
    let clientNames = new Map<number, string>();
    let bookingsLoading = true;
    let cancelError = '';
    let confirmError = '';
    let filterStatus: 'all' | 'PENDING' | 'CONFIRMED' | 'CANCELLED' = 'all';

    // ── Schedule ─────────────────────────────────────────────────
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

    // LocalTime from Java comes as "HH:mm:ss" — strip seconds for <input type="time">
    function toTimeInput(t: string): string {
        return t.substring(0, 5);
    }

    onMount(async () => {
        const user = get(currentUser);
        if (!user) return;

        // Fetch bookings + client names
        try {
            bookings = await bookingsApi.getBySpecialist(user.id);
            const uniqueClientIds = [...new Set(bookings.map(b => b.clientId))];
            const users = await Promise.all(uniqueClientIds.map(id => usersApi.getById(id)));
            clientNames = new Map(users.map(u => [u.id, u.fullName]));
        } finally {
            bookingsLoading = false;
        }

        // Fetch schedule (may not exist yet — 404 is fine)
        try {
            const existing = await scheduleApi.getBySpecialist(user.id);
            for (const day of existing.workingDays) {
                schedule[day.dayOfWeek] = {
                    enabled: true,
                    start: toTimeInput(day.start),
                    end: toTimeInput(day.end)
                };
            }
        } catch {
            // No schedule yet — leave defaults
        } finally {
            scheduleLoading = false;
        }
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
                workingDays: DAYS
                    .filter(d => schedule[d].enabled)
                    .map(d => ({
                        dayOfWeek: d,
                        start: schedule[d].start + ':00',
                        end: schedule[d].end + ':00'
                    }))
            });
            scheduleSuccess = true;
        } catch (e: any) {
            scheduleError = e.message ?? 'Failed to save schedule.';
        } finally {
            scheduleSaving = false;
        }
    }

    async function cancelBooking(id: number) {
        if (!confirm('Cancel this booking?')) return;
        cancelError = '';
        try {
            await bookingsApi.cancelAsSpecialist(id);
            bookings = bookings.map(b => b.id === id ? {...b, status: 'CANCELLED'} : b);
        } catch (e: any) {
            cancelError = e.message ?? 'Failed to cancel booking.';
        }
    }

    async function confirmBooking(id: number) {
        confirmError = '';
        try {
            await bookingsApi.confirmBooking(id);
            bookings = bookings.map(b => b.id === id ? {...b, status: 'CONFIRMED'} : b);
        } catch (e: any) {
            confirmError = e.message ?? 'Failed to confirm booking.';
        }
    }

    function formatTime(start: string, end: string): string {
        const [datePart, startTime] = start.split('T');
        const [year, month, day] = datePart.split('-');
        const startHHMM = startTime.substring(0, 5);
        const endHHMM = end.split('T')[1].substring(0, 5);
        return `${day}.${month}.${year} ${startHHMM} - ${endHHMM}`;
    }

    $: filteredBookings = filterStatus === 'all'
        ? bookings
        : bookings.filter(b => b.status === filterStatus);
</script>

<!-- Tab bar -->
<div class="flex gap-1 mb-6 border-b">
    <button
        on:click={() => activeTab = 'bookings'}
        class="px-4 py-2 text-sm font-medium border-b-2 -mb-px"
        class:border-blue-600={activeTab === 'bookings'}
        class:text-blue-600={activeTab === 'bookings'}
        class:border-transparent={activeTab !== 'bookings'}
        class:text-gray-500={activeTab !== 'bookings'}
    >
        My Schedule
    </button>
    <button
        on:click={() => activeTab = 'schedule'}
        class="px-4 py-2 text-sm font-medium border-b-2 -mb-px"
        class:border-blue-600={activeTab === 'schedule'}
        class:text-blue-600={activeTab === 'schedule'}
        class:border-transparent={activeTab !== 'schedule'}
        class:text-gray-500={activeTab !== 'schedule'}
    >
        Working Hours
    </button>
</div>

<!-- ── Bookings tab ───────────────────────────────────────────── -->
{#if activeTab === 'bookings'}
    <div class="flex gap-3 mb-6">
        {#each ['all', 'PENDING', 'CONFIRMED', 'CANCELLED'] as status}
            <button
                on:click={() => filterStatus = status as typeof filterStatus}
                class="px-3 py-1.5 rounded text-sm font-medium border"
                class:bg-blue-600={filterStatus === status}
                class:text-white={filterStatus === status}
                class:text-gray-600={filterStatus !== status}
            >
                {status === 'all' ? 'All' : status.charAt(0) + status.slice(1).toLowerCase()}
            </button>
        {/each}
    </div>

    {#if cancelError}<p class="text-red-600 text-sm mb-4">{cancelError}</p>{/if}
    {#if confirmError}<p class="text-red-600 text-sm mb-4">{confirmError}</p>{/if}

    {#if bookingsLoading}
        <p class="text-gray-500">Loading...</p>
    {:else if filteredBookings.length === 0}
        <p class="text-gray-500">
            {filterStatus === 'all' ? 'You have no bookings yet.' : `No ${filterStatus.toLowerCase()} bookings.`}
        </p>
    {:else}
        <div class="border rounded-lg overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium">ID</th>
                        <th class="text-left px-4 py-3 font-medium">Client</th>
                        <th class="text-left px-4 py-3 font-medium">Date / Time</th>
                        <th class="text-left px-4 py-3 font-medium">Status</th>
                        <th class="text-left px-4 py-3 font-medium">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {#each filteredBookings as booking}
                        <tr class="border-b last:border-0">
                            <td class="px-4 py-3">{booking.id}</td>
                            <td class="px-4 py-3">{clientNames.get(booking.clientId) ?? `#${booking.clientId}`}</td>
                            <td class="px-4 py-3">{formatTime(booking.start, booking.end)}</td>
                            <td class="px-4 py-3">
                                <span class="text-xs font-semibold px-2 py-0.5 rounded"
                                      class:bg-yellow-100={booking.status === 'PENDING'}
                                      class:bg-green-100={booking.status === 'CONFIRMED'}
                                      class:bg-red-100={booking.status === 'CANCELLED'}>
                                    {booking.status}
                                </span>
                            </td>
                            <td class="px-4 py-3 flex gap-3">
                                {#if booking.status === 'PENDING'}
                                    <button on:click={() => confirmBooking(booking.id)}
                                            class="text-sm text-green-600 hover:underline">
                                        Confirm
                                    </button>
                                {/if}
                                {#if booking.status !== 'CANCELLED'}
                                    <button on:click={() => cancelBooking(booking.id)}
                                            class="text-sm text-red-600 hover:underline">
                                        Cancel
                                    </button>
                                {/if}
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}

<!-- ── Schedule tab ───────────────────────────────────────────── -->
{:else}
    {#if scheduleLoading}
        <p class="text-gray-500">Loading schedule...</p>
    {:else}
        <p class="text-sm text-gray-500 mb-6">Set the days and hours you are available for bookings.</p>

        <div class="flex flex-col gap-3 mb-6">
            {#each DAYS as day}
                <div class="flex items-center gap-4">
                    <label class="flex items-center gap-2 w-32">
                        <input type="checkbox" bind:checked={schedule[day].enabled} class="rounded" />
                        <span class="text-sm font-medium">{DAY_LABELS[day]}</span>
                    </label>

                    {#if schedule[day].enabled}
                        <div class="flex items-center gap-2 text-sm">
                            <input
                                type="time"
                                bind:value={schedule[day].start}
                                class="border rounded px-2 py-1 text-sm"
                            />
                            <span class="text-gray-400">to</span>
                            <input
                                type="time"
                                bind:value={schedule[day].end}
                                class="border rounded px-2 py-1 text-sm"
                            />
                        </div>
                    {:else}
                        <span class="text-sm text-gray-400">Day off</span>
                    {/if}
                </div>
            {/each}
        </div>

        {#if scheduleError}
            <p class="text-red-600 text-sm mb-3">{scheduleError}</p>
        {/if}
        {#if scheduleSuccess}
            <p class="text-green-600 text-sm mb-3">Schedule saved.</p>
        {/if}

        <button
            on:click={saveSchedule}
            disabled={scheduleSaving}
            class="bg-blue-600 text-white px-5 py-2 rounded text-sm disabled:opacity-50"
        >
            {scheduleSaving ? 'Saving...' : 'Save schedule'}
        </button>
    {/if}
{/if}
