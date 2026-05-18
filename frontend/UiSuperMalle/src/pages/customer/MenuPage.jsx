import { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  Search, UtensilsCrossed, Clock, Plus, ChefHat, Star,
  X, ChevronLeft, ChevronRight, PackageOpen, Sparkles
} from 'lucide-react';
import { menuApi, categoryApi } from '../../api/endpoints';
import { extractItems, extractPage, formatPrice } from '../../api/helpers';
import useCartStore from '../../store/cartStore';

const PAGE_SIZE = 12;

const COPPER_GRADIENTS = [
  'from-copper-500/40 via-copper-600/20 to-bg-elevated',
  'from-amber-600/30 via-copper-500/20 to-bg-elevated',
  'from-copper-400/40 via-copper-700/20 to-bg-elevated',
  'from-copper-600/30 via-amber-600/20 to-bg-elevated',
  'from-copper-500/30 via-copper-400/20 to-bg-elevated',
  'from-copper-700/30 via-copper-500/20 to-bg-elevated',
  'from-amber-500/30 via-copper-600/20 to-bg-elevated',
  'from-copper-400/30 via-amber-700/20 to-bg-elevated',
];

function Skeleton({ className = '' }) {
  return <div className={`animate-shimmer rounded-lg ${className}`} />;
}

function CardSkeleton() {
  return (
    <div className="card-copper overflow-hidden">
      <Skeleton className="h-44 w-full !rounded-none" />
      <div className="p-5 space-y-3">
        <Skeleton className="h-5 w-3/4" />
        <Skeleton className="h-3 w-full" />
        <Skeleton className="h-3 w-2/3" />
        <div className="flex items-center justify-between pt-2">
          <Skeleton className="h-6 w-16" />
          <Skeleton className="h-9 w-24 rounded-lg" />
        </div>
      </div>
    </div>
  );
}

function MenuItemCard({ item, index, onAddToCart, addingId }) {
  const navigate = useNavigate();
  const gradient = COPPER_GRADIENTS[index % COPPER_GRADIENTS.length];
  const isAdding = addingId === item.id;
  const isUnavailable = !item.isAvailable;

  return (
    <div
      onClick={() => navigate(`/menu/${item.id}`)}
      className={`card-copper-hover cursor-pointer flex flex-col relative overflow-hidden group animate-slide-up ${
        isUnavailable ? 'opacity-60' : ''
      }`}
      style={{ animationDelay: `${index * 60}ms` }}
    >
      <div className={`h-44 bg-gradient-to-br ${gradient} flex items-center justify-center relative overflow-hidden`}>
        <div className="absolute inset-0 bg-noise pointer-events-none opacity-30" />
        <UtensilsCrossed className="w-14 h-14 text-copper-500/30 group-hover:text-copper-500/50 group-hover:scale-110 transition-all duration-500" />

        {isUnavailable ? (
          <span className="absolute top-3 right-3 inline-flex items-center gap-1 px-2.5 py-1 bg-bg-base/80 backdrop-blur-sm text-text-dim text-xs font-semibold rounded-full border border-border-subtle">
            <X className="w-3 h-3" />
            Unavailable
          </span>
        ) : (
          <span className="absolute top-3 right-3 inline-flex items-center gap-1 px-2.5 py-1 bg-emerald-500/20 backdrop-blur-sm text-emerald-400 text-xs font-semibold rounded-full border border-emerald-500/20">
            <Sparkles className="w-3 h-3" />
            Available
          </span>
        )}
      </div>

      <div className="p-5 flex flex-col flex-1">
        <h3 className="font-body font-bold text-text-primary text-lg leading-snug line-clamp-1 group-hover:text-copper-500 transition-colors">
          {item.name}
        </h3>

        {item.description && (
          <p className="text-sm text-text-secondary mt-1 line-clamp-2">{item.description}</p>
        )}

        <div className="flex items-center gap-3 mt-3 text-xs text-text-dim">
          {item.preparationTimeMinutes != null && (
            <span className="inline-flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              {item.preparationTimeMinutes} min
            </span>
          )}
          {item.categoryName && (
            <span className="inline-flex items-center gap-1">
              <ChefHat className="w-3.5 h-3.5" />
              {item.categoryName}
            </span>
          )}
        </div>

        <div className="mt-auto pt-4 flex items-center justify-between">
          <span className="text-xl font-bold text-copper-500 font-display tracking-wide">
            {formatPrice(item.price)}
          </span>

          <button
            onClick={(e) => { e.stopPropagation(); onAddToCart(item.id); }}
            disabled={isAdding || isUnavailable}
            className="btn-copper !px-4 !py-2 !text-xs !rounded-lg"
          >
            <Plus className="w-4 h-4" />
            {isAdding ? 'Adding...' : 'Add'}
          </button>
        </div>
      </div>
    </div>
  );
}

