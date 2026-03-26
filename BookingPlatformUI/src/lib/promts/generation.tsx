/**
 * Component Generation Prompt for TIME Booking & Scheduling Platform
 *
 * This prompt is designed to guide AI-based component generation so that
 * every generated Svelte component matches the existing design system,
 * coding conventions, and visual identity of the application.
 */

export const COMPONENT_GENERATION_PROMPT = `
You are generating Svelte components for the TIME Booking & Scheduling platform —
a warm, elegant, professional beauty & wellness booking SPA.

## Tech Stack (mandatory)
- **Svelte 5** with \`<script lang="ts">\` — always use TypeScript
- **Tailwind CSS 4** utility classes (no inline styles, no separate CSS files unless animations are needed)
- **Lucide-Svelte** for icons (import from 'lucide-svelte')
- **Flowbite-Svelte** for complex widgets when appropriate (import from 'flowbite-svelte')
- SvelteKit conventions: \`$lib/\` imports, \`$app/navigation\`, \`$app/stores\`

## Color Palette (use these Tailwind classes — NEVER use raw hex except for the footer)

| Token         | Class prefix   | Use for                                   |
|---------------|----------------|-------------------------------------------|
| brand-50      | bg-brand-50    | Page background, lightest surfaces        |
| brand-100     | bg-brand-100   | Active nav items, subtle card fills       |
| brand-200     | border-brand-200 | Borders, dividers, input borders        |
| brand-300     | text-brand-300 | Scrollbar, disabled text                  |
| brand-400     | text-brand-400 | Muted/placeholder text, loading states    |
| brand-500     | text-brand-500 | Secondary text, labels                    |
| brand-600     | text-brand-600 | Default nav/link text                     |
| brand-700     | bg-brand-700   | Secondary buttons, dark text              |
| brand-800     | bg-brand-800   | Primary buttons, headings, active sidebar |
| brand-900     | text-brand-900 | Body text, titles                         |
| gold-300      | text-gold-300  | Highlights, decorative accents            |
| gold-400      | border-gold-400| Hover borders on cards                    |
| gold-500      | text-gold-500  | Links, hover states, logo hover           |
| gold-600      | text-gold-600  | Dark gold for emphasis                    |

## Typography

- **Headings**: \`font-serif\` (Playfair Display) — use for page titles and branding
- **Body/UI**: \`font-sans\` (Inter) — default, no class needed
- **Font weights**: \`font-medium\` (labels, nav), \`font-semibold\` (subheadings), \`font-bold\` (headings)
- **Sizes**: \`text-xs\` (labels/badges), \`text-sm\` (body/buttons), \`text-lg\`/\`text-xl\` (headings)
- **Labels**: \`text-xs font-medium text-brand-500 uppercase tracking-wide\`

## Component Patterns

### Buttons
- **Primary**: \`bg-brand-800 text-brand-100 hover:bg-brand-700 px-4 py-2.5 rounded-lg text-sm font-medium transition-all\`
- **Secondary/outline**: \`bg-white border border-brand-200 text-brand-700 hover:bg-brand-50 px-4 py-2 rounded-lg text-sm font-medium transition-colors\`
- **Danger**: \`bg-red-500 text-white hover:bg-red-600 rounded-lg text-sm font-medium transition-colors\`
- **Icon button**: pair a Lucide icon (\`size={16}\`) with text using \`flex items-center gap-2\`

### Form Inputs
- \`border border-brand-200 rounded-lg px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition-colors\`
- Labels above inputs: \`text-xs font-medium text-brand-500 uppercase tracking-wide mb-1.5\`

### Cards
- \`bg-white border border-brand-200 rounded-xl p-6 hover:border-gold-400 hover:shadow-sm transition-all\`
- Content inside cards uses standard spacing: \`gap-3\`, \`gap-4\`
- For cards with images: use \`overflow-hidden\` on the card, image in a \`h-40 w-full object-cover\` container above \`p-6\` content
- For accent/featured cards: add a \`border-t-2 border-t-gold-400\` top accent stripe

### Inline Badges (category tags, labels)
- Use \`inline-flex items-center gap-1 text-xs font-medium px-2.5 py-0.5 rounded-full\`
- Icon inside badge: \`size={11}\` (NOT 12 or 16 — keeps icon proportional to text-xs)
- Muted badge: \`bg-brand-100 text-brand-600\`
- Gold badge: \`bg-gold-400/10 text-gold-600\`
- Ensure vertical centering: the \`items-center\` on the flex container is critical

### Icon + Text Pairs (metadata rows)
- Use \`flex items-center gap-1.5 text-sm text-brand-500\`
- Icon color: \`text-brand-400\` with \`size={14}\`
- Separate multiple metadata items with \`gap-4\` between them
- Wrap in a \`flex items-center gap-4\` container

### Status Badges
- PENDING:   \`bg-amber-50 text-amber-700 border border-amber-200 px-2.5 py-0.5 rounded-full text-xs font-medium\`
- CONFIRMED: \`bg-green-50 text-green-700 border border-green-200 px-2.5 py-0.5 rounded-full text-xs font-medium\`
- CANCELLED: \`bg-red-50 text-red-600 border border-red-200 px-2.5 py-0.5 rounded-full text-xs font-medium\`

### Modals
- Backdrop: \`fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm\`
- Panel: \`bg-white rounded-2xl shadow-xl max-w-md w-full mx-4 overflow-hidden\`
- Footer: \`flex justify-end gap-3 px-6 py-4 bg-gray-50 border-t border-brand-100\`
- Animate entrance with custom \`@keyframes\`: scale(0.95)+translateY(8px) → scale(1)+translateY(0), 0.2s ease-out

### Layout
- Page max-width: \`max-w-7xl mx-auto px-6\` (main pages) or \`max-w-5xl\` (admin)
- Responsive grids: \`grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6\`
- Section spacing: \`py-12\` or \`py-16\` between major sections
- Dividers: \`border-t border-brand-200\` or \`w-px h-6 bg-brand-200\` (vertical)

### Navigation Items
- Default: \`text-brand-600 hover:bg-brand-50 hover:text-brand-800 px-3 py-2 rounded-lg text-sm font-medium transition-all\`
- Active: \`bg-brand-100 text-brand-800\`

### Error/Empty States
- Error banner: \`bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm\`
- Empty state: centered icon (\`text-brand-300\`), message (\`text-brand-400\`), and CTA link (\`text-gold-500\`)

### Loading States
- Use \`text-brand-400\` for "Loading..." text
- Keep it simple — no spinners unless critical

## Code Conventions

1. **TypeScript always** — \`<script lang="ts">\`
2. **Props**: use \`export let propName: Type = defaultValue;\`
3. **Events**: use \`createEventDispatcher()\` for component events
4. **Reactivity**: use \`$:\` reactive declarations, not \`$state()\` runes (Svelte 4 style used in this codebase)
5. **Conditional rendering**: \`{#if}\`, \`{#each}\`, \`{:else}\`
6. **Icons**: import individually from 'lucide-svelte', use \`size={16}\` or \`size={20}\`
7. **No unnecessary wrappers** — keep DOM flat
8. **Accessibility**: semantic HTML elements (button, a, label), focus states on interactive elements
9. **Keyboard support**: Escape to close modals, Enter to submit
10. **Responsive**: mobile-first, add \`md:\` and \`lg:\` breakpoints

## Visual Refinements (learned from visual testing)

- **Icon sizing in badges**: Icons inside small badges (\`text-xs\`) MUST use \`size={11}\` — using 12 or 16 causes misalignment
- **Vertical rhythm**: Use consistent spacing scale — \`mt-1.5\` after titles for descriptions, \`mt-4\` before metadata, \`mt-5\` before CTAs
- **Card density**: Don't over-pad cards. \`p-5\` for compact cards, \`p-6\` for standard. Avoid \`p-8\` on cards.
- **Image cards**: When using thumbnail images, use \`aspect-video\` or fixed \`h-40\` to keep grid alignment consistent across cards with different image sizes
- **Group hover effects**: Use Tailwind \`group\` on the card container and \`group-hover:\` on inner elements for coordinated hover animations (e.g., arrow slide, image zoom)
- **Line clamping**: Use \`line-clamp-2\` on descriptions to prevent cards from becoming different heights
- **Divider between sections**: Use \`border-t border-brand-200 pt-4 mt-4\` (not margin alone) to visually separate card sections

## Anti-Patterns (NEVER do these)

- Do NOT use inline styles — use Tailwind classes
- Do NOT use colors outside the brand/gold palette for UI chrome
- Do NOT use \`rounded-md\` — use \`rounded-lg\` (cards) or \`rounded-2xl\` (modals) or \`rounded-full\` (badges)
- Do NOT add shadows by default — only \`hover:shadow-sm\` on cards
- Do NOT use generic blue/gray for buttons — always brand-800/brand-700
- Do NOT create oversized text — max heading size is \`text-2xl\` for page titles
- Do NOT use heavy borders — always \`border\` (1px), never \`border-2\` (exception: accent stripes with \`border-t-2\`)
- Do NOT forget \`transition-all\` or \`transition-colors\` on interactive elements
- Do NOT use \`size={12}\` for icons inside inline badges — use \`size={11}\`
- Do NOT mix spacing scales — pick consistent gaps within a component

## Example Component Structure

\`\`\`svelte
<script lang="ts">
    import { SomeIcon } from 'lucide-svelte';
    import { createEventDispatcher } from 'svelte';

    export let title: string;
    export let items: ItemType[] = [];

    const dispatch = createEventDispatcher();

    let searchQuery = '';

    $: filteredItems = items.filter(item =>
        item.name.toLowerCase().includes(searchQuery.toLowerCase())
    );
</script>

<div class="max-w-7xl mx-auto px-6 py-12">
    <h1 class="font-serif text-2xl font-bold text-brand-900 mb-6">{title}</h1>

    <input
        type="text"
        bind:value={searchQuery}
        placeholder="Search..."
        class="border border-brand-200 rounded-lg px-4 py-2.5 text-sm w-full max-w-sm
               focus:outline-none focus:border-gold-400 focus:ring-1 focus:ring-gold-400 transition-colors"
    />

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-8">
        {#each filteredItems as item}
            <div class="bg-white border border-brand-200 rounded-xl p-6
                        hover:border-gold-400 hover:shadow-sm transition-all">
                <h3 class="font-semibold text-brand-900">{item.name}</h3>
                <p class="text-sm text-brand-500 mt-1">{item.description}</p>
                <button
                    on:click={() => dispatch('select', item)}
                    class="mt-4 flex items-center gap-2 bg-brand-800 text-brand-100
                           hover:bg-brand-700 px-4 py-2.5 rounded-lg text-sm font-medium transition-all"
                >
                    <SomeIcon size={16} />
                    Select
                </button>
            </div>
        {/each}
    </div>

    {#if filteredItems.length === 0}
        <div class="text-center py-16">
            <SomeIcon size={48} class="mx-auto text-brand-300 mb-4" />
            <p class="text-brand-400">No items found</p>
        </div>
    {/if}
</div>
\`\`\`

## Grid & Equal Height Tips

- When cards are in a grid, ensure equal heights by keeping fixed-height image areas (\`h-40\`) and clamped descriptions (\`line-clamp-2\`)
- Use \`flex flex-col\` on the card with \`mt-auto\` on the CTA button to push it to the bottom, ensuring alignment across cards of different content lengths
- For grids with mixed content (image cards + text-only cards), use the same \`overflow-hidden rounded-xl\` structure on all cards

When generating a component, ALWAYS:
1. Match the warm brown/gold aesthetic — this is a premium beauty/wellness brand
2. Keep the DOM minimal and semantic
3. Include hover and focus states on all interactive elements
4. Make it responsive (mobile-first)
5. Use the exact Tailwind classes from the patterns above
6. Add TypeScript types for all props and data
7. Use \`line-clamp-2\` on variable-length text (descriptions) to maintain grid alignment
8. Use \`group\` + \`group-hover:\` for coordinated card hover effects
`;

export default COMPONENT_GENERATION_PROMPT;
