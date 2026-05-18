import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Plus,
  Pencil,
  Trash2,
  Search,
  ChevronLeft,
  ChevronRight,
  X,
  RefreshCw,
  AlertCircle,
  UtensilsCrossed,
  Timer,
  ToggleLeft,
  ToggleRight,
  ChevronDown,
  ChevronUp,
  Flame,
  Leaf,
  Wheat,
} from 'lucide-react';
import { adminMenuApi, adminCategoryApi } from '../../api/endpoints';
import { formatPrice, extractPage } from '../../api/helpers';

/* ──────────────────── Constants ──────────────────── */

const PAGE_SIZE = 20;

const EMPTY_FORM = {
  name: '',
  description: '',
  price: '',
  categoryId: '',
  imageUrl: '',
  available: true,
  preparationTime: '',
  spiceLevel: '0',
  isVegetarian: false,
  isVegan: false,
  isGlutenFree: false,
  calories: '',
  customizations: [],
};

const SPICE_LABELS = ['None', 'Mild', 'Medium', 'Hot', 'Extra Hot'];

/* ──────────────────── Skeleton ──────────────────── */

function TableSkeleton() {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-sm overflow-hidden">
      <div className="divide-y divide-border-subtle">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="px-6 py-4 flex items-center gap-4">
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-40" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-24" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-16" />
            <div className="animate-shimmer h-6 bg-bg-hover rounded-full w-16" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-12" />
            <div className="ml-auto animate-shimmer h-8 bg-bg-hover rounded-lg w-20" />
          </div>
        ))}
      </div>
    </div>
  );
}

/* ──────────────────── Error Banner ──────────────────── */

