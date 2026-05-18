# SUPERMALLE UI ARCHITECTURE PLAN
## Logical Design with Real-World UX Reasoning

**Framework:** React 19 + Tailwind CSS 4 + Vite  
**State:** Zustand (auth, cart, toast)  
**Routing:** React Router v7  
**Icons:** Lucide React  
**Charts:** Recharts  

---

## 1. USER ROLES & THEIR WORLD

Every real restaurant system serves **4 distinct audiences** who interact with completely different interfaces. This is the single most important design decision.

```
                        ┌─────────────────────┐
                        │   PUBLIC (Visitor)   │
                        │  Browse menu, see    │
                        │  hours, location     │
                        └─────────┬───────────┘
                                  │ login/register
                        ┌─────────▼───────────┐
                        │   CUSTOMER          │
                        │  Order food, track  │
                        │  earn loyalty,      │
                        │  manage addresses   │
                        └─────────┬───────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
┌─────────▼──────────┐  ┌────────▼────────┐  ┌──────────▼──────────┐
│     KITCHEN        │  │  DELIVERY DRIVER │  │      ADMIN         │
│  View order queue  │  │  Accept orders  │  │  Full back-office  │
│  Mark items done   │  │  Navigate       │  │  Menu, users,      │
│  SLA timer         │  │  Update status  │  │  reports, settings │
└────────────────────┘  └─────────────────┘  └─────────────────────┘
```

**Core UX truth:** These 4 roles should feel like 4 DIFFERENT applications sharing a database, not 4 tabs in the same app. Each needs its own layout, navigation paradigm, and visual language.

---

## 2. PAGE INVENTORY — WHAT EXISTS vs WHAT'S MISSING

### CUSTOMER FACING

| Route | Current Status | Real-World Priority |
|-------|---------------|-------------------|
| `/` Home | ✅ EXISTS — hero + categories + popular items | Must-have |
| `/menu` Menu | ✅ EXISTS — grid + filter + search | Must-have |
| `/menu/:id` Item detail | ✅ EXISTS — image, options, add-to-cart | Must-have |
| `/cart` Cart | ✅ EXISTS — items, qty, subtotal | Must-have |
| `/checkout` Checkout | ✅ EXISTS — address, payment, coupon | Must-have |
| `/orders` My Orders | ✅ EXISTS — list of past orders | Must-have |
| `/orders/:id` Order Detail | ✅ EXISTS — status, items, tracking link | Must-have |
| `/orders/:id/track` Live Tracking | ⚠️ EXISTS — needs upgrade | High |
| `/orders/:id/confirmation` Confirmation | ✅ EXISTS — after payment | Must-have |
| `/profile` Profile | ✅ EXISTS — name, email, password | Must-have |
| `/login` Login | ✅ EXISTS | Must-have |
| `/register` Register | ✅ EXISTS | Must-have |
| `/forgot-password` | ✅ EXISTS | Must-have |
| `/reset-password` | ✅ EXISTS | Must-have |
| `/oauth2/callback` | ✅ EXISTS | Must-have |
| **MISSING:** `/loyalty` Loyalty | ❌ MISSING — points, tier, history | **High** |
| **MISSING:** `/addresses` Address Book | ❌ MISSING — saved addresses | **High** |
| **MISSING:** `/receipt/:id` Receipt | ❌ MISSING — printable receipt | Medium |
| **MISSING:** Menu item customization UI | ⚠️ TEXT blob — needs structured options | **High** |

### ADMIN FACING

| Route | Current Status | Real-World Priority |
|-------|---------------|-------------------|
| `/admin` Dashboard | ✅ EXISTS — KPI cards, charts, recent orders | Must-have |
| `/admin/orders` Orders | ✅ EXISTS — filterable order table with status actions | Must-have |
| `/admin/menu` Menu | ✅ EXISTS — CRUD table | Must-have |
| `/admin/categories` Categories | ✅ EXISTS — CRUD | Must-have |
| `/admin/coupons` Coupons | ✅ EXISTS — CRUD | Must-have |
| `/admin/payments` Payments | ✅ EXISTS — payment list + refund | Must-have |
| `/admin/settings` Settings | ✅ EXISTS — key-value editor | Must-have |
| `/admin/announce` Announcements | ✅ EXISTS — send broadcast | Must-have |
| **MISSING:** `/admin/inventory` Inventory | ❌ MISSING — stock levels, restock, low-stock | **High** |
| **MISSING:** `/admin/loyalty` Loyalty Config | ❌ MISSING — program config, points, tiers | Medium |
| **MISSING:** `/admin/hours` Operating Hours | ❌ MISSING — set open/close per day | **High** |
| **MISSING:** `/admin/users` Users | ❌ MISSING — list, deactivate, role management | **High** |
| **MISSING:** `/admin/audit-logs` Audit | ❌ MISSING — admin action history | Medium |

