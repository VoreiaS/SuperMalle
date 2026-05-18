import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  UtensilsCrossed, ShoppingCart, Utensils, ArrowRight, ChefHat, Clock,
  Star, Plus, Search, Truck, CheckCircle2, PackageOpen
} from 'lucide-react';
import { categoryApi, menuApi } from '../../api/endpoints';
import { extractItems, formatPrice } from '../../api/helpers';
import useCartStore from '../../store/cartStore';
import useAuthStore from '../../store/authStore';

/* ─────────────── Color palettes ─────────────── */

const CATEGORY_COLORS = [
  'from-copper-500 to-copper-700',
  'from-copper-400 to-copper-600',
  'from-amber-600 to-copper-700',
  'from-copper-500 to-amber-700',
  'from-copper-600 to-copper-800',
  'from-copper-400 to-amber-600',
];

const DISH_COLORS = [
  'from-copper-500/30 via-copper-500/10 to-bg-elevated',
  'from-amber-600/30 via-copper-500/10 to-bg-elevated',
  'from-copper-400/30 via-copper-500/10 to-bg-elevated',
  'from-copper-600/30 via-copper-500/10 to-bg-elevated',
  'from-amber-500/30 via-copper-500/10 to-bg-elevated',
  'from-copper-700/30 via-copper-500/10 to-bg-elevated',
];

/* ─────────────── Skeleton ─────────────── */

function Skeleton({ className = '' }) {
  return <div className={`animate-shimmer rounded-lg ${className}`} />;
}

function CategorySkeleton() {
  return (
    <div className="card-copper overflow-hidden">
      <Skeleton className="h-24 w-full !rounded-none" />
      <div className="p-4 space-y-2"><Skeleton className="h-4 w-2/3" /></div>
    </div>
  );
}

function DishSkeleton() {
  return (
    <div className="card-copper overflow-hidden">
      <Skeleton className="h-40 w-full !rounded-none" />
      <div className="p-5 space-y-3">
        <Skeleton className="h-4 w-3/4" />
        <Skeleton className="h-3 w-1/2" />
        <Skeleton className="h-10 w-full mt-2" />
      </div>
    </div>
  );
}

/* ─────────────── Hero ─────────────── */

