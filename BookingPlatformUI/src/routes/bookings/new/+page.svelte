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
    import type {ServiceDTO, SpecialistDTO, ScheduleDTO, DayOfWeek, ServiceCategoryType, OrganizationDTO} from '$lib/types';
    import {ArrowLeft, ChevronLeft, ChevronRight, Clock, Sparkles, Check, MapPin} from 'lucide-svelte';

    requireAuth();

    const CATEGORIES: { value: ServiceCategoryType | 'ALL'; label: string }[] = [
        { value: 'ALL', label: 'All' },
        { value: 'MAKEUP', label: 'Makeup' },
        { value: 'NAILS', label: 'Nails' },
        { value: 'BARBER', label: 'Barber' },
        { value: 'MASSAGE', label: 'Massage' },
        { value: 'TATTOO', label: 'Tattoo' },
        { value: 'HAIR', label: 'Hair' },
        { value: 'HEALTH_AND_FITNESS', label: 'Health & Fitness' },
        { value: 'SKIN_CARE', label: 'Skin Care' },
        { value: 'OTHER', label: 'Other' },
    ];

    let allServices: ServiceDTO[] = [];
    let allOrgs: OrganizationDTO[] = [];
    let orgNames = new Map<number, string>();
    let orgCities = new Map<number, string>();
    let loading = true;
    let selectedService: ServiceDTO | null = null;
    let activeCategory: ServiceCategoryType | 'ALL' = 'ALL';
    let activeCity: string = 'ALL';
    let specialists: SpecialistDTO[] = [];

    let specialistId: number | null = null;
    let schedule: ScheduleDTO | null = null;
    let scheduleLoading = false;
    let submitting = false;
    let error = '';

    let weekOffset = 0;
    let selectedDate = '';
    let selectedTime = '';

    const SHORT_DAY = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

    const JS_DAY: Record<DayOfWeek, number> = {
        SUNDAY: 0, MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3,
        THURSDAY: 4, FRIDAY: 5, SATURDAY: 6
    };

    const todayStr = new Date().toISOString().substring(0, 10);

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

    function toDateStr(d: Date): string { return d.toISOString().substring(0, 10); }

    function weekLabel(days: Date[]): string {
        const fmt = (d: Date) => d.toLocaleDateString('en-GB', {day: 'numeric', month: 'short'});
        return `${fmt(days[0])} – ${fmt(days[6])}`;
    }

    function toHHMM(t: string): string { return t.substring(0, 5); }

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

    $: cities = [...new Set(allOrgs.map(o => o.city))].sort();

    $: services = allServices.filter(s => {
        const matchesCategory = activeCategory === 'ALL' || s.category === activeCategory;
        const matchesCity = activeCity === 'ALL' || orgCities.get(s.organizationId) === activeCity;
        return matchesCategory && matchesCity;
    });

    onMount(async () => {
        const [fetchedServices, fetchedOrgs] = await Promise.all([
            servicesApi.getAll(),
            organizationsApi.getAll()
        ]);
        allServices = fetchedServices;
        allOrgs = fetchedOrgs;
        orgNames = new Map(fetchedOrgs.map(o => [o.id, o.name]));
        orgCities = new Map(fetchedOrgs.map(o => [o.id, o.city]));
        loading = false;
    });

    async function selectService(service: ServiceDTO) {
        selectedService = service;
        specialistId = null;
        schedule = null;
        selectedDate = '';
        selectedTime = '';
        const allSpecialists = await organizationsApi.getSpecialists(service.organizationId);
        const user = get(currentUser);
        specialists = user ? allSpecialists.filter(s => s.userId !== user.id) : allSpecialists;
        error = '';
    }

    async function onSpecialistChange() {
        schedule = null;
        selectedDate = '';
        selectedTime = '';
        weekOffset = 0;
        if (!specialistId) return;
        scheduleLoading = true;
        try { schedule = await scheduleApi.getBySpecialist(specialistId); }
        catch { schedule = null; }
        finally { scheduleLoading = false; }
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

{#if loading}
    <h1 class="text-2xl font-serif font-semibold text-brand-800 mb-8">Book a Service</h1>
    <p class="text-brand-400">Loading services...</p>
{:else if selectedService}
    <div class="max-w-lg">
        <button on:click={() => { selectedService = null; schedule = null; selectedDate = ''; selectedTime = ''; }}
                class="flex items-center gap-1.5 text-sm text-brand-500 hover:text-brand-700 mb-6 transition-colors">
            <ArrowLeft size={16} /> Back to services
        </button>

        <div class="bg-white border border-brand-200 rounded-xl p-6 mb-6">
            <h2 class="text-lg font-serif font-semibold text-brand-800">{selectedService.name}</h2>
            <p class="text-sm text-brand-500 mt-1 flex items-center gap-3">
                <span class="flex items-center gap-1"><Clock size={14} /> {selectedService.durationMinutes} min</span>
                <span>{selectedService.price} RON</span>
            </p>
        </div>

        {#if error}
            <div class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-5">{error}</div>
        {/if}

        <!-- Specialist selector -->
        <div class="flex flex-col gap-1.5 mb-6">
            <label class="text-sm font-medium text-brand-700">Specialist</label>
            <select bind:value={specialistId} on:change={onSpecialistChange}
                    class="border border-brand-200 rounded-lg px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition">
                <option value={null} disabled>Select a specialist</option>
                {#each specialists as s}
                    <option value={s.userId}>{s.fullName}</option>
                {/each}
            </select>
        </div>

        {#if specialistId}
            {#if scheduleLoading}
                <p class="text-brand-400 text-sm">Loading schedule...</p>
            {:else}
                <!-- Week navigation -->
                <div class="flex items-center justify-between mb-4">
                    <button on:click={() => weekOffset--} disabled={weekOffset === 0}
                            class="p-2 rounded-lg text-brand-500 hover:bg-brand-100 disabled:opacity-30 disabled:cursor-default transition">
                        <ChevronLeft size={18} />
                    </button>
                    <span class="text-sm font-medium text-brand-700">{weekLabel(weekDays)}</span>
                    <button on:click={() => weekOffset++}
                            class="p-2 rounded-lg text-brand-500 hover:bg-brand-100 transition">
                        <ChevronRight size={18} />
                    </button>
                </div>

                <!-- Day columns -->
                <div class="grid grid-cols-7 gap-1 mb-5">
                    {#each weekDays as day, i}
                        {@const dateStr = toDateStr(day)}
                        {@const isPast = dateStr < todayStr}
                        {@const isToday = dateStr === todayStr}
                        {@const isSelected = dateStr === selectedDate}
                        {@const isWorking = !!schedule && !!workingHoursForDate(schedule, dateStr)}
                        <button type="button" on:click={() => selectDay(dateStr)} disabled={isPast}
                            class="flex flex-col items-center py-2.5 rounded-xl text-sm transition-all"
                            class:opacity-30={isPast}
                            class:cursor-default={isPast}
                            class:text-brand-300={!isWorking && !isPast}
                            class:text-brand-700={isWorking && !isSelected}
                            class:bg-brand-800={isSelected}
                            class:text-white={isSelected}
                            class:hover:bg-brand-100={!isSelected && !isPast}
                        >
                            <span class="text-xs mb-1">{SHORT_DAY[i]}</span>
                            <span class="w-8 h-8 flex items-center justify-center rounded-full font-semibold text-sm"
                                  class:border-2={isToday && !isSelected}
                                  class:border-gold-400={isToday && !isSelected}
                                  class:text-gold-500={isToday && !isSelected}
                            >{day.getDate()}</span>
                        </button>
                    {/each}
                </div>

                {#if selectedDate}
                    {#if !schedule || schedule.workingDays.length === 0}
                        <div class="text-sm text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-4 py-3">
                            This specialist has no schedule set.
                        </div>
                    {:else if !selectedHours}
                        <div class="text-sm text-brand-500 bg-brand-100 border border-brand-200 rounded-lg px-4 py-3">
                            Specialist not available on this day.
                        </div>
                    {:else if timeSlots.length === 0}
                        <div class="text-sm text-brand-500 bg-brand-100 border border-brand-200 rounded-lg px-4 py-3">
                            No available slots for this day.
                        </div>
                    {:else}
                        <p class="text-sm font-medium text-brand-700 mb-3">
                            Available slots &middot;
                            <span class="text-gold-500">
                                {new Date(selectedDate + 'T00:00:00').toLocaleDateString('en-GB', {day: 'numeric', month: 'short'})}
                            </span>
                        </p>
                        <div class="flex flex-col border border-brand-200 rounded-xl overflow-hidden mb-5">
                            {#each timeSlots as slot}
                                <button type="button" on:click={() => selectedTime = slot}
                                    class="flex items-center justify-between px-4 py-3.5 text-sm border-b border-brand-100 last:border-0 transition-all"
                                    class:bg-brand-800={selectedTime === slot}
                                    class:text-white={selectedTime === slot}
                                    class:hover:bg-brand-50={selectedTime !== slot}
                                >
                                    <span class="font-semibold">{slot}</span>
                                    <span class="text-sm {selectedTime === slot ? 'text-brand-300' : 'text-brand-400'}">
                                        {selectedService.durationMinutes} min
                                    </span>
                                </button>
                            {/each}
                        </div>
                    {/if}
                {/if}

                {#if startDateTime}
                    <button on:click={create} disabled={submitting}
                            class="w-full flex items-center justify-center gap-2 bg-gold-500 text-white rounded-xl px-4 py-3.5 font-medium hover:bg-gold-600 disabled:opacity-50 transition-colors">
                        <Check size={18} />
                        {submitting ? 'Booking...' : 'Confirm booking'}
                    </button>
                {/if}
            {/if}
        {/if}
    </div>
{:else}
    <!-- Title + Filters -->
    <div class="flex items-center gap-4 mb-8">
        <h1 class="text-2xl font-serif font-semibold text-brand-800 mr-auto">Book a Service</h1>
        <select bind:value={activeCategory}
                class="border border-brand-200 rounded-lg px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition">
            {#each CATEGORIES as cat}
                <option value={cat.value}>{cat.label}</option>
            {/each}
        </select>
        <select bind:value={activeCity}
                class="border border-brand-200 rounded-lg px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition">
            <option value="ALL">All cities</option>
            {#each cities as city}
                <option value={city}>{city}</option>
            {/each}
        </select>
    </div>

    {#if services.length === 0}
        <div class="text-center py-12">
            <Sparkles size={40} class="mx-auto text-brand-300 mb-3" />
            <p class="text-brand-500">No services found in this category.</p>
        </div>
    {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            {#each services as serv}
                <div class="bg-white border border-brand-200 rounded-xl p-5 hover:border-gold-400 hover:shadow-sm transition-all group">
                    <div class="flex items-center gap-2 mb-2">
                        <h2 class="font-semibold text-brand-800">{serv.name}</h2>
                        <span class="text-xs font-medium px-2.5 py-0.5 rounded-full bg-brand-100 text-brand-600">
                            {CATEGORIES.find(c => c.value === serv.category)?.label ?? serv.category}
                        </span>
                    </div>
                    <p class="text-brand-500 text-sm mb-1">{serv.description}</p>
                    <p class="text-brand-400 text-sm mb-1 flex items-center gap-1.5">
                        {orgNames.get(serv.organizationId) ?? 'Unknown'}
                        {#if orgCities.get(serv.organizationId)}
                            <span class="flex items-center gap-0.5"><MapPin size={12} /> {orgCities.get(serv.organizationId)}</span>
                        {/if}
                    </p>
                    <p class="text-sm text-brand-500 flex items-center gap-3 mb-3">
                        <span class="flex items-center gap-1"><Clock size={14} /> {serv.durationMinutes} min</span>
                        <span>{serv.price} RON</span>
                    </p>
                    <button on:click={() => selectService(serv)}
                            class="bg-brand-800 text-brand-100 text-sm px-4 py-2 rounded-lg font-medium hover:bg-brand-700 transition-colors">
                        Book
                    </button>
                </div>
            {/each}
        </div>
    {/if}
{/if}