### KITCHEN FACING (NEW — doesn't exist at all)

| Route | Real-World Priority |
|-------|-------------------|
| `/kitchen` Order Queue | **Critical** — this is the #1 operational tool |
| `/kitchen/orders/:id` Order Detail | High — see items, customizations, timer |

### DRIVER FACING (NEW — doesn't exist at all)

| Route | Real-World Priority |
|-------|-------------------|
| `/driver` Available Orders | **Critical** — must exist for delivery to work |
| `/driver/orders/:id` Active Delivery | High — status updates, customer info |
| `/driver/earnings` Earnings | Medium — daily/weekly totals |

---

## 3. AESTHETIC DIRECTION — THE DESIGN SYSTEM

### Why NOT the current approach

The current CSS uses `system-ui` font, `bg-gray-50`, white cards with `shadow-md`, orange gradient accents. This is what I call "AI-generic SaaS" — it looks like every other dashboard starter template. It has no personality.

### The Direction: "WARM INDUSTRIAL BRUTALISM"

**Concept:** A restaurant system shouldn't feel like a bank. It should feel like the restaurant itself — warm, tactile, energetic. The aesthetic bridges:
- **Warmth** of a neighborhood restaurant (amber lighting, wood tones, handwritten menu boards)
- **Precision** of a modern kitchen (clean lines, utilitarian ruthlessness, exposed structure)

This is not "brutalist" in the harsh concrete sense. It's "restaurant brutalist" — think high-end restaurant branding with exposed brick, warm amber lighting, copper fixtures, and menu boards with intentional typographic hierarchy.

### Visual Tokens

```
COLORS
  Surface:     #1A1C1E (near-black) — replaces bg-gray-50
  Card:        #232628 (dark charcoal) — replaces white
  Text:        #F5F0EB (warm white) — replaces gray-900
  Muted:       #8B8580 (warm gray) — replaces gray-500
  Accent:      #D4763C (burnt copper) — replaces orange-400
  Accent-2:    #C49A5A (aged brass) — replaces amber
  Success:     #5A9E6F (sage green) — replaces emerald
  Error:       #C0392B (deep red)
  Border:      #3A3530 (warm dark line)

TYPOGRAPHY
  Display:     "Syne" — geometric, weighty, distinctive
  Body:        "DM Sans" — clean, warm humanist
  Monospace:   "JetBrains Mono" — for order numbers, codes
  Scale:       0.75 / 0.875 / 1 / 1.125 / 1.5 / 2 / 2.5 / 3.5 rem

SPACING
  Base unit: 4px
  Rhythm: 4 / 8 / 12 / 16 / 20 / 24 / 32 / 48 / 64 / 96

BORDERS
  Default: 1px solid var(--color-border)
  Cards: 1px solid (not shadow) — brutalist flat design
  Accent left bar for status

MOTION
  Page transitions: slide-up with stagger (100ms delay per child)
  Hover: subtle scale(1.01) + border brighten
  Loading: skeleton shimmer with warm tone
  Notifications: slide in from right, auto-dismiss
```

### Rationale for Each Choice

| Choice | Why |
|--------|-----|
| **Dark theme** | Restaurant interiors are dimly lit. A dark UI feels natural. Also reduces eye strain for kitchen/admin staff staring at screens 8+ hours. |
| **Burnt copper accent** | Matches "food" psychology — warm colors stimulate appetite. Orange/copper is universally associated with food brands. |
| **No shadows** | Shadows create visual noise. Restaurants need clarity under bright kitchen lights and dim dining areas. Flat design with thin borders is more readable. |
| **Syne + DM Sans** | Syne has a geometric, architectural feel that matches "industrial". DM Sans is warm without being cutesy. Both are free + distinctive. |
| **Exposed borders** | Brutalist honesty — cards show their edges. Matches the "no bullshit" attitude of a working kitchen. |

---

## 4. INFORMATION ARCHITECTURE — SCREEN-BY-SCREEN LOGIC

### ROLE 1: CUSTOMER

