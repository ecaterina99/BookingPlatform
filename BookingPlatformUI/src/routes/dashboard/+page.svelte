<script lang="ts">
    import { onMount, onDestroy } from 'svelte';
    import { isLoggedIn, currentUser } from '$lib/stores/auth';
    import { CalendarDays, Building2, Scissors, ArrowRight, Search, UserSearch, Clock, CircleCheckBig, MapPin } from 'lucide-svelte';
    import { goto } from '$app/navigation';
    import { organizationsApi } from '$lib/api/organizations';
    import type { OrganizationDTO } from '$lib/types';

    const videos = [
        { src: '/3998516-uhd_4096_2160_25fps.mp4', text: 'Find it.', duration: 7 },          // nails
        { src: '/4855852-hd_1920_1080_30fps.mp4', text: 'Book it.', duration: 3 },            // barber
        { src: '/7509480-hd_1920_1080_25fps.mp4', text: 'Enjoy it.', duration: 7 },           // makeup
        { src: '/8480550-hd_1920_1080_25fps.mp4', text: 'Smart choices start with the right booking.', duration: 7 }, // fitness
    ];

    const categories = [
        { label: 'Hair', value: 'HAIR' },
        { label: 'Barber', value: 'BARBER' },
        { label: 'Nails', value: 'NAILS' },
        { label: 'Skin care', value: 'SKIN_CARE' },
        { label: 'Massage', value: 'MASSAGE' },
        { label: 'Makeup', value: 'MAKEUP' },
        { label: 'Tattoo', value: 'TATTOO' },
        { label: 'Health & Fitness', value: 'HEALTH_AND_FITNESS' },
        { label: 'More...', value: 'OTHER' },
    ];

    let searchQuery = '';
    let showSuggestions = false;
    let currentIndex = 0;
    let cities: string[] = [];
    let showAllCities = false;
    let displayedText = '';
    let typing = false;
    let videoEl: HTMLVideoElement;
    let typeTimer: ReturnType<typeof setTimeout>;
    let textVisible = false;

    function typeText(text: string): Promise<void> {
        return new Promise((resolve) => {
            displayedText = '';
            textVisible = true;
            typing = true;
            let i = 0;
            function next() {
                if (i < text.length) {
                    displayedText += text[i];
                    i++;
                    typeTimer = setTimeout(next, 60);
                } else {
                    typing = false;
                    resolve();
                }
            }
            next();
        });
    }

    let videoStartTime = 0;
    let switching = false;

    function goToNext() {
        if (switching) return;
        switching = true;
        textVisible = false;
        const nextIndex = (currentIndex + 1) % videos.length;
        currentIndex = nextIndex;
        if (videoEl) {
            videoEl.src = videos[currentIndex].src;
            videoEl.load();
            videoEl.play();
        }
    }

    function handleTimeUpdate() {
        if (!videoEl || switching) return;
        const elapsed = videoEl.currentTime - videoStartTime;
        if (elapsed >= videos[currentIndex].duration) {
            goToNext();
        }
    }

    function handleVideoEnded() {
        goToNext();
    }

    function handleVideoPlay() {
        switching = false;
        videoStartTime = videoEl?.currentTime ?? 0;
        displayedText = '';
        textVisible = false;
        const isLast = currentIndex === videos.length - 1;
        typeTimer = setTimeout(() => {
            if (isLast) {
                // Static text, no typewriter effect
                displayedText = videos[currentIndex].text;
                textVisible = true;
            } else {
                typeText(videos[currentIndex].text);
            }
        }, 300);
    }

    $: suggestions = searchQuery.trim().length > 0
        ? categories.filter(c => c.label.toLowerCase().startsWith(searchQuery.trim().toLowerCase()))
        : [];

    function handleSearch() {
        showSuggestions = false;
        // Check if the query matches a category exactly
        const match = categories.find(c => c.label.toLowerCase() === searchQuery.trim().toLowerCase());
        if (match) {
            goto(`/services?category=${match.value}`);
        } else {
            goto(`/services?q=${encodeURIComponent(searchQuery)}`);
        }
    }

    function selectSuggestion(cat: typeof categories[0]) {
        searchQuery = cat.label;
        showSuggestions = false;
        goto(`/services?category=${cat.value}`);
    }

    function selectCategory(value: string) {
        goto(`/services?category=${value}`);
    }

    function handleInputFocus() {
        if (searchQuery.trim().length > 0) showSuggestions = true;
    }

    function handleInputBlur() {
        // Delay to allow click on suggestion
        setTimeout(() => { showSuggestions = false; }, 200);
    }

    onMount(async () => {
        if (videoEl) {
            videoEl.src = videos[0].src;
            videoEl.load();
        }
        try {
            const orgs = await organizationsApi.getAll();
            cities = [...new Set(orgs.map((o: OrganizationDTO) => o.city))].sort();
        } catch {}
    });

    onDestroy(() => {
        clearTimeout(typeTimer);
    });
