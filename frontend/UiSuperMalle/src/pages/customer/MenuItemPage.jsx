import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft, Minus, Plus, ShoppingCart, Clock, ChefHat,
  UtensilsCrossed, CheckSquare, Square, Loader2
} from 'lucide-react';
import { menuApi } from '../../api/endpoints';
import { formatPrice } from '../../api/helpers';
import useCartStore from '../../store/cartStore';

const GRADIENTS = [
  'from-copper-500/30 via-copper-600/20 to-bg-elevated',
  'from-amber-600/30 via-copper-500/20 to-bg-elevated',
  'from-copper-400/30 via-copper-700/20 to-bg-elevated',
  'from-copper-600/30 via-amber-600/20 to-bg-elevated',
  'from-copper-500/20 via-copper-400/20 to-bg-elevated',
];

function parseCustomizations(raw) {
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
  return [];
}

export default function MenuItemPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [selectedCustomizations, setSelectedCustomizations] = useState([]);
  const [specialInstructions, setSpecialInstructions] = useState('');
  const [adding, setAdding] = useState(false);

  const addItem = useCartStore((s) => s.addItem);

  useEffect(() => {
    let cancelled = false;
    async function fetchItem() {
      setLoading(true);
      try {
        const res = await menuApi.get(id);
        if (!cancelled) setItem(res.data);
      } catch {
        if (!cancelled) setError('Failed to load menu item');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    fetchItem();
    return () => { cancelled = true; };
  }, [id]);

  const customizations = parseCustomizations(item?.customizations);
  const gradient = GRADIENTS[parseInt(id, 10) % GRADIENTS.length] || GRADIENTS[0];

  const toggleCustomization = (label) => {
    setSelectedCustomizations((prev) =>
      prev.includes(label) ? prev.filter((c) => c !== label) : [...prev, label]
    );
  };

  const handleAddToCart = async () => {
    setAdding(true);
    try {
      await addItem({
        menuItemId: item.id,
        quantity,
        customizations: selectedCustomizations,
        specialInstructions,
      });
      navigate('/menu');
    } catch {
      /* cart store handles errors */
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-bg-base pt-20 animate-fade-in">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="animate-pulse space-y-6">
            <Skeleton className="h-8 w-32" />
            <Skeleton className="h-72 sm:h-96 rounded-xl" />
            <div className="space-y-4">
              <Skeleton className="h-7 w-3/4" />
              <Skeleton className="h-5 w-1/3" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-2/3" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !item) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center animate-fade-in">
        <div className="text-center max-w-md px-4">
          <div className="w-20 h-20 rounded-xl bg-copper-500/10 flex items-center justify-center mx-auto mb-5 border border-copper-500/20">
            <UtensilsCrossed className="w-10 h-10 text-copper-500/50" />
          </div>
          <h2 className="text-xl font-bold text-text-primary mb-2">Item Not Found</h2>
          <p className="text-text-secondary mb-6">{error || "We couldn't find this menu item."}</p>
          <button
            onClick={() => navigate('/menu')}
            className="btn-copper"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Menu
          </button>
        </div>
      </div>
    );
  }

  const isUnavailable = !item.isAvailable;

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />

      <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
        <button
          onClick={() => navigate('/menu')}
          className="inline-flex items-center gap-2 text-text-secondary hover:text-copper-500 font-body font-medium mb-6 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Menu
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="rounded-xl overflow-hidden border border-border-subtle bg-bg-surface shadow-copper-md">
            {item.imageUrl ? (
              <img
                src={item.imageUrl}
                alt={item.name}
                className="w-full h-72 sm:h-96 object-cover"
              />
            ) : (
              <div className={`w-full h-72 sm:h-96 bg-gradient-to-br ${gradient} flex items-center justify-center`}>
                <div className="absolute inset-0 bg-noise pointer-events-none opacity-20" />
                <UtensilsCrossed className="w-20 h-20 text-copper-500/30" />
              </div>
            )}
          </div>

          <div className="flex flex-col">
            {item.categoryName && (
              <span className="inline-flex items-center gap-1.5 self-start px-3 py-1 bg-copper-500/10 text-copper-500 text-xs font-semibold rounded-lg border border-copper-500/20 mb-3">
                <ChefHat className="w-3.5 h-3.5" />
                {item.categoryName}
              </span>
            )}

            <h1 className="font-display text-2xl sm:text-3xl text-text-primary leading-tight">
              {item.name}
            </h1>

            <div className="flex items-center gap-4 mt-3">
              <span className="text-2xl font-bold text-copper-500 font-display tracking-wide">
                {formatPrice(item.price)}
              </span>
              {item.preparationTimeMinutes != null && (
                <span className="inline-flex items-center gap-1 text-sm text-text-dim">
                  <Clock className="w-4 h-4" />
                  {item.preparationTimeMinutes} min
                </span>
              )}
            </div>

            {item.description && (
              <p className="mt-4 text-text-secondary leading-relaxed font-body">
                {item.description}
              </p>
            )}

            {isUnavailable && (
              <div className="mt-4 px-4 py-3 bg-copper-500/10 border border-copper-500/20 rounded-lg text-copper-500 text-sm font-medium">
                This item is currently unavailable.
              </div>
            )}

            {customizations.length > 0 && (
              <div className="mt-6">
                <h3 className="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-3 font-body">
                  Customizations
                </h3>
                <div className="space-y-2">
                  {customizations.map((opt, i) => {
                    const label = typeof opt === 'string' ? opt : opt.name || opt.label || `Option ${i + 1}`;
                    const isSelected = selectedCustomizations.includes(label);
                    return (
                      <button
                        key={i}
                        type="button"
                        onClick={() => toggleCustomization(label)}
                        disabled={isUnavailable}
                        className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg border text-left transition-all ${
                          isSelected
                            ? 'border-copper-500/50 bg-copper-500/10 text-copper-500'
                            : 'border-border-subtle bg-bg-surface text-text-primary hover:border-copper-500/30 hover:bg-copper-500/5'
                        } ${isUnavailable ? 'opacity-50 cursor-not-allowed' : ''}`}
                      >
                        {isSelected ? (
                          <CheckSquare className="w-5 h-5 text-copper-500 shrink-0" />
                        ) : (
                          <Square className="w-5 h-5 text-text-dim shrink-0" />
                        )}
                        <span className="text-sm font-medium">{label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            <div className="mt-6">
              <label className="block text-sm font-semibold text-text-secondary uppercase tracking-wide mb-2 font-body">
                Special Instructions
              </label>
              <textarea
                value={specialInstructions}
                onChange={(e) => setSpecialInstructions(e.target.value)}
                disabled={isUnavailable}
                rows={3}
                placeholder="Any allergies, preferences, or special requests..."
                className="input-copper resize-none"
              />
            </div>

            <div className="mt-6 flex items-center gap-4">
              <span className="text-sm font-semibold text-text-secondary font-body">Quantity</span>
              <div className="inline-flex items-center gap-1 bg-bg-hover rounded-lg p-1">
                <button
                  onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                  disabled={isUnavailable || quantity <= 1}
                  className="w-10 h-10 flex items-center justify-center rounded-lg bg-bg-surface text-text-secondary hover:text-copper-500 hover:bg-copper-500/10 disabled:opacity-30 disabled:cursor-not-allowed shadow-sm transition-all active:scale-95"
                >
                  <Minus className="w-4 h-4" />
                </button>
                <span className="w-10 text-center font-bold text-text-primary">
                  {quantity}
                </span>
                <button
                  onClick={() => setQuantity((q) => Math.min(99, q + 1))}
                  disabled={isUnavailable}
                  className="w-10 h-10 flex items-center justify-center rounded-lg bg-bg-surface text-text-secondary hover:text-copper-500 hover:bg-copper-500/10 disabled:opacity-30 disabled:cursor-not-allowed shadow-sm transition-all active:scale-95"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            </div>

            <button
              onClick={handleAddToCart}
              disabled={adding || isUnavailable}
              className="mt-6 w-full btn-copper !py-4 !text-base !font-bold !rounded-lg shadow-copper-md hover:shadow-copper-lg"
            >
              {adding ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Adding...
                </>
              ) : (
                <>
                  <ShoppingCart className="w-5 h-5" />
                  Add to Cart — {formatPrice(item.price * quantity)}
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function Skeleton({ className = '' }) {
  return <div className={`animate-shimmer rounded-lg bg-bg-hover ${className}`} />;
}