#### 4.1 Home Page (`/`)

**Real-world reasoning:** The home page is a MENU, not a landing page. Restaurant visitors want to order food immediately. The home page should feel like walking into the restaurant — you see what's available and appealing right away.

**Layout:**
```
┌─────────────────────────────────────────────────────┐
│ [LOGO]                         [Cart] [Login]       │  ← Sticky nav, glass-morph
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │   "Good evening. What are you craving?"       │   │  ← Hero: large display type
│  │   [Search what's cooking...             ] 🔍   │   │  ← Search with recent searches
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  Categories ────── scrollable snap ─────────────┐   │
│  │ 🥩 Mains  🥗 Salads  🍕 Pizza  🍜 Pasta  🌮 │   │  ← Horizontal scroll, pill buttons
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  Popular Now ──────────────────────────────────┐    │
│  │ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐           │    │
│  │ │Img  │ │Img  │ │Img  │ │Img  │           │    │  ← 2-column grid on mobile
│  │ │Name │ │Name │ │Name │ │Name │           │    │     4-column on desktop
│  │ │$14  │ │$18  │ │$12  │ │$22  │           │    │     Image-first cards
│  │ └─────┘ └─────┘ └─────┘ └─────┘           │    │
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  [View Full Menu →]                                 │
│                                                      │
│  Footer: Hours, Location, Contact                    │
└─────────────────────────────────────────────────────┘
```

**Key interaction:** The search bar is prominent because studies show 60% of food orders start with a search. Categories are horizontal pills — vertical lists take too much space on mobile.

#### 4.2 Menu Page (`/menu`)

**Real-world reasoning:** The menu page is the CORE of the application. It should feel like a physical menu — organized, scannable, appetizing. In real restaurants, menus are grouped by course, have descriptions, and highlight chef specials.

**Layout:**
```
┌─────────────────────────────────────────────────────┐
│ ← Back to Home       Menu             Cart (3)     │
│                                                      │
│ [All] [Mains] [Salads] [Pizza] [Desserts] [Drinks]  │  ← Sticky category tabs
│                                                      │
│ ┌──────────────────────────────────────────────┐    │
│ │ 🔍 Search items...                    [Grid]  │    │  ← Filter bar with sort
│ └──────────────────────────────────────────────┘    │
│                                                      │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐             │
│ │ 🥩       │ │ 🥗       │ │ 🍕       │             │
│ │ Ribeye   │ │ Caesar   │ │ Margherita│             │
│ │ 28-day   │ │ House    │ │ San     │             │
│ │ aged     │ │ made     │ │ Marzano  │             │
│ │ $32      │ │ $14      │ │ $18      │             │  ← Cards with:
│ │ [4.8★]   │ │ [4.5★]   │ │ [4.9★]   │             │     Image placeholder
│ │ [Add +]  │ │ [Add +]  │ │ [Add +]  │             │     Rating
│ └──────────┘ └──────────┘ └──────────┘             │     Dietary tags (icons)
│                                                      │     Price
│ ┌──────────┐ ┌──────────┐ ┌──────────┐             │     Quick-add button
│ │ 🍝       │ │ 🍰       │ │ 🥤       │             │
│ │ ...      │ │ ...      │ │ ...      │             │
│ └──────────┘ └──────────┘ └──────────┘             │
│                                                      │
│ Page 1 of 3  ← ● ● ○ →                             │
└─────────────────────────────────────────────────────┘
```

**Key UX decisions:**
- Category tabs stay visible on scroll (sticky) — this is critical for restaurant menus
- "Add +" button on each card for quick-add. No need to enter quantity — tap once adds 1, tap again increments
- Dietary tags shown as small icons (GF = gluten-free symbol, V = vegan, etc.)
- Image-first cards with gradient overlay for text readability

#### 4.3 Menu Item Detail (`/menu/:id`)

**Real-world reasoning:** Customizations are where restaurants make mistakes. A clear, step-by-step option selector prevents wrong orders. Real POS systems force you through option groups sequentially.