function HeroSection() {
  const { user } = useAuthStore();
  return (
    <section className="relative overflow-hidden bg-bg-base border-b border-copper-500/10">
      {/* Ambient copper glow */}
      <div className="absolute -top-32 -right-32 w-[500px] h-[500px] rounded-full bg-copper-500/8 blur-[140px] pointer-events-none" />
      <div className="absolute -bottom-32 -left-32 w-[400px] h-[400px] rounded-full bg-copper-400/5 blur-[120px] pointer-events-none" />
      <div className="absolute inset-0 bg-noise pointer-events-none" />

      {/* Floating geometric elements */}
      <div className="absolute top-20 left-[15%] w-3 h-3 rounded bg-copper-500/20 animate-copper-float" />
      <div className="absolute top-40 right-[20%] w-2 h-2 rounded bg-copper-500/30" style={{ animation: 'copperFloat 7s ease-in-out infinite 1s' }} />
      <div className="absolute bottom-32 left-[30%] w-4 h-0.5 rounded-full bg-copper-500/20" style={{ animation: 'copperFloat 5s ease-in-out infinite 0.5s' }} />
      <div className="absolute top-1/3 left-[60%] w-1.5 h-1.5 rounded-full bg-copper-500/25" style={{ animation: 'copperFloat 8s ease-in-out infinite 2s' }} />

      <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20 sm:py-28 lg:py-36 flex flex-col items-center text-center">
        <div className="inline-flex items-center gap-2 bg-copper-500/10 text-copper-500 text-sm font-body font-semibold px-4 py-1.5 rounded-lg mb-6 border border-copper-500/20 animate-fade-in">
          <ChefHat className="w-4 h-4" />
          SuperMalle Kitchen
        </div>

        <h1 className="font-display text-4xl sm:text-5xl lg:text-7xl text-text-primary leading-tight animate-slide-up stagger-1">
          Handcrafted.
          <br />
          <span className="text-gradient-copper">Delivered.</span>
        </h1>

        <p className="mt-5 max-w-xl text-lg font-body text-text-secondary leading-relaxed animate-slide-up stagger-2">
          From our kitchen to your table — explore handcrafted dishes made with
          the finest ingredients, ready when you are.
        </p>

        <div className="mt-8 flex flex-col sm:flex-row gap-4 animate-slide-up stagger-3">
          <Link to="/menu" className="btn-copper !px-8 !py-4 !text-sm !font-bold uppercase tracking-wider !rounded-lg shadow-copper-md hover:shadow-copper-lg">
            <Search className="w-5 h-5" />
            Explore Menu
          </Link>
          <Link to={user ? "/menu" : "/register"}
            className="btn-copper-outline !px-8 !py-4 !text-sm !font-bold uppercase tracking-wider">
            {user ? 'Order Now' : 'Get Started'}
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>

        {/* Trust badges */}
        <div className="mt-10 flex flex-wrap justify-center gap-6 text-text-dim text-sm font-body animate-fade-in">
          {[
            { icon: Clock, text: '30 min delivery' },
            { icon: Star, text: '4.9 average rating' },
            { icon: Truck, text: 'Free shipping over $30' },
          ].map(({ icon: Icon, text }) => (
            <span key={text} className="flex items-center gap-1.5">
              <Icon className="w-4 h-4 text-copper-500" /> {text}
            </span>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ─────────────── Category Card ─────────────── */

function CategoryCard({ category, index }) {
  const color = CATEGORY_COLORS[index % CATEGORY_COLORS.length];
  const Icon = [UtensilsCrossed, ShoppingCart, ChefHat, Star, Clock, Utensils][index % 6];
  return (
    <Link to={`/menu?category=${category.id}`}
      className="group card-copper-hover overflow-hidden block">
      <div className={`h-24 bg-gradient-to-br ${color} flex items-center justify-center relative`}>
        <Icon className="w-10 h-10 text-white/40 group-hover:scale-110 group-hover:text-white/60 transition-all duration-300" />
      </div>
      <div className="p-4">
        <h3 className="font-body font-bold text-sm text-text-primary group-hover:text-copper-500 transition-colors">
          {category.name}
        </h3>
      </div>
    </Link>
  );
}

/* ─────────────── Categories Section ─────────────── */

function CategoriesSection({ categories, loading }) {
  return (
    <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div className="text-center mb-10">
        <h2 className="font-display text-3xl text-text-primary">Browse by Category</h2>
        <p className="mt-2 font-body text-text-secondary text-sm">Explore our menu by category</p>
      </div>

      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-5">
          {Array.from({ length: 6 }).map((_, i) => <CategorySkeleton key={i} />)}
        </div>
      ) : categories.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <div className="w-14 h-14 rounded-xl bg-copper-500/10 flex items-center justify-center mb-4">
            <UtensilsCrossed className="w-7 h-7 text-copper-500" />
          </div>
          <h3 className="font-body font-bold text-text-primary mb-1">No categories yet</h3>
          <p className="font-body text-text-dim text-sm">Categories will appear here once they are added.</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-5">
          {categories.map((cat, i) => (
            <div key={cat.id} className="animate-slide-up" style={{ animationDelay: `${i * 80}ms` }}>
              <CategoryCard category={cat} index={i} />
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

/* ─────────────── Dish Card ─────────────── */

function DishCard({ dish, index, onAddToCart, adding }) {
  const color = DISH_COLORS[index % DISH_COLORS.length];
  const isUnavailable = dish.isAvailable === false;

  return (
    <div className={`card-copper overflow-hidden flex flex-col transition-all duration-300 ${
      isUnavailable ? 'opacity-50' : 'card-copper-hover'
    }`}>
      {/* Image area */}
      <div className={`h-40 bg-gradient-to-br ${color} flex items-center justify-center relative`}>
        <UtensilsCrossed className="w-10 h-10 text-copper-500/20" />
        {isUnavailable ? (
          <span className="absolute top-3 right-3 px-2.5 py-1 bg-bg-base/80 text-text-dim text-xs font-body font-semibold rounded-md border border-border-subtle">
            Unavailable
          </span>
        ) : (
          <span className="absolute top-3 right-3 px-2.5 py-1 bg-green-500/20 text-green-500 text-xs font-body font-semibold rounded-md border border-green-500/30">
            Available
          </span>
        )}
      </div>

      <div className="p-5 flex flex-col flex-1">
        <h3 className="font-body font-bold text-text-primary text-lg leading-snug">{dish.name}</h3>
        {dish.description && (
          <p className="text-sm font-body text-text-secondary mt-1 line-clamp-2">{dish.description}</p>
        )}

        <div className="flex items-center gap-3 mt-2 text-xs text-text-dim font-body">
          {dish.categoryName && (
            <span className="inline-flex items-center gap-1">
              <ChefHat className="w-3.5 h-3.5" /> {dish.categoryName}
            </span>
          )}
          {dish.preparationTimeMinutes != null && (
            <span className="inline-flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" /> {dish.preparationTimeMinutes} min
            </span>
          )}
        </div>

        <div className="mt-auto pt-4 flex items-center justify-between">
          <span className="font-display text-xl text-copper-500">{formatPrice(dish.price)}</span>
          <button
            onClick={() => onAddToCart(dish.id)}
            disabled={adding || isUnavailable}
            className="btn-copper-outline !px-4 !py-2 !text-xs !font-bold uppercase tracking-wider"
          >
            <Plus className="w-3.5 h-3.5" />
            {adding ? 'Adding...' : 'Add'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ─────────────── Featured Dishes ─────────────── */

function FeaturedDishesSection({ dishes, loading }) {
  const addItem = useCartStore((s) => s.addItem);
  const [addingId, setAddingId] = useState(null);

  const handleAddToCart = useCallback(async (menuItemId) => {
    setAddingId(menuItemId);
    try {
      await addItem({ menuItemId, quantity: 1, customizations: [], specialInstructions: '' });
    } catch { /* handled by store */ } finally { setAddingId(null); }
  }, [addItem]);

  return (
    <section className="border-t border-copper-500/10">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-10">
          <h2 className="font-display text-3xl text-text-primary">Featured Dishes</h2>
          <p className="mt-2 font-body text-text-secondary text-sm">Our chef's handpicked favorites</p>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {Array.from({ length: 6 }).map((_, i) => <DishSkeleton key={i} />)}
          </div>
        ) : dishes.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="w-14 h-14 rounded-xl bg-copper-500/10 flex items-center justify-center mb-4">
              <PackageOpen className="w-7 h-7 text-copper-500" />
            </div>
            <h3 className="font-body font-bold text-text-primary mb-1">No dishes found</h3>
            <p className="font-body text-text-dim text-sm">Our chefs are cooking up something special. Check back soon!</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {dishes.map((dish, i) => (
              <div key={dish.id} className="animate-slide-up" style={{ animationDelay: `${i * 80}ms` }}>
                <DishCard dish={dish} index={i} onAddToCart={handleAddToCart} adding={addingId === dish.id} />
              </div>
            ))}
          </div>
        )}

        <div className="mt-10 text-center">
          <Link to="/menu"
            className="inline-flex items-center gap-2 text-copper-500 font-body font-semibold hover:text-copper-400 transition-colors text-sm">
            View Full Menu <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </div>
    </section>
  );
}

/* ─────────────── How It Works ─────────────── */

function HowItWorksSection() {
  const steps = [
    { icon: Search, title: 'Browse Menu', desc: 'Explore our curated selection of dishes across multiple categories.' },
    { icon: ShoppingCart, title: 'Place Order', desc: 'Customize your items, add them to your cart, and checkout in seconds.' },
    { icon: CheckCircle2, title: 'Enjoy Food', desc: 'Sit back and relax — we deliver fresh, hot food right to your door.' },
  ];

  return (
    <section className="border-t border-copper-500/10">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-12">
          <h2 className="font-display text-3xl text-text-primary">How It Works</h2>
          <p className="mt-2 font-body text-text-secondary text-sm">Three simple steps to your next meal</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {steps.map((step, i) => (
            <div key={i} className="relative text-center group">
              {i < steps.length - 1 && (
                <div className="hidden md:block absolute top-10 left-[60%] w-[80%] h-px bg-gradient-to-r from-copper-500/40 to-transparent" />
              )}
              <div className="inline-flex items-center justify-center w-20 h-20 rounded-xl bg-gradient-to-br from-copper-500 to-copper-700 shadow-copper-md mb-5 group-hover:shadow-copper-lg group-hover:-translate-y-0.5 transition-all duration-300">
                <step.icon className="w-9 h-9 text-white" />
              </div>
              <div className="font-body font-bold text-xs text-copper-500 uppercase tracking-widest mb-1">Step {i + 1}</div>
              <h3 className="font-body font-bold text-lg text-text-primary mb-2">{step.title}</h3>
              <p className="font-body text-text-secondary text-sm leading-relaxed max-w-xs mx-auto">{step.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ─────────────── CTA Banner ─────────────── */

function CtaBanner() {
  return (
    <section className="relative overflow-hidden"
      style={{ background: 'linear-gradient(135deg, #a84d28 0%, #c8663e 50%, #d97a52 100%)' }}>
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-30" />
      <div className="absolute -top-20 -right-20 w-64 h-64 rounded-full bg-white/5 blur-[80px] pointer-events-none" />

      <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-14 flex flex-col md:flex-row items-center justify-between gap-6 text-center md:text-left">
        <div>
          <h2 className="font-display text-2xl sm:text-3xl text-white">Ready to order?</h2>
          <p className="mt-1 font-body text-white/70 max-w-md text-sm">
            Your next delicious meal is just a few clicks away. Browse our menu and enjoy fresh food delivered to you.
          </p>
        </div>
        <Link to="/menu"
          className="inline-flex items-center gap-2 px-8 py-3.5 bg-white text-copper-600 font-body font-bold text-sm rounded-lg hover:shadow-xl hover:-translate-y-0.5 transition-all shrink-0 copper-press uppercase tracking-wider">
          Order Now <ArrowRight className="w-5 h-5" />
        </Link>
      </div>
    </section>
  );
}

/* ─────────────── Main Page ─────────────── */

export default function HomePage() {
  const [categories, setCategories] = useState([]);
  const [dishes, setDishes] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [loadingDishes, setLoadingDishes] = useState(true);

  useEffect(() => {
    let cancelled = false;
    async function fetchCategories() {
      try { const res = await categoryApi.list(); if (!cancelled) setCategories(extractItems(res)); }
      catch { if (!cancelled) setCategories([]); } finally { if (!cancelled) setLoadingCategories(false); }
    }
    async function fetchDishes() {
      try { const res = await menuApi.list({ size: 6 }); if (!cancelled) setDishes(extractItems(res)); }
      catch { if (!cancelled) setDishes([]); } finally { if (!cancelled) setLoadingDishes(false); }
    }
    fetchCategories();
    fetchDishes();
    return () => { cancelled = true; };
  }, []);

  return (
    <div className="animate-fade-in">
      <HeroSection />
      <CategoriesSection categories={categories} loading={loadingCategories} />
      <FeaturedDishesSection dishes={dishes} loading={loadingDishes} />
      <HowItWorksSection />
      <CtaBanner />
    </div>
  );
}
