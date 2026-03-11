<script lang="ts">
    import {onMount} from 'svelte';
    import {goto} from '$app/navigation';
    import {servicesApi} from '$lib/api/services';
    import {bookingsApi} from '$lib/api/bookings';
    import {organizationsApi} from '$lib/api/organizations';
    import {scheduleApi} from '$lib/api/schedule';
    import {currentUser} from '$lib/stores/auth';
    import {requireAuth} from '$lib/guards';
    import {ApiError} from '$lib/api/client';
    import {get} from 'svelte/store';
    import type {ServiceDTO, SpecialistDTO, ScheduleDTO, DayOfWeek} from '$lib/types';

    requireAuth();

    // ── Services list ─────────────────────────────────────────────
    let services: ServiceDTO[] = [];
    let orgNames = new Map<number, string>();
    let loading = true;
    let selectedService: ServiceDTO | null = null;
    let specialists: SpecialistDTO[] = [];

    // ── Booking form ──────────────────────────────────────────────
    let specialistId: number | null = null;
    let schedule: ScheduleDTO | null = null;
    let scheduleLoading = false;
    let submitting = false;
    let error = '';

    // ── Calendar state ────────────────────────────────────────────
    let weekOffset = 0;
    let selectedDate = '';   // 'YYYY-MM-DD'
    let selectedTime = '';   // 'HH:mm'

    const SHORT_DAY = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

    const JS_DAY: Record<DayOfWeek, number> = {
        SUNDAY: 0, MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3,
        THURSDAY: 4, FRIDAY: 5, SATURDAY: 6
    };

    const todayStr = new Date().toISOString().substring(0, 10);

    // Returns the 7 dates (Mon–Sun) for the given week offset
    function getWeekDays(offset: number): Date[] {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dow = today.getDay();
        const monday = new Date(today);
        monday.setDate(today.getDate() - (dow === 0 ? 6 : dow - 1) + offset * 7);
        return Array.from({length: 7}, (_, i) => {
            const d = new Date(monday);
            d.setDate(monday.getDate() + i);
            return d;
        });
    }

    function toDateStr(d: Date): string {
        return d.toISOString().substring(0, 10);
    }

    function weekLabel(days: Date[]): string {
        const fmt = (d: Date) => d.toLocaleDateString('en-GB', {day: 'numeric', month: 'short'});
        return `${fmt(days[0])} – ${fmt(days[6])}`;
    }

    function toHHMM(t: string): string {
        return t.substring(0, 5);
    }

    function workingHoursForDate(s: ScheduleDTO, dateStr: string): {start: string; end: string} | null {
        const jsDay = new Date(dateStr + 'T00:00:00').getDay();
        const match = Object.entries(JS_DAY).find(([, v]) => v === jsDay);
        if (!match) return null;
        const day = s.workingDays.find(d => d.dayOfWeek === match[0] as DayOfWeek);
        return day ? {start: toHHMM(day.start), end: toHHMM(day.end)} : null;
    }

    function generateSlots(start: string, end: string, durationMinutes: number): string[] {
        const toMins = (t: string) => { const [h, m] = t.split(':').map(Number); return h * 60 + m; };
        const startMins = toMins(start);
        const endMins = toMins(end);
        const slots: string[] = [];
        for (let t = startMins; t + durationMinutes <= endMins; t += durationMinutes) {
            slots.push(`${String(Math.floor(t / 60)).padStart(2, '0')}:${String(t % 60).padStart(2, '0')}`);
        }
        return slots;
    }

    $: weekDays = getWeekDays(weekOffset);
    $: selectedHours = schedule && selectedDate ? workingHoursForDate(schedule, selectedDate) : null;
    $: timeSlots = selectedHours && selectedService
        ? generateSlots(selectedHours.start, selectedHours.end, selectedService.durationMinutes)
        : [];
    $: startDateTime = selectedDate && selectedTime ? `${selectedDate}T${selectedTime}` : '';

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
        specialistId = null;
        schedule = null;
        selectedDate = '';
        selectedTime = '';
        specialists = await organizationsApi.getSpecialists(service.organizationId);
        error = '';
    }

    async function onSpecialistChange() {
        schedule = null;
        selectedDate = '';
        selectedTime = '';
        weekOffset = 0;
        if (!specialistId) return;
        scheduleLoading = true;
        try {
            schedule = await scheduleApi.getBySpecialist(specialistId);
        } catch {
            schedule = null;
        } finally {
            scheduleLoading = false;
        }
    }

    function selectDay(dateStr: string) {
        if (dateStr < todayStr) return;
        selectedDate = dateStr;
        selectedTime = '';
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
                specialistId,
                serviceId: selectedService.id,
                start: startDateTime
            });
            goto('/bookings');
        } catch (e) {
            if (e instanceof ApiError) error = e.message;
            else error = 'Something went wrong.';
        } finally {
            submitting = false;
        }
    }
</script>

<h1 class="text-3xl font-bold mb-6">Book a service</h1>

