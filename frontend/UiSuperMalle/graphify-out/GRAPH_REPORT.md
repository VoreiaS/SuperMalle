# Graph Report - UiSuperMalle  (2026-05-16)

## Corpus Check
- 55 files · ~53,602 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 746 nodes · 881 edges · 56 communities (50 shown, 6 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 20 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]

## God Nodes (most connected - your core abstractions)
1. `SuperMalle Restaurant UI` - 25 edges
2. `formatPrice()` - 21 edges
3. `Session Summary - Phase 2 Implementation` - 20 edges
4. `dependencies` - 19 edges
5. `Phase 2 Implementation Summary` - 15 edges
6. `formatDate()` - 13 edges
7. `Quick Start Guide` - 13 edges
8. `useAuthStore` - 12 edges
9. `Quick Reference Guide - Phase 2 Features` - 11 edges
10. `Implementation Summary` - 11 edges

## Surprising Connections (you probably didn't know these)
- `MenuItemCard()` --calls--> `formatPrice()`  [INFERRED]
  src/pages/customer/MenuPage.jsx → src/api/helpers.js
- `OrderDetailModal()` --calls--> `formatPrice()`  [INFERRED]
  src/pages/admin/AdminOrders.jsx → src/api/helpers.js
- `AdminDashboard()` --calls--> `formatPrice()`  [INFERRED]
  src/pages/admin/AdminDashboard.jsx → src/api/helpers.js
- `fmtAmount()` --calls--> `formatPrice()`  [INFERRED]
  src/pages/admin/AdminPayments.jsx → src/api/helpers.js
- `OrderDetailPage()` --calls--> `formatPrice()`  [INFERRED]
  src/pages/customer/OrderDetailPage.jsx → src/api/helpers.js

## Communities (56 total, 6 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.13
Nodes (8): emptyForm, extractItems(), extractPage(), PAYMENT_STATUS_CONFIG, STATUS_CONFIG, FOOD_ICONS, MenuItemCard(), PLACEHOLDER_COLORS

### Community 1 - "Community 1"
Cohesion: 0.17
Nodes (12): NEXT_STATUS_MAP, OrderDetailModal(), PaymentStatusBadge(), prettyStatus(), STATUS_ICON, StatusBadge(), STATUSES, formatDate() (+4 more)

### Community 4 - "Community 4"
Cohesion: 0.23
Nodes (7): fmtAmount(), PaymentDetailModal(), prettyBrand(), prettyStatus(), RefundModal(), StatusBadge(), STATUSES

### Community 6 - "Community 6"
Cohesion: 0.19
Nodes (6): AdminDashboard(), prettyStatus(), StatusBadge(), formatPrice(), CartPage(), DishCard()

### Community 7 - "Community 7"
Cohesion: 0.22
Nodes (3): CategoryCard(), getGradient(), GRADIENTS

### Community 8 - "Community 8"
Cohesion: 0.05
Nodes (37): createCancellableRequest(), delay(), extractErrorMessage(), handleApiError(), isRetryable(), CheckoutForm(), StripeCheckoutPage(), stripePromise (+29 more)

### Community 10 - "Community 10"
Cohesion: 0.04
Nodes (45): Accessibility, Additional Resources, API Call with Loading, Backend Integration, Basic Usage, code:jsx (import StripeCheckoutPage from '@/pages/checkout/StripeCheck), code:env (# .env), code:jsx (import OrderConfirmationPage from '@/pages/orders/OrderConfi) (+37 more)

