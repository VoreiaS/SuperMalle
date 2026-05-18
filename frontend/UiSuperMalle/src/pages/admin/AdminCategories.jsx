import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plus,
  Pencil,
  Trash2,
  GripVertical,
  Image,
  X,
  Tag,
  Loader2,
  AlertCircle,
  ToggleLeft,
  ToggleRight,
  RefreshCw,
  UtensilsCrossed,
  Eye,
  Search,
  ExternalLink,
} from 'lucide-react';
import { adminCategoryApi, adminMenuApi } from '../../api/endpoints';
import { extractItems, extractPage, formatPrice } from '../../api/helpers';

const SKELETON_COUNT = 6;

const GRADIENTS = [
  'from-copper-500 to-amber-600',
  'from-amber-600 to-copper-700',
  'from-copper-600 to-amber-700',
  'from-amber-500 to-copper-600',
  'from-copper-500 to-amber-500',
  'from-amber-600 to-copper-600',
];

function getGradient(id) {
  return GRADIENTS[(id ?? 0) % GRADIENTS.length];
}

/* ─── Error Banner ──────────────────────────────────────────────── */
function ErrorBanner({ message, onDismiss }) {
  return (
    <div className="flex items-center gap-3 bg-red-50 border border-red-200 text-red-700 rounded-xl px-5 py-4">
      <AlertCircle className="w-5 h-5 shrink-0" />
      <p className="text-sm flex-1">{message}</p>
      {onDismiss && (
        <button onClick={onDismiss} className="p-1 rounded-lg hover:bg-red-100 transition">
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
}

/* ─── Modal ──────────────────────────────────────────────────────── */
function CategoryModal({ open, onClose, onSave, initial, loading }) {
  const [form, setForm] = useState({ name: '', description: '', imageUrl: '', active: true, sortOrder: 0 });

  useEffect(() => {
    if (open && initial) {
      setForm({
        name: initial.name ?? '',
        description: initial.description ?? '',
        imageUrl: initial.imageUrl ?? '',
        active: initial.active ?? true,
        sortOrder: initial.sortOrder ?? 0,
      });
    } else if (open) {
      setForm({ name: '', description: '', imageUrl: '', active: true, sortOrder: 0 });
    }
  }, [open, initial]);

  if (!open) return null;

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : name === 'sortOrder' ? Number(value) : value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const payload = {
      name: form.name,
      description: form.description || undefined,
      imageUrl: form.imageUrl || undefined,
      active: form.active,
      sortOrder: form.sortOrder,
    };
    onSave(payload);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-xl bg-bg-surface p-6 shadow-copper-sm">
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-text-primary">
            {initial ? 'Edit Category' : 'Add Category'}
          </h2>
          <button onClick={onClose} className="rounded-lg p-1 text-text-dim hover:bg-bg-hover hover:text-text-secondary transition">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-text-primary">Name</label>
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              required
              placeholder="Category name"
              className="w-full rounded-xl border border-border-subtle px-4 py-2.5 text-sm text-text-primary placeholder-text-dim focus:border-copper-500 focus:ring-2 focus:ring-copper-500/20 focus:outline-none transition"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-text-primary">Description</label>
            <textarea
              name="description"
              value={form.description}
              onChange={handleChange}
              rows={2}
              placeholder="Brief description..."
              className="w-full rounded-xl border border-border-subtle px-4 py-2.5 text-sm text-text-primary placeholder-text-dim focus:border-copper-500 focus:ring-2 focus:ring-copper-500/20 focus:outline-none transition resize-none"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-text-primary">Image URL</label>
            <input
              type="text"
              name="imageUrl"
              value={form.imageUrl}
              onChange={handleChange}
              placeholder="https://example.com/image.jpg"
              className="w-full rounded-xl border border-border-subtle px-4 py-2.5 text-sm text-text-primary placeholder-text-dim focus:border-copper-500 focus:ring-2 focus:ring-copper-500/20 focus:outline-none transition"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-text-primary">Sort Order</label>
              <input
                type="number"
                name="sortOrder"
                value={form.sortOrder}
                onChange={handleChange}
                min={0}
                className="w-full rounded-xl border border-border-subtle px-4 py-2.5 text-sm text-text-primary placeholder-text-dim focus:border-copper-500 focus:ring-2 focus:ring-copper-500/20 focus:outline-none transition"
              />
            </div>
            <div className="flex items-end pb-1">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  name="active"
                  checked={form.active}
                  onChange={handleChange}
                  className="w-4 h-4 text-copper-500 border-border-default rounded focus:ring-copper-500/40"
                />
                <span className="text-sm font-medium text-text-primary">Active</span>
              </label>
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl px-4 py-2.5 text-sm font-medium text-text-secondary hover:bg-bg-hover transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center gap-2 rounded-xl bg-copper-500 px-5 py-2.5 text-sm font-semibold text-white hover:bg-copper-600 disabled:opacity-50 transition"
            >
              {loading && <Loader2 size={16} className="animate-spin" />}
              {initial ? 'Save Changes' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

/* ─── Skeleton Card ──────────────────────────────────────────────── */
function SkeletonCard() {
  return (
    <div className="animate-shimmer rounded-lg bg-bg-hover">
      <div className="h-36 rounded-t-lg bg-bg-hover" />
      <div className="p-4 space-y-3">
        <div className="h-5 w-3/5 rounded-lg bg-bg-hover" />
        <div className="h-4 w-1/4 rounded-md bg-bg-hover" />
      </div>
    </div>
  );
}

/* ─── Delete Confirm Modal ──────────────────────────────────────── */
function DeleteConfirmModal({ category, onConfirm, onCancel, deleting }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-sm rounded-xl bg-bg-surface p-6 shadow-copper-sm text-center">
        <div className="mx-auto w-12 h-12 flex items-center justify-center rounded-full bg-red-500/10 mb-4">
          <Trash2 className="w-6 h-6 text-red-500" />
        </div>
        <h3 className="text-lg font-bold text-text-primary">Deactivate Category</h3>
        <p className="text-sm text-text-secondary mt-2">
          Are you sure you want to deactivate{' '}
          <span className="font-semibold text-text-primary">{category.name}</span>? It will be hidden from customers but can be reactivated.
        </p>
        <div className="flex items-center justify-center gap-3 mt-6">
          <button
            onClick={onCancel}
            className="flex-1 px-4 py-2.5 text-sm font-medium text-text-secondary bg-bg-hover hover:bg-bg-surface rounded-xl transition"
          >
            Cancel
          </button>
          <button
            onClick={onConfirm}
            disabled={deleting}
            className="flex-1 px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white text-sm font-semibold rounded-xl transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {deleting && <Loader2 className="w-4 h-4 animate-spin" />}
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}

/* ─── Category Card ──────────────────────────────────────────────── */
function CategoryCard({ category, onEdit, onDelete, onToggleActive, onViewItems, togglingId }) {
  const gradient = getGradient(category.id);
  const canDelete = (category.menuItemCount ?? 0) === 0;
  const toggling = togglingId === category.id;

  return (
    <div className="group rounded-xl bg-bg-surface shadow-copper-sm hover:shadow-copper-sm transition-shadow duration-200">
      {/* Image / gradient placeholder */}
      <div className={`relative h-36 rounded-t-xl bg-gradient-to-br ${gradient} flex items-center justify-center overflow-hidden`}>
        {category.imageUrl ? (
          <img
            src={category.imageUrl}
            alt={category.name}
            className="h-full w-full object-cover"
            onError={(e) => { e.target.style.display = 'none'; }}
          />
        ) : (
          <Image size={40} className="text-white/60" />
        )}

        {/* Sort order badge */}
        <span className="absolute top-3 left-3 inline-flex items-center gap-1 rounded-lg bg-bg-surface/90 px-2.5 py-1 text-xs font-semibold text-text-primary backdrop-blur-sm">
          <GripVertical size={12} />
          #{category.sortOrder ?? 0}
        </span>

        {/* Active status dot */}
        <button
          onClick={() => onToggleActive(category)}
          disabled={toggling}
          className="absolute top-3 right-3 cursor-pointer"
          title={category.active ? 'Active — click to deactivate' : 'Inactive — click to activate'}
        >
          {toggling ? (
            <RefreshCw className="w-4 h-4 text-white animate-spin" />
          ) : category.active ? (
            <span className="flex h-4 w-4 rounded-full bg-emerald-400 shadow-copper-sm shadow-emerald-300 ring-2 ring-white" />
          ) : (
            <span className="flex h-4 w-4 rounded-full bg-gray-400 ring-2 ring-white" />
          )}
        </button>
      </div>

      {/* Body */}
      <div className="p-4">
        <h3 className="truncate text-base font-semibold text-text-primary">{category.name}</h3>

        {category.description && (
          <p className="mt-1 text-xs text-text-secondary line-clamp-2">{category.description}</p>
        )}

        <div className="mt-2 flex items-center gap-2 flex-wrap">
          <span
            className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
              category.active
                ? 'bg-emerald-50 text-emerald-700'
                : 'bg-bg-hover text-text-secondary'
            }`}
          >
            {category.active ? 'Active' : 'Inactive'}
          </span>
          <span className="inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium bg-copper-500/10 text-copper-500">
            <UtensilsCrossed className="w-3 h-3" />
            {category.menuItemCount ?? 0} items
          </span>
        </div>

        {/* Actions */}
        <div className="mt-4 flex items-center gap-2 border-t border-border-subtle pt-3">
          <button
            onClick={() => onViewItems(category)}
            className="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium text-text-secondary hover:bg-copper-500/10 hover:text-copper-500 transition"
          >
            <Eye size={14} />
            View Items
          </button>
          <button
            onClick={() => onEdit(category)}
            className="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium text-text-secondary hover:bg-copper-500/10 hover:text-copper-500 transition"
          >
            <Pencil size={14} />
            Edit
          </button>
          <button
            onClick={() => onDelete(category)}
            disabled={!canDelete}
            title={!canDelete ? 'Cannot delete — category has menu items' : 'Delete category'}
            className={`inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition ${
              canDelete
                ? 'text-text-secondary hover:bg-red-50 hover:text-red-500'
                : 'text-text-dim cursor-not-allowed'
            }`}
          >
            <Trash2 size={14} />
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}

/* ─── Empty State ────────────────────────────────────────────────── */
function EmptyState({ onAdd }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-border-subtle py-20">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-copper-500/10">
        <Tag size={28} className="text-copper-500" />
      </div>
      <h3 className="text-lg font-semibold text-text-primary">No categories yet</h3>
      <p className="mt-1 text-sm text-text-secondary">Create your first category to get started.</p>
      <button
        onClick={onAdd}
        className="mt-5 inline-flex items-center gap-2 rounded-xl bg-copper-500 px-5 py-2.5 text-sm font-semibold text-white hover:bg-copper-600 transition"
      >
        <Plus size={18} />
        Add Category
      </button>
    </div>
  );
}

/* ─── Category Items Modal ────────────────────────────────────────── */
function CategoryItemsModal({ category, onClose }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [removingId, setRemovingId] = useState(null);
  const navigate = useNavigate();

  const fetchItems = async () => {
    setLoading(true);
    try {
      const res = await adminMenuApi.list({ categoryId: category.id, page: 0, size: 100 });
      const p = extractPage(res);
      setItems(p.items);
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchItems(); }, [category.id]);

  const handleRemove = async (item) => {
    setRemovingId(item.id);
    try {
      await adminMenuApi.update(item.id, { ...item, categoryId: null });
      await fetchItems();
    } catch {} finally {
      setRemovingId(null);
    }
  };

  const handleAddNew = () => {
    navigate(`/admin/menu?categoryId=${category.id}`);
  };

  const filtered = search
    ? items.filter((i) => i.name?.toLowerCase().includes(search.toLowerCase()))
    : items;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-2xl max-h-[80vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border-subtle shrink-0">
          <div>
            <h2 className="text-lg font-bold text-text-primary">{category.name}</h2>
            <p className="text-sm text-text-secondary">Dishes in this category</p>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-bg-hover transition text-text-dim">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Search + Add */}
        <div className="px-6 py-3 flex items-center gap-3 border-b border-border-subtle shrink-0">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
            <input type="text" value={search} onChange={(e) => setSearch(e.target.value)}
              placeholder="Search dishes..." 
              className="w-full pl-9 pr-3 py-2 rounded-lg border border-border-subtle bg-bg-surface text-sm text-text-primary focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition" />
          </div>
          <button onClick={handleAddNew}
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-copper-500 text-white text-sm font-semibold rounded-lg hover:bg-copper-400 transition whitespace-nowrap">
            <Plus className="w-4 h-4" /> Add Dish
          </button>
        </div>

        {/* Items list */}
        <div className="flex-1 overflow-y-auto px-6 py-4">
          {loading ? (
            <div className="space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-12 bg-bg-hover rounded-lg animate-pulse" />
              ))}
            </div>
          ) : filtered.length === 0 ? (
            <div className="py-12 text-center text-text-dim">
              <UtensilsCrossed className="w-10 h-10 mx-auto mb-2" />
              <p className="text-sm font-medium">{search ? 'No matching dishes' : 'No dishes in this category'}</p>
            </div>
          ) : (
            <div className="space-y-2">
              {filtered.map((item) => (
                <div key={item.id} className="flex items-center gap-3 p-3 rounded-lg bg-bg-hover group hover:bg-copper-500/5 transition">
                  {item.imageUrl ? (
                    <img src={item.imageUrl} alt={item.name} className="w-10 h-10 rounded-lg object-cover border border-border-subtle" />
                  ) : (
                    <div className="w-10 h-10 rounded-lg bg-copper-500/10 flex items-center justify-center">
                      <UtensilsCrossed className="w-5 h-5 text-copper-500" />
                    </div>
                  )}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-text-primary truncate">{item.name}</p>
                    <p className="text-xs text-text-secondary">{formatPrice(item.price)}{!item.available && <span className="ml-2 text-red-400">(unavailable)</span>}</p>
                  </div>
                  <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition">
                    <button onClick={() => navigate(`/admin/menu?edit=${item.id}`)}
                      className="p-1.5 rounded-lg hover:bg-copper-500/10 text-text-dim hover:text-copper-500 transition" title="Edit in Menu">
                      <ExternalLink className="w-4 h-4" />
                    </button>
                    <button onClick={() => handleRemove(item)} disabled={removingId === item.id}
                      className="p-1.5 rounded-lg hover:bg-red-500/10 text-text-dim hover:text-red-500 transition" title="Remove from category">
                      {removingId === item.id ? <Loader2 className="w-4 h-4 animate-spin" /> : <X className="w-4 h-4" />}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

/* ─── Main Page ───────────────────────────────────────────────────── */
export default function AdminCategories() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState(null);
  const [togglingId, setTogglingId] = useState(null);
  const [viewItemsCategory, setViewItemsCategory] = useState(null);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await adminCategoryApi.list();
      const data = extractItems(res);
      setCategories(data);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load categories.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const handleOpenCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const handleOpenEdit = (category) => {
    setEditing(category);
    setModalOpen(true);
  };

  const handleCloseModal = () => {
    setModalOpen(false);
    setEditing(null);
  };

  const handleSave = async (form) => {
    try {
      setSaving(true);
      setError(null);
      if (editing) {
        await adminCategoryApi.update(editing.id, form);
      } else {
        await adminCategoryApi.create(form);
      }
      handleCloseModal();
      await fetchCategories();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save category.');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    if ((deleteTarget.menuItemCount ?? 0) > 0) {
      setError('Cannot delete a category that has menu items. Remove or reassign items first.');
      setDeleteTarget(null);
      return;
    }
    setDeleting(true);
    try {
      await adminCategoryApi.delete(deleteTarget.id);
      setDeleteTarget(null);
      await fetchCategories();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to delete category.');
    } finally {
      setDeleting(false);
    }
  };

  const handleToggleActive = async (category) => {
    setTogglingId(category.id);
    try {
      await adminCategoryApi.update(category.id, {
        name: category.name,
        description: category.description || undefined,
        imageUrl: category.imageUrl || undefined,
        active: !category.active,
        sortOrder: category.sortOrder,
      });
      await fetchCategories();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to toggle category status.');
    } finally {
      setTogglingId(null);
    }
  };

  return (
    <div className="relative mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8 animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />

      {/* Header */}
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Categories</h1>
          <p className="mt-1 text-sm text-text-secondary">Manage your restaurant menu categories</p>
        </div>
        <button
          onClick={handleOpenCreate}
          className="inline-flex items-center gap-2 rounded-xl bg-copper-500 px-5 py-2.5 text-sm font-semibold text-white shadow-copper-sm hover:bg-copper-600 active:scale-[0.97] transition"
        >
          <Plus size={18} />
          Add Category
        </button>
      </div>

      {/* Error */}
      {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

      {/* Content */}
      {loading ? (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: SKELETON_COUNT }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      ) : categories.length === 0 ? (
        <EmptyState onAdd={handleOpenCreate} />
      ) : (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((cat) => (
            <CategoryCard
              key={cat.id}
              category={cat}
              onEdit={handleOpenEdit}
              onDelete={(c) => setDeleteTarget(c)}
              onToggleActive={handleToggleActive}
              onViewItems={(c) => setViewItemsCategory(c)}
              togglingId={togglingId}
            />
          ))}
        </div>
      )}

      {/* Modal */}
      <CategoryModal
        open={modalOpen}
        onClose={handleCloseModal}
        onSave={handleSave}
        initial={editing}
        loading={saving}
      />

      {/* Delete Confirm */}
      {deleteTarget && (
        <DeleteConfirmModal
          category={deleteTarget}
          onConfirm={handleDeleteConfirm}
          onCancel={() => setDeleteTarget(null)}
          deleting={deleting}
        />
      )}

      {/* View Items */}
      {viewItemsCategory && (
        <CategoryItemsModal
          category={viewItemsCategory}
          onClose={() => setViewItemsCategory(null)}
        />
      )}
    </div>
  );
}