{#if loading}
    <p>Loading...</p>
{:else if selectedService}
    <div class="max-w-lg">
        <button on:click={() => { selectedService = null; schedule = null; selectedDate = ''; selectedTime = ''; }}
                class="text-sm text-gray-500 hover:underline mb-4">← Back to services</button>

        <h2 class="text-xl font-semibold mb-1">{selectedService.name}</h2>
        <p class="text-sm text-gray-500 mb-4">{selectedService.durationMinutes} min · {selectedService.price} RON</p>

        {#if error}<p class="text-red-600 text-sm mb-4">{error}</p>{/if}

        <!-- Specialist selector -->
        <div class="flex flex-col gap-1 mb-6">
            <label class="text-sm font-medium">Specialist</label>
            <select bind:value={specialistId} on:change={onSpecialistChange} class="border rounded px-3 py-2">
                <option value={null} disabled>Select a specialist</option>
                {#each specialists as s}
                    <option value={s.userId}>{s.fullName}</option>
                {/each}
            </select>
        </div>

        <!-- Calendar (only shown once specialist is chosen) -->
        {#if specialistId}
            {#if scheduleLoading}
                <p class="text-gray-400 text-sm">Loading schedule...</p>
            {:else}
                <!-- Week navigation -->
                <div class="flex items-center justify-between mb-3">
                    <button
                        on:click={() => weekOffset--}
                        disabled={weekOffset === 0}
                        class="text-sm text-blue-600 disabled:text-gray-300 disabled:cursor-default"
                    >← Prev</button>
                    <span class="text-sm font-medium">{weekLabel(weekDays)}</span>
                    <button on:click={() => weekOffset++} class="text-sm text-blue-600">Next →</button>
                </div>

                <!-- Day columns -->
                <div class="grid grid-cols-7 gap-1 mb-4">
                    {#each weekDays as day, i}
                        {@const dateStr = toDateStr(day)}
                        {@const isPast = dateStr < todayStr}
                        {@const isToday = dateStr === todayStr}
                        {@const isSelected = dateStr === selectedDate}
                        {@const isWorking = !!schedule && !!workingHoursForDate(schedule, dateStr)}
                        <button
                            type="button"
                            on:click={() => selectDay(dateStr)}
                            disabled={isPast}
                            class="flex flex-col items-center py-2 rounded-lg text-sm transition"
                            class:opacity-30={isPast}
                            class:cursor-default={isPast}
                            class:text-gray-400={!isWorking && !isPast}
                            class:text-gray-800={isWorking && !isSelected}
                            class:bg-blue-600={isSelected}
                            class:text-white={isSelected}
                            class:hover:bg-gray-100={!isSelected && !isPast}
                        >
                            <span class="text-xs mb-1">{SHORT_DAY[i]}</span>
                            <span class="w-8 h-8 flex items-center justify-center rounded-full font-semibold text-sm"
                                  class:border-2={isToday && !isSelected}
                                  class:border-blue-600={isToday && !isSelected}
                                  class:text-blue-600={isToday && !isSelected}
                            >{day.getDate()}</span>
                        </button>
                    {/each}
                </div>

                <!-- Slots or unavailable message -->
                {#if selectedDate}
                    {#if !schedule || schedule.workingDays.length === 0}
                        <p class="text-sm text-amber-600 bg-amber-50 border border-amber-200 rounded px-3 py-2">
                            This specialist has no schedule set.
                        </p>
                    {:else if !selectedHours}
                        <p class="text-sm text-gray-500 bg-gray-50 border rounded px-3 py-2">
                            Specialist not available on this day.
                        </p>
                    {:else if timeSlots.length === 0}
                        <p class="text-sm text-gray-500 bg-gray-50 border rounded px-3 py-2">
                            No available slots for this day.
                        </p>
                    {:else}
                        <p class="text-sm font-medium mb-2">
                            Available slots ·
                            <span class="text-blue-600">
                                {new Date(selectedDate + 'T00:00:00').toLocaleDateString('en-GB', {day: 'numeric', month: 'short'})}
                            </span>
                        </p>
                        <div class="flex flex-col border rounded-lg overflow-hidden mb-4">
                            {#each timeSlots as slot}
                                <button
                                    type="button"
                                    on:click={() => selectedTime = slot}
                                    class="flex items-center justify-between px-4 py-3 text-sm border-b last:border-0 transition"
                                    class:bg-blue-600={selectedTime === slot}
                                    class:text-white={selectedTime === slot}
                                    class:hover:bg-gray-50={selectedTime !== slot}
                                >
                                    <span class="font-semibold">{slot}</span>
                                    <span class:text-blue-200={selectedTime === slot} class:text-gray-400={selectedTime !== slot}>
                                        {selectedService.durationMinutes} min →
                                    </span>
                                </button>
                            {/each}
                        </div>
                    {/if}
                {/if}

                {#if startDateTime}
                    <button on:click={create} disabled={submitting}
                            class="w-full bg-blue-600 text-white rounded-lg px-4 py-3 font-medium disabled:opacity-50">
                        {submitting ? 'Booking...' : 'Confirm booking'}
                    </button>
                {/if}
            {/if}
        {/if}
    </div>
{:else}
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        {#each services as serv}
            <div class="border rounded-lg p-4 hover:shadow transition">
                <h2 class="font-semibold text-lg">{serv.name}</h2>
                <p class="text-gray-500 text-sm">{serv.description}</p>
                <p class="text-gray-500 text-sm">{orgNames.get(serv.organizationId) ?? 'Unknown'}</p>
                <p class="text-gray-500 text-sm">Duration: {serv.durationMinutes} min · Price: {serv.price}</p>
                <button on:click={() => selectService(serv)}
                        class="mt-3 bg-blue-600 text-white text-sm px-3 py-1 rounded">
                    Book
                </button>
            </div>
        {/each}
    </div>
{/if}