</script>

<div class="flex flex-col">
    <!-- Hero: single video player, half screen height -->
    <section class="relative w-full h-[50vh] overflow-hidden">
        <video
            bind:this={videoEl}
            autoplay
            muted
            playsinline
            class="absolute inset-0 w-full h-full object-cover"
            on:ended={handleVideoEnded}
            on:playing={handleVideoPlay}
            on:timeupdate={handleTimeUpdate}
        >
            <source src={videos[0].src} type="video/mp4" />
        </video>

        <!-- Dark overlay -->
        <div class="absolute inset-0 bg-black/50"></div>

        <!-- Typed text overlay -->
        <div class="relative z-10 flex flex-col items-center justify-center h-full text-white px-4">
            <h1 class="font-serif text-5xl md:text-7xl font-bold drop-shadow-lg text-center min-h-[1.2em]">
                {#if textVisible}
                    <span>{displayedText}</span>
                {/if}
            </h1>
        </div>

        <!-- Search bar overlaid at the bottom -->
        <div class="absolute bottom-0 left-0 right-0 z-20 pb-6 px-4">
            <div class="max-w-2xl mx-auto">
                <div class="flex flex-col sm:flex-row gap-3 w-full mb-4">
                    <div class="flex-1 relative">
                        <Search size={20} class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 z-10" />
                        <input
                            type="text"
                            bind:value={searchQuery}
                            placeholder="Search services or businesses"
                            class="w-full pl-12 pr-4 py-4 rounded-xl text-gray-800 bg-white/95 backdrop-blur shadow-lg border-0 focus:ring-2 focus:ring-gold-400 text-base"
                            on:keydown={(e) => e.key === 'Enter' && handleSearch()}
                            on:input={() => { showSuggestions = true; }}
                            on:focus={handleInputFocus}
                            on:blur={handleInputBlur}
                            autocomplete="off"
                        />
                        {#if showSuggestions && suggestions.length > 0}
                            <ul class="absolute top-full left-0 right-0 mt-1 bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden z-30">
                                {#each suggestions as cat}
                                    <li>
                                        <button
                                            on:mousedown|preventDefault={() => selectSuggestion(cat)}
                                            class="w-full text-left px-5 py-3 text-gray-800 hover:bg-brand-50 transition-colors text-sm"
                                        >
                                            {cat.label}
                                        </button>
                                    </li>
                                {/each}
                            </ul>
                        {/if}
                    </div>
                    <button
                        on:click={handleSearch}
                        class="px-8 py-4 bg-brand-700 hover:bg-brand-800 text-white font-semibold rounded-xl shadow-lg transition-colors text-base"
                    >
                        Search
                    </button>
                </div>
                <!-- Category links -->
                <div class="flex flex-wrap justify-center gap-4">
                    {#each categories as cat}
                        <button
                            on:click={() => selectCategory(cat.value)}
                            class="text-white/80 hover:text-white text-sm font-medium border-b border-transparent hover:border-white/60 pb-0.5 transition-all"
                        >
                            {cat.label}
                        </button>
                    {/each}
                </div>
            </div>
        </div>
    </section>

    <!-- Cards section -->
    <section class="bg-gray-50 py-20 px-4">
        <div class="max-w-5xl mx-auto">
            {#if $isLoggedIn}
                <h2 class="text-2xl font-serif font-semibold text-brand-800 mb-2 text-center">
                    Welcome back, {$currentUser?.fullName}
                </h2>
                <p class="text-brand-500 mb-12 max-w-md mx-auto text-center">
                    Manage your appointments, explore services, and stay on schedule.
                </p>
            {/if}

            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 w-full">
                <a href="/bookings/new"
                   class="group bg-white rounded-xl border border-brand-200 p-8 hover:border-gold-400 hover:shadow-md transition-all">
                    <div class="w-12 h-12 rounded-lg bg-brand-100 flex items-center justify-center mb-5 group-hover:bg-gold-400/10 transition-colors">
                        <Scissors size={24} class="text-brand-600 group-hover:text-gold-500" />
                    </div>
                    <h3 class="text-lg font-semibold text-brand-800 mb-2">Book a Service</h3>
                    <p class="text-sm text-brand-500 mb-4">Browse and book from a wide range of beauty and wellness services</p>
                    <span class="text-sm font-medium text-gold-500 flex items-center gap-1 group-hover:gap-2 transition-all">
                        Get started <ArrowRight size={14} />
                    </span>
                </a>

                <a href="/organizations"
                   class="group bg-white rounded-xl border border-brand-200 p-8 hover:border-gold-400 hover:shadow-md transition-all">
                    <div class="w-12 h-12 rounded-lg bg-brand-100 flex items-center justify-center mb-5 group-hover:bg-gold-400/10 transition-colors">
                        <Building2 size={24} class="text-brand-600 group-hover:text-gold-500" />
                    </div>
                    <h3 class="text-lg font-semibold text-brand-800 mb-2">Organizations</h3>
                    <p class="text-sm text-brand-500 mb-4">Explore salons, studios, and wellness centers near you</p>
                    <span class="text-sm font-medium text-gold-500 flex items-center gap-1 group-hover:gap-2 transition-all">
                        Browse <ArrowRight size={14} />
                    </span>
                </a>

                {#if $isLoggedIn}
                    <a href="/bookings"
                       class="group bg-white rounded-xl border border-brand-200 p-8 hover:border-gold-400 hover:shadow-md transition-all">
                        <div class="w-12 h-12 rounded-lg bg-brand-100 flex items-center justify-center mb-5 group-hover:bg-gold-400/10 transition-colors">
                            <CalendarDays size={24} class="text-brand-600 group-hover:text-gold-500" />
                        </div>
                        <h3 class="text-lg font-semibold text-brand-800 mb-2">My Bookings</h3>
                        <p class="text-sm text-brand-500 mb-4">View and manage your upcoming appointments</p>
                        <span class="text-sm font-medium text-gold-500 flex items-center gap-1 group-hover:gap-2 transition-all">
                            View all <ArrowRight size={14} />
                        </span>
                    </a>
                {:else}
                    <a href="/register"
                       class="group bg-white rounded-xl border border-brand-200 p-8 hover:border-gold-400 hover:shadow-md transition-all">
                        <div class="w-12 h-12 rounded-lg bg-brand-100 flex items-center justify-center mb-5 group-hover:bg-gold-400/10 transition-colors">
                            <CalendarDays size={24} class="text-brand-600 group-hover:text-gold-500" />
                        </div>
                        <h3 class="text-lg font-semibold text-brand-800 mb-2">Get Started</h3>
                        <p class="text-sm text-brand-500 mb-4">Create an account to book appointments</p>
                        <span class="text-sm font-medium text-gold-500 flex items-center gap-1 group-hover:gap-2 transition-all">
                            Register <ArrowRight size={14} />
                        </span>
                    </a>
                {/if}
            </div>
        </div>
    </section>

    <!-- How it works section -->
    <section class="bg-white py-20 px-4">
        <div class="max-w-5xl mx-auto">
            <h2 class="text-2xl font-serif font-bold text-brand-900 mb-12">How does TIME work?</h2>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-10">
                <div>
                    <div class="w-12 h-12 rounded-full bg-brand-100 flex items-center justify-center mb-4">
                        <UserSearch size={24} class="text-brand-600" />
                    </div>
                    <h3 class="text-lg font-bold text-brand-900 mb-2">Choose your salon and service</h3>
                    <p class="text-sm text-brand-500 leading-relaxed">
                        Pick from the categories above or search for your favorite salon and select the service you need.
                    </p>
                </div>

                <div>
                    <div class="w-12 h-12 rounded-full bg-brand-100 flex items-center justify-center mb-4">
                        <Clock size={24} class="text-brand-600" />
                    </div>
                    <h3 class="text-lg font-bold text-brand-900 mb-2">Pick the date and time</h3>
                    <p class="text-sm text-brand-500 leading-relaxed">
                        Check availability for the service and choose the day and time that works best for you.
                    </p>
                </div>

                <div>
                    <div class="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center mb-4">
                        <CircleCheckBig size={24} class="text-green-600" />
                    </div>
                    <h3 class="text-lg font-bold text-brand-900 mb-2">Confirm and you're all set</h3>
                    <p class="text-sm text-brand-500 leading-relaxed">
                        The specialist receives your booking details directly, and you'll get a reminder notification before your appointment.
                    </p>
                </div>
            </div>
        </div>
    </section>

    <!-- Find salons in your city -->
    {#if cities.length > 0}
        <section class="bg-gray-50 py-20 px-4">
            <div class="max-w-5xl mx-auto">
                <h2 class="text-2xl font-serif font-bold text-brand-900 mb-10">Find the best salons in your city</h2>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-x-16 gap-y-1">
                    {#each (showAllCities ? cities : cities.slice(0, 6)) as city}
                        <a href="/organizations?city={encodeURIComponent(city)}"
                           class="flex items-center gap-3 py-4 border-b border-brand-200 text-brand-800 hover:text-gold-600 transition-colors">
                            <MapPin size={16} class="text-brand-400 shrink-0" />
                            <span class="text-base">Salons in {city}</span>
                        </a>
                    {/each}
                </div>

                {#if cities.length > 6 && !showAllCities}
                    <button
                        on:click={() => showAllCities = true}
                        class="mt-6 text-brand-700 font-semibold text-sm flex items-center gap-1 hover:text-gold-600 transition-colors"
                    >
                        See more cities <ArrowRight size={14} />
                    </button>
                {/if}
            </div>
        </section>
    {/if}
</div>