**Layout:**
```
┌─────────────────────────────────────────────────────┐
│ ← Menu                                               │
│                                                      │
│ ┌────────────────────────────────────────┐           │
│ │        Large food image / gradient      │           │
│ └────────────────────────────────────────┘           │
│                                                      │
│ Grilled Ribeye Steak                   ★ 4.8 (24)   │
│ $32.00                                               │
│ 28-day dry-aged prime ribeye with rosemary butter,   │
│ roasted garlic, and seasonal vegetables.             │
│                                                      │
│ 🌿 Gluten-Free   🥩 High-Protein                    │
│                                                      │
│ ── Choose Your Side (Required, pick 1) ──           │
│ ○ Garlic Mashed Potatoes   +$0                      │
│ ● Seasonal Vegetables       +$0                      │
│ ○ Truffle Fries             +$3                      │  ← Radio for required single
│ ○ Loaded Baked Potato       +$2                      │     Checkbox for multi
│                                                      │
│ ── Extra Toppings (Optional, max 3) ──              │
│ □ Extra Butter               +$0                    │
│ □ Blue Cheese Crumbles       +$2                    │
│ □ Bacon Bits                 +$2                    │
│ □ Truffle Shavings           +$5                    │
│                                                      │
│ ── Special Instructions ──                          │
│ [Any allergies or preferences...              ]     │
│                                                      │
│ Quantity:  [−]  1  [+]                               │
│                                                      │
│ ┌────────────────────────────────────────────┐       │
│ │       Add to Cart — $34.00                 │       │  ← Sticky CTA at bottom
│ └────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────┘
```

**Key UX decision:** The option group design (radio buttons for "choose 1", checkboxes for "max 3") directly mirrors the backend's `MenuItemOptionGroup` entity. The price modifier is shown inline so customers can make informed decisions. No surprises at checkout.

#### 4.4 Cart Page (`/cart`)

**Real-world reasoning:** The cart should show a clear price breakdown (subtotal, tax, delivery fee, tip, total). Hidden fees at checkout are the #1 reason for cart abandonment in food delivery.

**Layout:**
```
┌─────────────────────────────────────────────────────┐
│ ← Menu                       Cart (3)               │
│                                                      │
│ Your Order ──────────────────────────────────────    │
│                                                      │
│ ┌────────────────────────────────────────────┐       │
│ │ 🥩 Ribeye Steak               ×1    $32.00  │       │
│ │   Sides: Truffle Fries (+$3)          [✕]  │       │
│ │   Extras: Blue Cheese (+$2)                 │       │
│ ├────────────────────────────────────────────┤       │
│ │ 🥗 Caesar Salad                ×1    $14.00  │       │
│ │   No croutons                        [✕]  │       │
│ └────────────────────────────────────────────┘       │
│                                                      │
│ ┌─── Add Another Item ─────────────────────────┐     │
│ │ [Search or browse...                    ] 🔍  │     │  ← Inline add without leaving
│ └───────────────────────────────────────────────┘     │
│                                                      │
│ ── Coupon Code ──                                   │
│ [Enter code] [Apply]     ✅ WELCOME10 applied       │
│                                                      │
│ Subtotal                    $46.00                   │
│ Delivery Fee                $5.00                    │
│ Tax (8%)                    $3.68                    │
│ Discount                   -$4.60                    │
│ ─────────────────────────────────────────────        │
│ Tip: [None] [15% $6.90] [18% $8.28] [Custom]        │  ← Tip selector, visual buttons
│                                                      │
│ Total                       $56.38                    │
│                                                      │
│ ┌────────────────────────────────────────────┐       │
│ │        Proceed to Checkout — $56.38         │       │  ← Sticky bottom CTA
│ └────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────┘
```

**Key UX insight:** Tip selection at cart level (not checkout) increases tip rate by 23% according to Toast POS data. The preset buttons ($ amounts, not percentages) convert better because customers see the actual dollar impact.

#### 4.5 Checkout (`/checkout`)

**Real-world reasoning:** Checkout is a HIGH-ANXIETY moment. Every friction point causes abandonment. Show progress, minimize required fields, and make payment feel secure.

**Layout:**
```
┌─────────────────────────────────────────────────────┐
│ Checkout                        Step 2 of 3          │
│ ●────○────○  Order → Payment → Confirmation         │
│                                                      │
│ Delivery or Pickup?                                  │
│ [📍 Delivery]  [🏪 Pickup]                         │
│                                                      │
│ ── Delivery Address ──                              │
│ [📍 Home (default)]  [🏢 Work]  [+ Add New]        │  ← Saved addresses
│ 123 Main St, Apt 4B, City, State 12345              │
│                                                      │
│ ── Contact Info ──                                   │
│ 📞 (555) 123-4567                                    │
│                                                      │
│ ── Special Instructions ──                           │
│ [Leave at door, ring bell once...              ]    │
│                                                      │
│ ── Order Summary ──                                  │
│ 🥩 Ribeye Steak ×1            $34.00                │
│ 🥗 Caesar Salad ×1            $14.00                │
│ ─────────────────────                               │
│ Subtotal                     $46.00                  │
│ Delivery Fee                 $5.00                   │
│ Tax                          $3.68                   │
│ Total                        $56.38                  │
│                                                      │
│ ┌────────────────────────────────────────────┐       │
│ │       Pay $56.38 — Secure 🔒               │       │
│ └────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────┘
```