function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  const pages = useMemo(() => {
    const items = [];
    const maxVisible = 5;
    let start = Math.max(0, page - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages, start + maxVisible);
    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }
    for (let i = start; i < end; i++) items.push(i);
    return items;
  }, [page, totalPages]);

  return (
    <div className="flex items-center justify-center gap-1.5 mt-10">
      <button
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        className="inline-flex items-center justify-center w-10 h-10 rounded-lg text-text-secondary hover:bg-copper-500/10 hover:text-copper-500 disabled:opacity-30 disabled:cursor-not-allowed transition-all"
      >
        <ChevronLeft className="w-5 h-5" />
      </button>

      {pages.map((p) => (
        <button
          key={p}
          onClick={() => onPageChange(p)}
          className={`w-10 h-10 rounded-lg text-sm font-semibold transition-all ${
            p === page
              ? 'bg-copper-500 text-white shadow-copper-sm'
              : 'text-text-secondary hover:bg-copper-500/10 hover:text-copper-500'
          }`}
        >
          {p + 1}
        </button>
      ))}

      <button
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="inline-flex items-center justify-center w-10 h-10 rounded-lg text-text-secondary hover:bg-copper-500/10 hover:text-copper-500 disabled:opacity-30 disabled:cursor-not-allowed transition-all"
      >
        <ChevronRight className="w-5 h-5" />
      </button>
    </div>
  );
}

function EmptyState({ search, selectedCategory, categories }) {
  const categoryName = selectedCategory
    ? categories.find((c) => c.id === selectedCategory)?.name
    : null;

  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-20 h-20 rounded-xl bg-copper-500/10 flex items-center justify-center mb-5 border border-copper-500/20">
        <PackageOpen className="w-10 h-10 text-copper-500/50" />
      </div>
      <h3 className="text-xl font-bold text-text-primary mb-2">No dishes found</h3>
      <p className="text-text-secondary max-w-sm">
        {search
          ? `We couldn't find anything matching "${search}". Try a different search term.`
          : categoryName
            ? `There are no dishes in the "${categoryName}" category right now. Check back soon!`
            : 'Our chefs are cooking up something special. Check back soon for new additions!'}
      </p>
    </div>
  );
}

