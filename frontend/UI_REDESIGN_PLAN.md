# SuperMalle UI Redesign Plan — "Forged Copper"

## Aesthetic Direction: Warm Industrial Modernism

**Concept**: A restaurant system that feels like a masterfully forged copper pan — substantial, warm, confident, with raw edges polished to brilliance. Dark charcoal foundations, burnt copper accents, brutalist typography, and an atmosphere of refined craftsmanship.

**Tone**: Industrial warmth. Dark, heavy, grounded, yet inviting. Think high-end gastropub meets modernist design studio.

**What makes it unforgettable**: The copper glow — a deep, metallic burnt-copper (#c8663e) that pulses through dark charcoal surfaces like embers in a forge. Every interactive element has a tangible, weighted feel.

---

## 1. Design System

### 1.1 Typography

| Role | Font | Weight | Details |
|------|------|--------|---------|
| **Display / Hero** | Alfa Slab One | 400 only | Heavy slab serif, warm authority |
| **Headings H1–H3** | Alfa Slab One | 400 | Oversized, bold |
| **Headings H4–H6** | DM Sans | 700 | Clean contrast |
| **Body** | DM Sans | 400 / 500 | Refined, readable |
| **Labels / Data** | DM Sans | 600 / 700 | Technical clarity |
| **Prices / Codes** | DM Sans | 700 tabular | Monospaced numbers |

**Why this pairing**: Alfa Slab One is a heavy, warm slab serif that instantly communicates substance and craftsmanship — perfect for a restaurant brand. DM Sans provides clean, modern readability as a foil. The contrast between the heavy serif display and clean sans body creates the "industrial warmth" tension.

### 1.2 Color Palette

```
--copper-50:  #fdf2ed
--copper-100: #fae0d5
--copper-200: #f5c1aa
--copper-300: #ef9a7a
--copper-400: #dc7a52
--copper-500: #c8663e     ← PRIMARY
--copper-600: #a84d28
--copper-700: #8a3d1f
--copper-800: #6e3018
--copper-900: #542412

--bg-base:      #121212     ← Darkest background
--bg-surface:   #1a1a1a     ← Card/panel backgrounds
--bg-elevated:  #242424     ← Elevated surfaces
--bg-hover:     #2e2e2e     ← Hover states
--border:       #333333     ← Borders
--border-light: #444444     ← Lighter borders
--text-primary: #f5f5f5     ← Primary text
--text-secondary: #a3a3a3   ← Secondary/muted
--text-dim:     #666666     ← Placeholder/disabled
```

**Light mode**: Uses same copper scale, inverted grays (#fafafa bg, #e5e5e5 surfaces, #171717 text).

### 1.3 Spacing & Radius

- **Border radius**: Default `8px` (rounded-lg equivalent). Sharp corners on action elements (buttons: `6px`), softer on cards (`12px`). No pillowy `rounded-2xl` everywhere.
- **Card padding**: Consistent `p-6` (24px) on surfaces.
- **Gap system**: `3` → `5` → `8` → `12` → `16` (12px, 20px, 32px, 48px, 64px).

### 1.4 Shadows & Glows

```
--shadow-sm:   0 1px 2px rgba(0,0,0,0.3)
--shadow-md:   0 4px 12px rgba(0,0,0,0.4)
--shadow-lg:   0 8px 24px rgba(0,0,0,0.5)
--shadow-copper: 0 4px 20px rgba(200,102,62,0.25)
--glow-copper: 0 0 20px rgba(200,102,62,0.15)
```

---

## 2. Component Architecture

### 2.1 Layout

**Navbar** (customer):
- Fixed top, full-width dark glass (`bg-[#1a1a1a]/80 backdrop-blur-xl border-b border-white/[0.06]`)
- Logo: ChefHat icon in copper gradient square + "SuperMalle" in DM Sans bold
- Nav links: DM Sans medium, uppercase, letter-spaced
- Cart badge: copper circle with count
- Mobile: slide-down menu with copper accent
- **Animations**: Staggered link underline on hover, cart badge bounce on change

**Footer** (customer):
- Dark section (`bg-[#0a0a0a]`) with subtle copper top border
- Logo + address + quick links
- Copyright line with copper "•" separator
- Grain overlay texture

**AdminSidebar**:
- Dark (`bg-[#0a0a0a]`) full-height fixed
- Copper left border accent on active item
- Subtle glow on hover
- Collapsed mode: shows only icons with copper tooltip on hover

### 2.2 Shared Components

- **Button**: 
  - Primary: Copper gradient bg, DM Sans 600, uppercase, tight tracking
  - Secondary: Border with copper border, transparent bg
  - Ghost: No bg, copper text on hover
  - All have `active:scale-[0.97]` press effect

- **Card**: 
  - Dark surface (`bg-[#1a1a1a]`), `border border-white/[0.06]`
  - Hover: slight lift, copper border glow
  - Overflow hidden, uniform padding

- **Input / Form**: 
  - Dark bg (`bg-[#242424]`), copper focus ring (`ring-2 ring-copper-500/50`)
  - Labels: DM Sans 600, uppercase tracking-wider, text-xs
  - Error: copper-400 border + message

- **Badge / Tag**:
  - Solid fill for status (copper for active, gray for inactive)
  - DM Sans 700, text-xs, uppercase

- **Modal**:
  - Full dark overlay (`bg-black/70 backdrop-blur-sm`)
  - Content: dark surface with copper accent header
  - Esc to close, smooth scale-in animation

### 2.3 Animation System

```
Page enter:     fadeIn(0.3s) + slideUp(0.4s) — staggered on children
Card enter:     fadeIn(0.4s) + slideUp(0.3s) — staggered by index * 80ms
Button hover:   subtle scale(1.02) + copper glow
Button press:   scale(0.97)
Nav link hover: underline sweep from center
Modal:          scaleIn(0.95→1) + fadeIn(0.2s)
Toast:          slideInRight(0.3s)
Cart badge:     pulse on count change
Skeleton:       shimmer sweep (not just opacity pulse)
```

---

## 3. Page-by-Page Redesign

### 3.1 Auth Pages (Login, Register, Forgot/Reset Password)
_Difficulty: Medium | Priority: High_

- Each page: centered single-card layout on dark background
- Card: `bg-[#1a1a1a]` with subtle copper border top (`border-t-2 border-copper-500`)
- Title: "Welcome back" / "Create account" in Alfa Slab One, white
- Subtitle: DM Sans muted text
- Inputs: dark bg, copper focus
- Primary CTA: copper gradient button, full width
- OAuth: secondary button with Google icon
- Link to alternate page: "Don't have an account?" in text-secondary
- Background: subtle subtle geometric pattern or noise grain

### 3.2 HomePage
_Difficulty: Hard | Priority: Highest (first impression)_

- **Hero Section**:
  - Full-width dark section with ambient copper glow behind
  - Gradient copper mesh background (subtle, not flat gradient)
  - H1: Alfa Slab One, 5xl, white — "Handcrafted. Delivered."
  - Tagline: DM Sans, text-lg, text-secondary
  - Two CTAs: "Explore Menu" (copper primary) + "Order Now" (border)
  - Trust badges with copper icons in a row below
  - **Decorative**: Floating geometric copper shapes (CSS-only) that drift slowly

- **Categories Section**:
  - Dark section, no gradient white bg
  - H2: Alfa Slab One, "Browse by Category"
  - Cards: dark elevated surfaces with copper-tinted image placeholder
  - Category icon in copper gradient circle
  - Hover: slight lift, copper border glow

- **Featured Dishes Section**:
  - Grid of dish cards
  - Each card: dark bg, top area with food-tinted gradient placeholder
  - Name: DM Sans bold, white
  - Description: text-secondary, line-clamp-2
  - Price: Alfa Slab One, copper-500
  - Add button: copper outline, fills on hover
  - Status badge: copper/gray dot

- **How It Works**:
  - 3-step layout with copper connectors
  - Each step: copper circle icon on dark bg, Alfa Slab One number

- **CTA Banner**:
  - Copper gradient band across full width
  - Heavy text, white

### 3.3 MenuPage + MenuItemPage
_Difficulty: Medium | Priority: High_

- **MenuPage**:
  - Search: dark input with copper search icon
  - Category pills: dark bg, copper active state
  - Grid of item cards (same as featured dish card)
  - Empty state: copper illustration + message

- **MenuItemPage**:
  - Left: large image (or placeholder) with copper border accent
  - Right: name (Alfa Slab One), description, price (copper), customizations
  - Customizations: checkboxes in dark cards with copper accent
  - Special instructions textarea
  - Quantity selector: copper +/- buttons on dark bg
  - Add to cart button: large, copper gradient, full impact

### 3.4 CartPage + CheckoutPage
_Difficulty: Medium | Priority: High_

- **CartPage**:
  - Split layout: items table (left, 2/3) + summary sidebar (right, 1/3)
  - Items: dark card per item, image placeholder + name + qty controls
  - Qty: copper outlined - / + buttons
  - Remove: copper trash icon
  - Summary: dark card with copper accent top
  - Subtotal / tax / total: DM Sans tabular figures
  - Checkout button: full copper gradient

- **CheckoutPage**:
  - Stepped form with copper step indicators
  - Order type: toggle pills (copper active)
  - Address / instructions / coupon sections
  - Coupon: input + apply button (copper outline)
  - Payment: card selection with copper active
  - Place Order: massive copper button

### 3.5 OrdersPage + OrderDetailPage
_Difficulty: Medium | Priority: Medium_

- **OrdersPage**:
  - List of order cards on dark bg
  - Each card: order number, status badge, total, date
  - Status badges: copper for pending, green for completed, etc.
  - Pagination: copper outlined page buttons

- **OrderDetailPage**:
  - Top: Order number (Alfa Slab One) + status badge
  - Status tracker: 5-step copper progress bar
  - Details grid: copper bordered cards
  - Items list with prices
  - Timeline: copper dots with connecting lines

### 3.6 ProfilePage
_Difficulty: Easy | Priority: Low_

- User info card with copper avatar placeholder
- Tabs: Edit Profile / Change Password
- Form fields same as auth inputs
- Save button: copper gradient

### 3.7 Admin Pages
_Difficulty: Hard | Priority: Medium_

- **AdminDashboard**:
  - KPI cards in a 2x3 grid
  - Each card: dark bg, copper gradient icon circle at top
  - KPI value: Alfa Slab One, white
  - KPI label: DM Sans, text-secondary, uppercase
  - Mini chart sparkline (if data available)

  - Recent Orders: compact table with copper header row
  - Quick Links: copper-ghost button links

- **AdminOrders**:
  - Filter bar: search input + status dropdown
  - Table: dark header, alternating row hover
  - Action buttons: copper outlined
  - Detail modal: copper-accented sections

- **AdminMenu, AdminCategories, AdminCoupons**:
  - Consistent table layout with copper action buttons
  - Form modals with copper accent headers

- **AdminSettings**:
  - Sectioned form cards with copper accent left border
  - Working Hours: toggle pills for each day

- **AdminAnnounce**:
  - Composer: dark textarea with copper send button
  - History: cards with copper left border

### 3.8 Error Pages (404, 500)
_Difficulty: Easy | Priority: Low_

- Centered on dark bg
- Error code: massive Alfa Slab One in copper
- Message: DM Sans
- "Go Home" copper button
- Subtle geometric decoration

### 3.9 Legacy Indigo Pages (Stripe Checkout, Order Confirmation, Order Tracking)
_Difficulty: Easy | Priority: Medium_

- Re-theme from indigo to copper
- Same structure, updated colors and typography

### 3.10 Shared Components Overhaul

- **ToastContainer**: Dark bg, copper left border, icon in copper circle
- **Loading**: Copper-colored shimmer skeleton, not gray
- **Form system**: Update to use copper focus ring colors, dark mode defaults

---

## 4. Implementation Order

| Phase | Files | Effort | Impact |
|-------|-------|--------|--------|
| **1. Foundation** | `index.css`, `helpers.js` (add copper utils) | Medium | ★★★★★ |
| **2. Layout** | `Navbar.jsx`, `Footer.jsx`, `AdminSidebar.jsx`, `App.jsx` | Medium | ★★★★★ |
| **3. Auth Pages** | `LoginPage`, `RegisterPage`, `ForgotPasswordPage`, `ResetPasswordPage` | Medium | ★★★★ |
| **4. HomePage** | `HomePage.jsx` | Large | ★★★★★ |
| **5. Menu** | `MenuPage.jsx`, `MenuItemPage.jsx` | Medium | ★★★★ |
| **6. Cart + Checkout** | `CartPage.jsx`, `CheckoutPage.jsx` | Medium | ★★★★ |
| **7. Customer Pages** | `OrdersPage`, `OrderDetailPage`, `ProfilePage` | Medium | ★★★ |
| **8. Admin** | `AdminDashboard`, `AdminOrders`, `AdminMenu`, etc. | Large | ★★★★ |
| **9. Cleanup** | Legacy indigo pages, error pages, shared components | Medium | ★★★ |
| **10. Polish** | Animations, transitions, micro-interactions, grain textures | Medium | ★★★★ |

---

## 5. Technical Approach

### Tailwind v4 Custom Theme
Using `@theme` directive in `index.css`:
```css
@theme {
  --color-copper-50: #fdf2ed;
  --color-copper-500: #c8663e;
  /* ... */
}
```

This makes all copper shades available as `bg-copper-500`, `text-copper-500`, etc. in Tailwind.

### Google Fonts
Load via `index.html` `<link>` tags with `display=swap` for:
- Alfa Slab One (1 weight)
- DM Sans (400, 500, 600, 700)

### Font Classes
```css
.font-display { font-family: 'Alfa Slab One', serif; }
.font-body { font-family: 'DM Sans', sans-serif; }
```

### CSS Custom Properties
All theme tokens available via `var(--copper-500)` for dynamic use in JS.

### Animation Classes
```css
@keyframes copper-shimmer { /* ... */ }
@keyframes glow-pulse { /* ... */ }
@keyframes slide-up { /* ... */ }
/* etc. */
```

---

## 6. Validation Checklist

- [ ] All Tailwind classes exist (no arbitrary values where tokens exist)
- [ ] Typography loads correctly (Google Fonts + fallbacks)
- [ ] Dark mode default with light mode support
- [ ] Responsive at mobile, tablet, desktop
- [ ] All interactive elements have hover/active/focus states
- [ ] Animations respect `prefers-reduced-motion`
- [ ] Color contrast meets WCAG AA (4.5:1 text, 3:1 large text)
- [ ] No indigo/purple legacy colors remain
- [ ] No system-ui/Inter/Roboto/Arial in font stacks
- [ ] Consistent spacing rhythm throughout
- [ ] Copper is dominant, not an afterthought