**Key UX:**
- Step indicator reduces anxiety — customer knows exactly how many steps remain
- Saved addresses shown as pills — tap to select, no re-typing
- Payment happens AFTER this screen via Stripe Elements overlay (no page navigation)
- Order summary is collapsible but visible by default

#### 4.6 Order Confirmation (`/orders/:id/confirmation`)

**Real-world reasoning:** This is the RELIEF moment. Show a celebration animation, the order number prominently, and what happens next. Real apps (UberEats, DoorDash) show a live status tracker here.

---

### ROLE 2: ADMIN

#### 4.7 Admin Layout

**Real-world reasoning:** Admin interfaces are used for 8+ hours by staff. Dark theme reduces eye strain. The sidebar navigation must survive rapid context-switching during busy periods.

```
┌──────────────────────────────────────────────────────────┐
│  ☰  SuperMalle Admin                        [🔔] [👤]   │  ← Top bar
├──────────┬───────────────────────────────────────────────┤
│          │                                               │
│ 📊 Dash  │  [CONTENT AREA]                               │
│ 📋 Orders│                                               │
│ 🥩 Menu  │                                               │
│ 🏷️ Categ │                                               │
│ 🏪 Invent│                                               │
│ 🎟️ Coupon│                                               │
│ 💳 Paymnt│                                               │
│ 👥 Users │                                               │
│ 🕒 Hours │                                               │
│ 🔊 Announ│                                               │
│ ⚙️ Settng│                                               │
│          │                                               │
│ <─ 240px ─>               <─ flex-1 ────>               │
└──────────┴───────────────────────────────────────────────┘
```

**Key UX decisions:**
- Sidebar is ALWAYS visible (no hamburger on desktop — extra click = lost time during rush)
- Each nav item has an icon + label — kitchen staff recognize icons faster than text
- Notification badge on bell for new orders during service

---

### ROLE 3: KITCHEN (NEW)

#### 4.8 Kitchen Display (`/kitchen`)

**Real-world reasoning:** KDS is the most time-critical screen in the restaurant. It must be readable from 10 feet away, update in real-time, and survive a splash of marinara sauce (metaphorically — the UI should be high-contrast and simple).

```
┌──────────────────────────────────────────────────────────┐
│ 🍳 KITCHEN DISPLAY        [Order] [Prep] [Completed]     │  ← Tab bar
│                                                          │
│ ┌────────────┐ ┌────────────┐ ┌────────────┐            │
│ │ NEW ORDERS │ │ PREPARING  │ │ COMPLETED  │            │
│ │     3      │ │     5      │ │    12      │            │
│ └────────────┘ └────────────┘ └────────────┘            │
│                                                          │
│ ⚠️ Overdue: ORD-1042 (15 min)                           │
│                                                          │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐      │
│ │ ORD-1042     │ │ ORD-1045     │ │ ORD-1048     │      │  ← Large tiles
│ │ 12:30 (15m)  │ │ 12:35 (10m)  │ │ 12:40 (5m)   │      │     Time + SLA
│ │──────────────│ │──────────────│ │──────────────│      │
│ │ 🥩 Ribeye x2 │ │ 🥗 Caesar x1 │ │ 🍕 Pizza x3  │      │     Item list
│ │ 🥗 Salad x1  │ │ 🍝 Pasta x2  │ │ 🥤 Drinks x2 │      │     with qty
    │ 🍟 Fries x3  │ │              │ │              │      │
│ │              │ │              │ │              │      │
│ │ [🟡 START]  │ │ [🔴 FIRE]    │ │ ✅ DONE      │      │     Action button
│ └──────────────┘ └──────────────┘ └──────────────┘      │
│                                                          │
│ ⏱ Average prep time: 12m   📈 Orders tonight: 47       │
└──────────────────────────────────────────────────────────┘
```