export default function MenuPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [categories, setCategories] = useState([]);
  const [items, setItems] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [total, setTotal] = useState(0);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [loadingItems, setLoadingItems] = useState(true);
  const [addingId, setAddingId] = useState(null);

  const addItem = useCartStore((s) => s.addItem);

  const selectedCategory = searchParams.get('category') || '';
  const page = parseInt(searchParams.get('page') || '0', 10);
  const [searchInput, setSearchInput] = useState(searchParams.get('search') || '');
  const [searchDebounced, setSearchDebounced] = useState(searchParams.get('search') || '');

  useEffect(() => {
    const timer = setTimeout(() => {
      setSearchDebounced(searchInput);
      setSearchParams(
        (prev) => {
          const next = new URLSearchParams(prev);
          if (searchInput) {
            next.set('search', searchInput);
          } else {
            next.delete('search');
          }
          next.delete('page');
          return next;
        },
        { replace: true }
      );
    }, 350);
    return () => clearTimeout(timer);
  }, [searchInput, setSearchParams]);

  useEffect(() => {
    let cancelled = false;
    async function fetchCategories() {
      try {
        const res = await categoryApi.list();
        if (!cancelled) setCategories(extractItems(res));
      } catch {
        if (!cancelled) setCategories([]);
      } finally {
        if (!cancelled) setLoadingCategories(false);
      }
    }
    fetchCategories();
    return () => { cancelled = true; };
  }, []);

  useEffect(() => {
    let cancelled = false;
    async function fetchMenu() {
      setLoadingItems(true);
      try {
        const params = { page, size: PAGE_SIZE };
        if (selectedCategory) params.categoryId = selectedCategory;
        if (searchDebounced) params.search = searchDebounced;
        const res = await menuApi.list(params);
        if (!cancelled) {
          const pageData = extractPage(res);
          setItems(pageData.items);
          setTotalPages(pageData.totalPages);
          setTotal(pageData.total);
        }
      } catch {
        if (!cancelled) {
          setItems([]);
          setTotalPages(0);
          setTotal(0);
        }
      } finally {
        if (!cancelled) setLoadingItems(false);
      }
    }
    fetchMenu();
    return () => { cancelled = true; };
  }, [selectedCategory, searchDebounced, page]);

  const handleCategorySelect = useCallback(
    (categoryId) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (categoryId && categoryId !== selectedCategory) {
          next.set('category', categoryId);
        } else {
          next.delete('category');
        }
        next.delete('page');
        return next;
      });
    },
    [selectedCategory, setSearchParams]
  );

  const handlePageChange = useCallback(
    (newPage) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (newPage > 0) {
          next.set('page', String(newPage));
        } else {
          next.delete('page');
        }
        return next;
      });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },
    [setSearchParams]
  );

  const handleAddToCart = useCallback(
    async (menuItemId) => {
      setAddingId(menuItemId);
      try {
        await addItem({ menuItemId, quantity: 1, customizations: [], specialInstructions: '' });
      } catch {
        /* cart store handles errors */
      } finally {
        setAddingId(null);
      }
    },
    [addItem]
  );

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />

      <section className="relative overflow-hidden border-b border-copper-500/10">
        <div className="absolute -top-32 -right-32 w-[500px] h-[500px] rounded-full bg-copper-500/8 blur-[140px] pointer-events-none" />
        <div className="absolute -bottom-32 -left-32 w-[400px] h-[400px] rounded-full bg-copper-400/5 blur-[120px] pointer-events-none" />

        <div className="absolute top-20 left-[15%] w-3 h-3 rounded bg-copper-500/20 animate-copper-float" />
        <div className="absolute bottom-32 right-[20%] w-2 h-2 rounded bg-copper-500/30" style={{ animation: 'copperFloat 7s ease-in-out infinite 1s' }} />

        <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-16 pb-12">
          <h1 className="font-display text-3xl sm:text-4xl lg:text-5xl text-text-primary leading-tight">
            Our <span className="text-gradient-copper">Menu</span>
          </h1>
          <p className="mt-2 text-text-secondary text-lg font-body">
            Explore our selection and find your next favorite dish
          </p>

          <div className="mt-6 max-w-xl relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-dim" />
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Search dishes..."
              className="input-copper !pl-12 !pr-12"
            />
            {searchInput && (
              <button
                onClick={() => setSearchInput('')}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-text-dim hover:text-text-primary transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            )}
          </div>
        </div>
      </section>

      <section className="bg-bg-surface/50 border-b border-border-subtle sticky top-0 z-20 backdrop-blur-md">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-2 py-4 overflow-x-auto scrollbar-hide -mx-4 px-4 sm:mx-0 sm:px-0">
            <button
              onClick={() => handleCategorySelect('')}
              className={`shrink-0 inline-flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200 ${
                !selectedCategory
                  ? 'bg-copper-500 text-white shadow-copper-sm'
                  : 'bg-bg-hover text-text-secondary hover:text-copper-500 hover:bg-copper-500/10'
              }`}
            >
              <UtensilsCrossed className="w-4 h-4" />
              All
            </button>

            {loadingCategories
              ? Array.from({ length: 5 }).map((_, i) => (
                  <div key={i} className="shrink-0 animate-shimmer bg-bg-hover h-10 w-24 rounded-lg" />
                ))
              : categories.map((cat) => (
                  <button
                    key={cat.id}
                    onClick={() => handleCategorySelect(cat.id)}
                    className={`shrink-0 inline-flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200 ${
                      selectedCategory === cat.id
                        ? 'bg-copper-500 text-white shadow-copper-sm'
                        : 'bg-bg-hover text-text-secondary hover:text-copper-500 hover:bg-copper-500/10'
                    }`}
                  >
                    {cat.name}
                  </button>
                ))}
          </div>
        </div>
      </section>

      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-8">
        <div className="flex items-center justify-between">
          <p className="text-sm text-text-dim">
            {loadingItems ? (
              <span className="inline-block w-32 h-4 animate-shimmer bg-bg-hover rounded" />
            ) : (
              <>
                Showing <span className="font-semibold text-text-primary">{items.length}</span>
                {total > 0 && (
                  <> of <span className="font-semibold text-text-primary">{total}</span></>
                )} dishes
                {selectedCategory && (
                  <> in <span className="font-semibold text-copper-500">{categories.find((c) => c.id === selectedCategory)?.name || 'Category'}</span></>
                )}
                {searchDebounced && (
                  <> matching <span className="font-semibold text-copper-500">"{searchDebounced}"</span></>
                )}
              </>
            )}
          </p>
        </div>
      </section>

      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {loadingItems ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {Array.from({ length: PAGE_SIZE }).map((_, i) => (
              <CardSkeleton key={i} />
            ))}
          </div>
        ) : items.length === 0 ? (
          <EmptyState search={searchDebounced} selectedCategory={selectedCategory} categories={categories} />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {items.map((item, i) => (
              <MenuItemCard
                key={item.id}
                item={item}
                index={i}
                onAddToCart={handleAddToCart}
                addingId={addingId}
              />
            ))}
          </div>
        )}

        {!loadingItems && items.length > 0 && (
          <Pagination page={page} totalPages={totalPages} onPageChange={handlePageChange} />
        )}
      </section>
    </div>
  );
}