### Community 11 - "Community 11"
Cohesion: 0.04
Nodes (44): Accessibility, Accessibility Features, Accessibility Testing, Automated Testing, Backend Integration, Browser Compatibility, Code Quality, code:json ({) (+36 more)

### Community 13 - "Community 13"
Cohesion: 0.33
Nodes (3): AnnouncementCard(), DEMO_ANNOUNCEMENTS, formatNumber()

### Community 14 - "Community 14"
Cohesion: 0.05
Nodes (43): API Client, Code Quality Improvements, Conclusion, Configuration Files, Core Application, ✅ Critical Features (Completed), Dependencies, Dependencies Added (+35 more)

### Community 15 - "Community 15"
Cohesion: 0.05
Nodes (41): Accessibility, Analytics & Monitoring, API Client, Authentication & Security, Compatibility Testing, Critical Issues (Must Fix Before Production), Deployment, Developer Documentation (+33 more)

### Community 16 - "Community 16"
Cohesion: 0.05
Nodes (40): 10. Analytics & Monitoring ⚠️, 1. Environment Configuration ✅, 1. Stripe Payment Integration ⚠️, 2. Error Handling System ✅, 2. Form Validation ⚠️, 3. Loading States ⚠️, 3. OAuth2 Implementation ✅, 4. Accessibility Improvements ⚠️ (+32 more)

### Community 17 - "Community 17"
Cohesion: 0.05
Nodes (39): 10. Performance Optimization, 11. Authentication & Security, 12. State Management, 13. API Client Improvements, 14. Testing, 15. Internationalization (i18n), 16. PWA Support, 17. Analytics & Monitoring (+31 more)

### Community 18 - "Community 18"
Cohesion: 0.05
Nodes (38): dependencies, axios, jsdom, lucide-react, react, react-dom, react-router-dom, recharts (+30 more)

### Community 19 - "Community 19"
Cohesion: 0.06
Nodes (36): 1. Stripe Payment Integration, 2. Order Confirmation Page, 3. Order Tracking Page, 4. Form Validation System, 5. Loading States System, 6. Route Updates, code:jsx (// Navigate to checkout), code:jsx (// Redirected from checkout after successful payment) (+28 more)

### Community 20 - "Community 20"
Cohesion: 0.06
Nodes (30): Building for Production, code:bash (# Navigate to the project directory), code:bash (# Copy the example environment file), code:env (VITE_API_URL=http://localhost:8080/api/v1), code:bash (npm run dev), code:env (VITE_STRIPE_PUBLIC_KEY=pk_test_your_key_here), code:bash (npm run test), code:bash (npm run build) (+22 more)

### Community 21 - "Community 21"
Cohesion: 0.09
Nodes (22): Available Loading Components, Basic Usage, Button Loading, code:jsx (import {), code:jsx (<LoadingSpinner size="sm" color="indigo" />), code:jsx (<FullPageLoading message="Loading your data..." />), code:jsx (<CardSkeleton count={3} />), code:jsx (<button disabled={loading}>) (+14 more)

### Community 22 - "Community 22"
Cohesion: 0.11
Nodes (18): Available Form Components, Available Validation Schemas, Basic Form Example, code:javascript (import { schemas } from '@/lib/validation';), code:javascript (import { z } from 'zod';), code:jsx (import { Form, FormInput, FormSubmitButton } from '@/compone), code:jsx (<FormInput), code:jsx (<FormTextarea) (+10 more)

### Community 23 - "Community 23"
Cohesion: 0.12
Nodes (16): Accessibility, Acknowledgments, API Integration, Available Scripts, Browser Support, Contributing, Error Handling, License (+8 more)

### Community 24 - "Community 24"
Cohesion: 0.17
Nodes (11): Accessibility, Accessibility Features, Browser Compatibility, Conclusion, Contact & Support, Executive Summary, 🎯 Key Achievements, Session Summary - Phase 2 Implementation (+3 more)

### Community 25 - "Community 25"
Cohesion: 0.17
Nodes (12): 1. Stripe Payment Integration, 2. Form Validation System, 3. Loading States System, Components, Components (14), Components (9), Features, Features (+4 more)

### Community 26 - "Community 26"
Cohesion: 0.22
Nodes (9): Automated Testing, code:bash (# Run unit tests), Form Validation, Loading States, Manual Testing Checklist, Order Confirmation, Order Tracking, Stripe Payment Flow (+1 more)

### Community 31 - "Community 31"
Cohesion: 0.33
Nodes (6): code:bash (git clone <repository-url>), code:bash (npm install), code:bash (cp .env.example .env), code:env (# API Configuration), code:bash (npm run dev), Installation

### Community 32 - "Community 32"
Cohesion: 0.33
Nodes (6): code:dockerfile (FROM node:18-alpine as builder), code:bash (docker build -t supermalle-ui .), Deployment, Deployment Options, Docker Deployment, Environment Setup

### Community 34 - "Community 34"
Cohesion: 0.33
Nodes (6): Documentation (2 files), Files Created, Form Validation (2 files), Loading States (1 file), Payment & Order Management (3 files), Route Updates (1 file)

### Community 35 - "Community 35"
Cohesion: 0.4
Nodes (5): code:bash (# Build for production), Deployment Checklist, Deployment Steps, Environment Configuration, Pre-Deployment

### Community 36 - "Community 36"
Cohesion: 0.5
Nodes (4): Admin Features, Customer Features, Features, Technical Features

### Community 37 - "Community 37"
Cohesion: 0.5
Nodes (4): code:bash (npm run test), code:bash (npm run test:coverage), code:bash (npm run test:ui), Testing

### Community 38 - "Community 38"
Cohesion: 0.5
Nodes (4): code:json ({), Key Technologies, New Dependencies, Technical Stack

### Community 39 - "Community 39"
Cohesion: 0.5
Nodes (4): Critical (Must Complete Before Production), High Priority, Medium Priority, Remaining Work

### Community 40 - "Community 40"
Cohesion: 0.5
Nodes (4): Immediate (This Week), Medium-term (Week 3-4), Next Steps, Short-term (Next Week)

### Community 41 - "Community 41"
Cohesion: 0.67
Nodes (3): Authentication, JWT Authentication, OAuth2 (Google)

### Community 42 - "Community 42"
Cohesion: 0.67
Nodes (3): Building for Production, code:bash (npm run build), code:bash (npm run preview)

### Community 43 - "Community 43"
Cohesion: 0.67
Nodes (3): Environment Variables, Optional Variables, Required Variables

### Community 44 - "Community 44"
Cohesion: 0.67
Nodes (3): After This Session, Before This Session, Production Readiness Progress

### Community 45 - "Community 45"
Cohesion: 0.67
Nodes (3): Backend Integration, Frontend Integration, Integration Points

### Community 46 - "Community 46"
Cohesion: 0.67
Nodes (3): Current Limitations, Future Enhancements, Known Issues & Limitations

### Community 47 - "Community 47"
Cohesion: 0.67
Nodes (3): Optimizations Implemented, Performance Considerations, Performance Metrics

### Community 48 - "Community 48"
Cohesion: 0.67
Nodes (3): Security Best Practices, Security Considerations, Security Features

## Knowledge Gaps
- **383 isolated node(s):** `name`, `private`, `version`, `type`, `dev` (+378 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Quick Reference Guide - Phase 2 Features` connect `Community 10` to `Community 21`, `Community 22`?**
  _High betweenness centrality (0.011) - this node is a cross-community bridge._
- **Why does `Phase 2 Implementation Summary` connect `Community 11` to `Community 19`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **Why does `Session Summary - Phase 2 Implementation` connect `Community 24` to `Community 34`, `Community 35`, `Community 38`, `Community 39`, `Community 40`, `Community 44`, `Community 45`, `Community 46`, `Community 47`, `Community 48`, `Community 25`, `Community 26`?**
  _High betweenness centrality (0.008) - this node is a cross-community bridge._
- **Are the 8 inferred relationships involving `formatPrice()` (e.g. with `OrderDetailModal()` and `AdminDashboard()`) actually correct?**
  _`formatPrice()` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `name`, `private`, `version` to the rest of the system?**
  _383 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.13 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.13 - nodes in this community are weakly interconnected._