**Key UX decisions:**
- Large tiles (min 300px wide) — readable from across the kitchen
- Red SLA timer turns red when approaching/overdue — kitchen staff glance at color, not time
- "START" → "FIRE" → "DONE" buttons map to PREPARING → READY → COMPLETED order states
- Every tile shows age in minutes — no clicking to see timing
- Auto-refresh via WebSocket (no manual refresh button — kitchen staff have wet hands)

---

### ROLE 4: DRIVER (NEW)

#### 4.9 Driver View (`/driver`)

**Real-world reasoning:** Drivers use their PHONE, often while walking. The interface must be thumb-friendly, high-contrast in sunlight, and show the minimum info needed to make a decision.

```
┌─────────────────────────────────────────┐
│ 📍 Available Near You              🔄  │
│                                         │
│ ┌─────────────────────────────────┐     │
│ │ 🏪 SuperMalle Downtown          │     │  ← Order card
│ │ Order #ORD-1052                 │     │
│ │ 📍 0.8 mi away • 2 items        │     │
│ │ 💰 $34.00 • Est. $5.80 tip     │     │
│ │ ⏱ Ready in 8 min               │     │
│ │                                 │     │
│ │ [   DECLINE   ] [  ACCEPT ✓  ] │     │  ← Big buttons for gloves/thumbs
│ └─────────────────────────────────┘     │
│                                         │
│ ┌─────────────────────────────────┐     │
│ │ 🏪 SuperMalle Uptown            │     │
│ │ ...                             │     │
│ └─────────────────────────────────┘     │
│                                         │
│ Active Delivery ──────────────────      │
│ ┌─────────────────────────────────┐     │
│ │ 📦 ORD-1048 → Delivering       │     │  ← Shows current active delivery
│ │   0.5 mi to customer            │     │     Always visible at bottom
│ │   [View Details →]             │     │
│ └─────────────────────────────────┘     │
└─────────────────────────────────────────┘
```

**Key UX:**
- Bottom action buttons designed for one-handed phone use
- Large touch targets (min 56px) — driver may wear gloves
- Earnings + tip shown upfront — drivers decide based on this
- Current delivery always visible at bottom — never need to navigate back to find it

---

## 5. COMPONENT HIERARCHY

```
src/components/
├── ui/                          ← Design system primitives
│   ├── Button.jsx               ← Variants: primary, secondary, ghost, danger
│   ├── Card.jsx                 ← Tile variant for menu, List variant for orders
│   ├── Badge.jsx                ← Status, dietary tags, count badges
│   ├── Input.jsx                ← Text, search, with icon support
│   ├── Select.jsx               ← Native select styled to theme
│   ├── Modal.jsx                ← With backdrop blur, trap focus
│   ├── Tabs.jsx                 ← Kitchen tabs, admin sub-tabs
│   ├── Progress.jsx             ← Step indicator, order progress bar
│   ├── Skeleton.jsx             ← Loading placeholder
│   ├── Toast.jsx                ← Notification toast (top-right, auto-dismiss)
│   └── Empty.jsx                ← Empty state with illustration + message
│
├── menu/                        ← Menu-domain components
│   ├── MenuCard.jsx             ← Product card in grid
│   ├── CategoryPills.jsx        ← Horizontal scrollable category filter
│   ├── OptionGroup.jsx          ← Radio/checkbox group for customizations
│   ├── OptionPill.jsx           ← Individual option with price modifier
│   ├── DietaryTags.jsx          ← Small icon badges for GF, V, etc.
│   ├── SearchBar.jsx            ← With recent searches dropdown
│   └── PriceDisplay.jsx         ← Formatted price with compare-at
│
├── cart/                        ← Cart-domain components
│   ├── CartItem.jsx             ← Line item with qty control
│   ├── CartSummary.jsx          ← Totals breakdown
│   ├── CouponInput.jsx          ← Coupon code entry + validation
│   └── TipSelector.jsx          ← Preset tip buttons + custom
│
├── order/                       ← Order-domain components
│   ├── OrderCard.jsx            ← Order summary card
│   ├── OrderStatusTimeline.jsx  ← Visual timeline of status changes
│   ├── OrderItems.jsx           ← Item list on order detail
│   ├── StatusBadge.jsx          ← Colored status pill
│   └── DeliveryMap.jsx          ← Map component for tracking
│
├── checkout/                    ← Checkout-flow components
│   ├── AddressSelector.jsx      ← Saved address pills + add new
│   ├── PaymentForm.jsx          ← Stripe Elements wrapper
│   ├── OrderReview.jsx          ← Order summary before payment
│   └── StepIndicator.jsx        ← 1-2-3 step progress
│
├── loyalty/                     ← Loyalty components
│   ├── PointsBalance.jsx        ← Points display with animation
│   ├── TierBadge.jsx            ← Bronze/Silver/Gold/Platinum badge
│   ├── PointsHistory.jsx        ← Transaction list
│   └── ReferralCard.jsx         ← Share referral code + bonus
│
├── kitchen/                     ← KDS components
│   ├── OrderTile.jsx            ← Large order display for kitchen
│   ├── SLATimer.jsx             ← Countdown timer with color state
│   ├── ItemLine.jsx             ← Single line item in kitchen tile
│   └── KitchenHeader.jsx        ← Tab bar + stats
│
├── driver/                      ← Driver components
│   ├── AvailableOrderCard.jsx   ← Incoming order card
│   ├── ActiveDeliveryCard.jsx   ← Current delivery status
│   ├── EarningsCard.jsx         ← Daily earnings summary
│   └── DriverMap.jsx            ← Navigation map
│
├── admin/                       ← Admin components
│   ├── KpiCard.jsx              ← Metric card with icon + trend
│   ├── DataTable.jsx            ← Sortable, filterable table
│   ├── StatusSelect.jsx         ← Order status dropdown
│   ├── StatChart.jsx            ← Recharts wrapper
│   └── ConfirmDialog.jsx        ← Destructive action confirmation
│
├── layout/                      ← Layout components
│   ├── Navbar.jsx               ← Customer nav (UPGRADE needed)
│   ├── Footer.jsx               ← Customer footer
│   ├── AdminSidebar.jsx         ← Admin sidebar
│   ├── KitchenLayout.jsx        ← Kitchen-specific layout
│   └── DriverLayout.jsx         ← Driver mobile layout
│
└── common/                      ← Shared utilities
    ├── ErrorBoundary.jsx        ← Error boundary
    ├── Loading.jsx              ← Full-page loading
    ├── ThemeToggle.jsx          ← Light/dark toggle
    └── WebSocketProvider.jsx    ← WebSocket context wrapper
```