function ErrorBanner({ message, onDismiss }) {
  return (
    <div className="flex items-center gap-3 bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl px-5 py-4">
      <AlertCircle className="w-5 h-5 shrink-0" />
      <p className="text-sm flex-1">{message}</p>
      {onDismiss && (
        <button onClick={onDismiss} className="p-1 rounded-lg hover:bg-red-500/10 transition">
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
}

/* ──────────────────── Customization Editor ──────────────────── */

function CustomizationEditor({ customizations, onChange }) {
  const addCustomization = () => {
    onChange([...customizations, { name: '', required: false, multiSelect: false, options: [] }]);
  };

  const removeCustomization = (idx) => {
    onChange(customizations.filter((_, i) => i !== idx));
  };

  const updateCustomization = (idx, key, value) => {
    const next = [...customizations];
    next[idx] = { ...next[idx], [key]: value };
    onChange(next);
  };

  const addOption = (cIdx) => {
    const next = [...customizations];
    next[cIdx] = { ...next[cIdx], options: [...next[cIdx].options, { name: '', priceModifier: 0 }] };
    onChange(next);
  };

  const removeOption = (cIdx, oIdx) => {
    const next = [...customizations];
    next[cIdx] = { ...next[cIdx], options: next[cIdx].options.filter((_, i) => i !== oIdx) };
    onChange(next);
  };

  const updateOption = (cIdx, oIdx, key, value) => {
    const next = [...customizations];
    const opts = [...next[cIdx].options];
    opts[oIdx] = { ...opts[oIdx], [key]: key === 'priceModifier' ? Number(value) : value };
    next[cIdx] = { ...next[cIdx], options: opts };
    onChange(next);
  };

  return (
    <div className="space-y-3">
      {customizations.map((cust, cIdx) => (
        <div key={cIdx} className="border border-border-subtle rounded-xl p-3 space-y-2 bg-bg-hover">
          <div className="flex items-center gap-2">
            <input
              type="text"
              placeholder="Customization name (e.g. Size)"
              value={cust.name}
              onChange={(e) => updateCustomization(cIdx, 'name', e.target.value)}
              className="flex-1 rounded-lg border border-border-subtle bg-bg-surface px-3 py-1.5 text-sm text-text-primary focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
            />
            <label className="flex items-center gap-1 text-xs text-text-secondary whitespace-nowrap">
              <input
                type="checkbox"
                checked={cust.required}
                onChange={(e) => updateCustomization(cIdx, 'required', e.target.checked)}
                className="w-3.5 h-3.5 text-copper-500 border-border-subtle rounded focus:ring-copper-500/40"
              />
              Required
            </label>
            <label className="flex items-center gap-1 text-xs text-text-secondary whitespace-nowrap">
              <input
                type="checkbox"
                checked={cust.multiSelect}
                onChange={(e) => updateCustomization(cIdx, 'multiSelect', e.target.checked)}
                className="w-3.5 h-3.5 text-copper-500 border-border-subtle rounded focus:ring-copper-500/40"
              />
              Multi
            </label>
            <button
              type="button"
              onClick={() => removeCustomization(cIdx)}
              className="p-1 rounded-lg hover:bg-red-500/10 text-text-dim hover:text-red-500 transition"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Options */}
          <div className="pl-4 space-y-1.5">
            {cust.options.map((opt, oIdx) => (
              <div key={oIdx} className="flex items-center gap-2">
                <input
                  type="text"
                  placeholder="Option name"
                  value={opt.name}
                  onChange={(e) => updateOption(cIdx, oIdx, 'name', e.target.value)}
                  className="flex-1 rounded-lg border border-border-subtle bg-bg-surface px-2.5 py-1.5 text-sm text-text-primary focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
                />
                <div className="flex items-center gap-1">
                  <span className="text-xs text-text-dim">$</span>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    value={opt.priceModifier || ''}
                    onChange={(e) => updateOption(cIdx, oIdx, 'priceModifier', e.target.value)}
                    className="w-20 rounded-lg border border-border-subtle bg-bg-surface px-2.5 py-1.5 text-sm text-text-primary focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
                  />
                </div>
                <button
                  type="button"
                  onClick={() => removeOption(cIdx, oIdx)}
                  className="p-1 rounded-lg hover:bg-red-500/10 text-text-dim hover:text-red-500 transition"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
            <button
              type="button"
              onClick={() => addOption(cIdx)}
              className="text-xs font-medium text-copper-500 hover:text-copper-400 transition"
            >
              + Add Option
            </button>
          </div>
        </div>
      ))}
      <button
        type="button"
        onClick={addCustomization}
        className="text-sm font-medium text-copper-500 hover:text-copper-400 transition"
      >
        + Add Customization
      </button>
    </div>
  );
}

/* ──────────────────── Menu Form Modal ──────────────────── */

function MenuFormModal({ item, categories, onClose, onSaved }) {
  const isEdit = !!item;
  const [form, setForm] = useState(
    isEdit
      ? {
          name: item.name || '',
          description: item.description || '',
          price: item.price ?? '',
          categoryId: item.categoryId || '',
          imageUrl: item.imageUrl || '',
          available: item.available ?? true,
          preparationTime: item.preparationTime ?? '',
          spiceLevel: String(item.spiceLevel ?? 0),
          isVegetarian: item.isVegetarian ?? false,
          isVegan: item.isVegan ?? false,
          isGlutenFree: item.isGlutenFree ?? false,
          calories: item.calories ?? '',
          customizations: (item.customizations || []).map((c) => ({
            name: c.name || '',
            required: c.required ?? false,
            multiSelect: c.multiSelect ?? false,
            options: (c.options || []).map((o) => ({
              name: o.name || '',
              priceModifier: o.priceModifier ?? 0,
            })),
          })),
        }
      : { ...EMPTY_FORM },
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const set = (key) => (e) => {
    const val = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((f) => ({ ...f, [key]: val }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    const payload = {
      name: form.name,
      description: form.description || undefined,
      price: Number(form.price),
      imageUrl: form.imageUrl || undefined,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      available: form.available,
      preparationTime: form.preparationTime ? Number(form.preparationTime) : undefined,
      spiceLevel: form.spiceLevel ? Number(form.spiceLevel) : undefined,
      isVegetarian: form.isVegetarian || undefined,
      isVegan: form.isVegan || undefined,
      isGlutenFree: form.isGlutenFree || undefined,
      calories: form.calories ? Number(form.calories) : undefined,
      customizations:
        form.customizations.length > 0
          ? form.customizations.map((c) => ({
              name: c.name,
              required: c.required,
              multiSelect: c.multiSelect,
              options: c.options.map((o) => ({
                name: o.name,
                priceModifier: Number(o.priceModifier) || 0,
              })),
            }))
          : undefined,
    };

    try {
      if (isEdit) {
        await adminMenuApi.update(item.id, payload);
      } else {
        await adminMenuApi.create(payload);
      }
      onSaved();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save menu item.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-bg-surface z-10 flex items-center justify-between px-6 py-4 border-b border-border-subtle rounded-t-xl">
          <h2 className="text-lg font-bold text-text-primary">
            {isEdit ? 'Edit Menu Item' : 'Add Menu Item'}
          </h2>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-bg-hover transition text-text-dim hover:text-text-secondary"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
          {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

          {/* Name */}
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Name</label>
            <input
              type="text"
              required
              value={form.name}
              onChange={set('name')}
              className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
              placeholder="e.g. Margherita Pizza"
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Description</label>
            <textarea
              value={form.description}
              onChange={set('description')}
              rows={3}
              className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition resize-none"
              placeholder="Brief description of the item..."
            />
          </div>

          {/* Price + Category */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">Price ($)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                required
                value={form.price}
                onChange={set('price')}
                className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
                placeholder="0.00"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">Category</label>
              <select
                value={form.categoryId}
                onChange={set('categoryId')}
                className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
              >
                <option value="">Select category...</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Image URL */}
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Image URL</label>
            <input
              type="url"
              value={form.imageUrl}
              onChange={set('imageUrl')}
              className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
              placeholder="https://..."
            />
          </div>

          {/* Prep Time + Spice Level */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">Prep Time (min)</label>
              <input
                type="number"
                min="0"
                value={form.preparationTime}
                onChange={set('preparationTime')}
                className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
                placeholder="e.g. 15"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">Spice Level</label>
              <select
                value={form.spiceLevel}
                onChange={set('spiceLevel')}
                className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
              >
                {SPICE_LABELS.map((label, idx) => (
                  <option key={idx} value={String(idx)}>
                    {label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Calories + Available */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">Calories</label>
              <input
                type="number"
                min="0"
                value={form.calories}
                onChange={set('calories')}
                className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
                placeholder="e.g. 450"
              />
            </div>
            <div className="flex items-end pb-1">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={form.available}
                  onChange={set('available')}
                  className="w-4 h-4 text-copper-500 border-border-subtle rounded focus:ring-copper-500/40"
                />
                <span className="text-sm font-medium text-text-primary">Available</span>
              </label>
            </div>
          </div>

          {/* Dietary Flags */}
          <div>
            <label className="block text-sm font-medium text-text-primary mb-2">Dietary</label>
            <div className="flex items-center gap-5">
              <label className="flex items-center gap-1.5 cursor-pointer">
                <input
                  type="checkbox"
                  checked={form.isVegetarian}
                  onChange={set('isVegetarian')}
                  className="w-4 h-4 text-emerald-500 border-border-subtle rounded focus:ring-emerald-500/40"
                />
                <Leaf className="w-3.5 h-3.5 text-emerald-500" />
                <span className="text-sm text-text-primary">Vegetarian</span>
              </label>
              <label className="flex items-center gap-1.5 cursor-pointer">
                <input
                  type="checkbox"
                  checked={form.isVegan}
                  onChange={set('isVegan')}
                  className="w-4 h-4 text-emerald-500 border-border-subtle rounded focus:ring-emerald-500/40"
                />
                <Leaf className="w-3.5 h-3.5 text-emerald-500" />
                <span className="text-sm text-text-primary">Vegan</span>
              </label>
              <label className="flex items-center gap-1.5 cursor-pointer">
                <input
                  type="checkbox"
                  checked={form.isGlutenFree}
                  onChange={set('isGlutenFree')}
                  className="w-4 h-4 text-copper-500 border-border-subtle rounded focus:ring-copper-500/40"
                />
                <Wheat className="w-3.5 h-3.5 text-copper-500" />
                <span className="text-sm text-text-primary">Gluten-Free</span>
              </label>
            </div>
          </div>

          {/* Customizations */}
          <div>
            <label className="block text-sm font-medium text-text-primary mb-2">Customizations</label>
            <CustomizationEditor
              customizations={form.customizations}
              onChange={(c) => setForm((f) => ({ ...f, customizations: c }))}
            />
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 text-sm font-medium text-text-secondary bg-bg-surface hover:bg-bg-hover rounded-xl transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-5 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-400 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {saving ? (
                <RefreshCw className="w-4 h-4 animate-spin" />
              ) : (
                <Plus className="w-4 h-4" />
              )}
              {isEdit ? 'Save Changes' : 'Add Item'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

/* ──────────────────── Delete Confirm Modal ──────────────────── */

function DeleteModal({ name, onConfirm, onCancel, deleting }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onCancel} />
      <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-sm p-6">
        <div className="flex items-center justify-center w-12 h-12 mx-auto bg-red-500/10 rounded-full mb-4">
          <Trash2 className="w-6 h-6 text-red-500" />
        </div>
        <h3 className="text-lg font-bold text-text-primary text-center mb-2">Delete Item</h3>
        <p className="text-sm text-text-secondary text-center mb-6">
          Are you sure you want to delete <span className="font-semibold text-text-primary">{name}</span>?
          This action cannot be undone.
        </p>
        <div className="flex gap-3">
          <button
            onClick={onCancel}
            className="flex-1 px-4 py-2.5 text-sm font-medium text-text-secondary bg-bg-surface hover:bg-bg-hover rounded-xl transition"
          >
            Cancel
          </button>
          <button
            onClick={onConfirm}
            disabled={deleting}
            className="flex-1 px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white text-sm font-semibold rounded-xl transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {deleting && <RefreshCw className="w-4 h-4 animate-spin" />}
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}

/* ──────────────────── Main Page ──────────────────── */

export default function AdminMenu() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [items, setItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState(searchParams.get('categoryId') || '');

  // Modal state
  const [showForm, setShowForm] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [deletingItem, setDeletingItem] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [togglingId, setTogglingId] = useState(null);

  /* ── Fetch categories once ── */

  useEffect(() => {
    adminCategoryApi
      .list()
      .then((res) => {
        const data = res.data;
        setCategories(Array.isArray(data) ? data : data?.items || []);
      })
      .catch(() => {});
  }, []);

  /* ── Fetch menu items ── */

  const fetchItems = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE };
      if (search) params.search = search;
      if (categoryFilter) params.categoryId = categoryFilter;
      const res = await adminMenuApi.list(params);
      const pageData = extractPage(res);
      setItems(pageData.items);
      setTotalPages(pageData.totalPages);
      setTotalElements(pageData.total);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load menu items.');
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [page, search, categoryFilter]);

  useEffect(() => {
    fetchItems();
  }, [fetchItems]);

  /* ── Handle query params from categories view ── */

  useEffect(() => {
    const editId = searchParams.get('edit');
    const catId = searchParams.get('categoryId');
    if (editId && items.length > 0) {
      const found = items.find(i => String(i.id) === editId);
      if (found) {
        setEditingItem(found);
        setShowForm(true);
        setSearchParams({}, { replace: true });
      }
    }
    if (catId) {
      setCategoryFilter(catId);
    }
  }, [searchParams, items]);

  /* ── Category name lookup ── */

  const catMap = {};
  categories.forEach((c) => {
    catMap[c.id] = c.name;
  });

  /* ── Toggle availability via full update ── */

  const handleToggle = async (item) => {
    setTogglingId(item.id);
    try {
      await adminMenuApi.update(item.id, { available: !item.available });
      await fetchItems();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to toggle availability.');
    } finally {
      setTogglingId(null);
    }
  };

  /* ── Delete ── */

  const handleDelete = async () => {
    if (!deletingItem) return;
    setDeleting(true);
    try {
      await adminMenuApi.delete(deletingItem.id);
      setDeletingItem(null);
      await fetchItems();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to delete item.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Noise texture overlay */}
        <div className="fixed inset-0 pointer-events-none opacity-[0.04]" style={{ backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.75' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E")` }} />

        {/* ── Header ── */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-text-primary">Menu Items</h1>
            <p className="mt-1 text-text-secondary">
              Manage your restaurant menu — {totalElements} item{totalElements !== 1 ? 's' : ''} total
            </p>
          </div>
          <button
            onClick={() => {
              setEditingItem(null);
              setShowForm(true);
            }}
            className="inline-flex items-center gap-2 px-5 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-400 transition shadow-copper-sm"
          >
            <Plus className="w-4 h-4" />
            Add Item
          </button>
        </div>

        {/* ── Error ── */}
        {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

        {/* ── Search + Category Filter ── */}
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
            <input
              type="text"
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              placeholder="Search by name..."
              className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-border-subtle bg-bg-surface text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
            />
          </div>
          <select
            value={categoryFilter}
            onChange={(e) => { setCategoryFilter(e.target.value); setPage(0); }}
            className="sm:w-48 rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
          >
            <option value="">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </div>

        {/* ── Table ── */}
        {loading ? (
          <TableSkeleton />
        ) : items.length === 0 ? (
          <div className="bg-bg-surface rounded-xl shadow-copper-sm py-16 flex flex-col items-center text-text-dim">
            <UtensilsCrossed className="w-12 h-12 mb-3 text-text-dim" />
            <p className="text-sm font-medium">No menu items found</p>
            <p className="text-xs mt-1">Add your first item to get started</p>
          </div>
        ) : (
          <div className="bg-bg-surface rounded-xl shadow-copper-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-bg-hover text-text-secondary text-xs uppercase tracking-wide border-b border-border-subtle">
                    <th className="text-left px-6 py-3.5 font-medium">Name</th>
                    <th className="text-left px-4 py-3.5 font-medium">Category</th>
                    <th className="text-left px-4 py-3.5 font-medium">Price</th>
                    <th className="text-center px-4 py-3.5 font-medium">Available</th>
                    <th className="text-center px-4 py-3.5 font-medium">Prep Time</th>
                    <th className="text-right px-6 py-3.5 font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-subtle">
                  {items.map((item) => (
                    <tr key={item.id} className="hover:bg-bg-hover transition">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          {item.imageUrl ? (
                            <img
                              src={item.imageUrl}
                              alt={item.name}
                              className="w-9 h-9 rounded-lg object-cover border border-border-subtle"
                            />
                          ) : (
                            <div className="w-9 h-9 rounded-lg bg-copper-500/10 flex items-center justify-center">
                              <UtensilsCrossed className="w-4 h-4 text-copper-500" />
                            </div>
                          )}
                          <div>
                            <span className="font-medium text-text-primary">{item.name}</span>
                            <div className="flex items-center gap-2 mt-0.5">
                              {item.isVegetarian && (
                                <span title="Vegetarian"><Leaf className="w-3 h-3 text-emerald-500" /></span>
                              )}
                              {item.isVegan && (
                                <span title="Vegan"><Leaf className="w-3 h-3 text-emerald-500" /></span>
                              )}
                              {item.isGlutenFree && (
                                <span title="Gluten-Free"><Wheat className="w-3 h-3 text-copper-500" /></span>
                              )}
                              {item.spiceLevel > 0 && (
                                <span title={`Spice: ${SPICE_LABELS[item.spiceLevel] || item.spiceLevel}`}>
                                  <Flame className={`w-3 h-3 ${item.spiceLevel >= 3 ? 'text-red-500' : 'text-copper-500'}`} />
                                </span>
                              )}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-4 py-4 text-text-secondary">{item.categoryName || catMap[item.categoryId] || '--'}</td>
                      <td className="px-4 py-4 font-semibold text-text-primary">{formatPrice(item.price)}</td>
                      <td className="px-4 py-4 text-center">
                        <button
                          onClick={() => handleToggle(item)}
                          disabled={togglingId === item.id}
                          className="inline-flex items-center gap-1 group"
                          title={item.available ? 'Click to disable' : 'Click to enable'}
                        >
                          {togglingId === item.id ? (
                            <RefreshCw className="w-5 h-5 text-text-dim animate-spin" />
                          ) : item.available ? (
                            <>
                              <ToggleRight className="w-5 h-5 text-emerald-500 group-hover:text-emerald-400 transition" />
                              <span className="text-xs font-medium text-emerald-500">Yes</span>
                            </>
                          ) : (
                            <>
                              <ToggleLeft className="w-5 h-5 text-text-dim group-hover:text-text-secondary transition" />
                              <span className="text-xs font-medium text-text-dim">No</span>
                            </>
                          )}
                        </button>
                      </td>
                      <td className="px-4 py-4 text-center">
                        {item.preparationTime ? (
                          <span className="inline-flex items-center gap-1 text-text-secondary">
                            <Timer className="w-3.5 h-3.5 text-text-dim" />
                            {item.preparationTime}m
                          </span>
                        ) : (
                          <span className="text-text-dim">--</span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button
                            onClick={() => {
                              setEditingItem(item);
                              setShowForm(true);
                            }}
                            className="p-2 rounded-lg hover:bg-copper-500/10 text-text-dim hover:text-copper-500 transition"
                            title="Edit"
                          >
                            <Pencil className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => setDeletingItem(item)}
                            className="p-2 rounded-lg hover:bg-red-500/10 text-text-dim hover:text-red-500 transition"
                            title="Delete"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between px-6 py-4 border-t border-border-subtle">
                <p className="text-sm text-text-secondary">
                  Page {page + 1} of {totalPages}
                </p>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="p-2 rounded-lg border border-border-subtle bg-bg-surface text-text-secondary hover:bg-bg-hover transition disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="p-2 rounded-lg border border-border-subtle bg-bg-surface text-text-secondary hover:bg-bg-hover transition disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* ── Modals ── */}
      {showForm && (
        <MenuFormModal
          item={editingItem}
          categories={categories}
          onClose={() => {
            setShowForm(false);
            setEditingItem(null);
          }}
          onSaved={() => {
            setShowForm(false);
            setEditingItem(null);
            fetchItems();
          }}
        />
      )}

      {deletingItem && (
        <DeleteModal
          name={deletingItem.name}
          onConfirm={handleDelete}
          onCancel={() => setDeletingItem(null)}
          deleting={deleting}
        />
      )}
    </div>
  );
}