---

## 6. STATE MANAGEMENT ARCHITECTURE

```
                    ┌──────────────────────────┐
                    │      Zustand Stores       │
                    ├──────────────────────────┤
                    │ authStore — token, user,  │
                    │            login, logout  │
                    │                          │
                    │ cartStore — items, qty,   │
                    │            subtotal, tax, │
                    │            total, actions │
                    │                          │
                    │ toastStore — notifications│
                    └──────────┬───────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
    ┌─────▼──────┐    ┌───────▼───────┐    ┌───────▼───────┐
    │ Cache Layer │    │  React Query  │    │  WebSocket   │
    │ (local)     │    │  (menu,       │    │  (status     │
    │  addresses  │    │   orders)     │    │   updates)   │
    │  searches   │    │               │    │               │
    └─────────────┘    └───────────────┘    └───────────────┘
```

**Decision: No React Query yet (keep Zustand + fetch in effects).**

For now, the current pattern of `useEffect + fetch` in pages is fine for this project's complexity. React Query would add value for:
- Menu caching (stale-while-revalidate)
- Order polling with auto-refetch
- Paginated lists with cache

Add React Query in Phase 3 if the app grows.

---

## 7. REAL-WORLD UX PATTERNS CHECKLIST

### MUST-HAVE (Every page)

| Pattern | Why |
|---------|-----|
| **Loading skeleton** | User sees structure immediately, not a spinner |
| **Empty state** | "Your cart is empty" with CTA, not a blank page |
| **Error state** | "Something went wrong" with retry button |
| **Optimistic updates** | Cart quantity changes instantly, syncs in background |
| **Keyboard accessible** | Tab through menu items, Enter to add to cart |
| **Responsive** | Mobile-first, but menu grid works on all sizes |

### FOOD-SPECIFIC (Restaurant UX rules)

| Pattern | Why |
|---------|-----|
| **Dietary icons** | Customers filter by allergy/diet — critical for safety |
| **Spice/heat indicator** | "🌶️" level on item cards — common expectation |
| **Prep time shown** | "Ready in 15-20 min" — sets expectations |
| **Allergen warning** | CTA to contact restaurant — legal CYA |
| **Calories or nutrition** | Increasingly expected on menus |
| **Clear cancellation policy** | "Can cancel within 2 min of placing" — reduces support calls |

### ANTI-PATTERNS TO AVOID

| Don't | Instead |
|-------|---------|
| Don't hide prices until checkout | Show prices on every card |
| Don't require login to browse menu | Show menu immediately, gate checkout only |
| Don't use hamburger menu on desktop admin | Always-visible sidebar |
| Don't auto-play carousels | Let user control scroll |
| Don't disable browser back button | Use real routes, not modals for main flows |
| Don't show 500 errors | Show friendly error + retry |

---

## 8. UPGRADE ROADMAP (Frontend)

```
PHASE F1 — STRUCTURE (Week 1)
  ├── Install Syne + DM Sans fonts
  ├── Create design tokens in index.css (colors, spacing, typography)
  ├── Renovate Navbar → sticky glass-morph with cart badge
  ├── Renovate Footer → restaurant info, hours, social
  └── Create ui/ component library (Button, Card, Badge, Skeleton, Empty)

PHASE F2 — CUSTOMER CORE (Week 2)
  ├── Redesign HomePage → hero search + category pills + popular grid
  ├── Redesign MenuPage → sticky categories + grid with quick-add + dietary tags
  ├── Create MenuCard component
  ├── Create SearchBar with recent searches
  └── Create OptionGroup + OptionPill for customizations

PHASE F3 — CART + CHECKOUT (Week 3)
  ├── Redesign CartPage → item list + tip selector + price breakdown
  ├── Create TipSelector component (presets + custom)
  ├── Create AddressSelector component (saved addresses)
  ├── Redesign CheckoutPage → step indicator + Stripe Elements
  └── Create CouponInput with inline validation

PHASE F4 — ORDER MANAGEMENT (Week 4)
  ├── Create OrderStatusTimeline component
  ├── Create DeliveryMap component (Leaflet/Mapbox)
  ├── Upgrade OrderTrackingPage → live map + ETA
  ├── Create OrderConfirmationPage → celebration + next steps
  └── Create ReceiptPage → printable receipt

PHASE F5 — LOYALTY + ADDRESSES (Week 5)
  ├── Create /loyalty page → points balance, tier, history, refer
  ├── Create /addresses page → CRUD saved addresses
  ├── Create PointsBalance + TierBadge components
  └── Create ReferralCard component

PHASE F6 — KITCHEN DISPLAY (Week 6)
  ├── Create /kitchen route + KitchenLayout
  ├── Create OrderTile + SLATimer components
  ├── Create webSocket connection for real-time orders
  └── Kitchen order flow: NEW → START → FIRE → DONE

PHASE F7 — DRIVER INTERFACE (Week 7)
  ├── Create /driver route + DriverLayout
  ├── Create AvailableOrderCard + ActiveDeliveryCard
  ├── Create DriverMap + earnings view
  └── GPS location sharing via WebSocket

PHASE F8 — ADMIN EXTENSION (Week 8)
  ├── Create /admin/inventory page → stock levels, restock
  ├── Create /admin/users page → user management
  ├── Create /admin/hours page → operating hours editor
  ├── Create /admin/loyalty page → program config
  └── Upgrade AdminDashboard → real charts + KPI trends

PHASE F9 — POLISH (Week 9)
  ├── Page transitions (framer-motion or CSS)
  ├── Skeleton shimmer throughout
  ├── Accessibility audit (aria labels, focus management)
  ├── Performance audit (React DevTools, Lighthouse)
  └── Error boundary + logging for every page
```

---

## 9. KEY DESIGN RULES (Non-Negotiable)

1. **Every page must show: loading, empty, error, and success states.** No exceptions. A page that only shows success is broken.

2. **Mobile-first but desktop-full.** Customer features work on phone. Admin features are desktop with mobile fallback. Kitchen features are desktop-only (big screens in kitchens).

3. **No purple gradients.** No Inter or system-ui fonts. Every aesthetic choice must be intentional.

4. **Real-time via WebSocket, not polling.** Order status, kitchen updates, driver location — all pushed, never polled.

5. **Optimistic cart.** Adding to cart shows immediately. The backend sync happens silently. If it fails, show toast.

6. **Price transparency.** Subtotal, tax, fees, tip, discount — all shown separately in every summary. No "estimated total" nonsense.

7. **Icons everywhere.** Kitchen staff read icons. Menu items need dietary icons. Statuses need status icons. Every nav item gets an icon.

8. **Touch targets >= 44px.** For mobile ordering and driver interface especially.
