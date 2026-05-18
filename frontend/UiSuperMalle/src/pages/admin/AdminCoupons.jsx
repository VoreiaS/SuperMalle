import { useState, useEffect } from 'react';
import { adminCouponApi, adminCategoryApi } from '../../api/endpoints';
import { formatPrice, formatDate, extractItems } from '../../api/helpers';
import {
  Plus,
  Pencil,
  Trash2,
  Tag,
  X,
  Loader2,
  AlertTriangle,
  RefreshCw,
  ToggleLeft,
  ToggleRight,
  Percent,
  DollarSign,
} from 'lucide-react';

const emptyForm = {
  code: '',
  description: '',
  discountType: 'PERCENTAGE',
  discountValue: '',
  minimumOrderAmount: '',
  maximumDiscountAmount: '',
  validFrom: '',
  validUntil: '',
  active: true,
  usageLimit: '',
  applicableCategoryIds: [],
};

export default function AdminCoupons() {
  const [coupons, setCoupons] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingCoupon, setEditingCoupon] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState(null);
  const [togglingId, setTogglingId] = useState(null);

  const fetchCoupons = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await adminCouponApi.list();
      const data = extractItems(res);
      setCoupons(data);
    } catch (err) {
      setError('Failed to load coupons.');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const res = await adminCategoryApi.list();
      const data = extractItems(res);
      setCategories(data);
    } catch {
      // non-critical
    }
  };

  useEffect(() => {
    fetchCoupons();
    fetchCategories();
  }, []);

  const openAddModal = () => {
    setEditingCoupon(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEditModal = (coupon) => {
    setEditingCoupon(coupon);
    setForm({
      code: coupon.code || '',
      description: coupon.description || '',
      discountType: coupon.discountType || 'PERCENTAGE',
      discountValue: coupon.discountValue != null ? String(coupon.discountValue) : '',
      minimumOrderAmount: coupon.minimumOrderAmount != null ? String(coupon.minimumOrderAmount) : '',
      maximumDiscountAmount: coupon.maximumDiscountAmount != null ? String(coupon.maximumDiscountAmount) : '',
      validFrom: coupon.validFrom ? coupon.validFrom.slice(0, 16) : '',
      validUntil: coupon.validUntil ? coupon.validUntil.slice(0, 16) : '',
      active: coupon.active ?? true,
      usageLimit: coupon.usageLimit != null ? String(coupon.usageLimit) : '',
      applicableCategoryIds: coupon.applicableCategoryIds || [],
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingCoupon(null);
    setForm(emptyForm);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        code: form.code.trim().toUpperCase(),
        description: form.description || undefined,
        discountType: form.discountType,
        discountValue: Number(form.discountValue),
        minimumOrderAmount: form.minimumOrderAmount ? Number(form.minimumOrderAmount) : undefined,
        maximumDiscountAmount: form.maximumDiscountAmount ? Number(form.maximumDiscountAmount) : undefined,
        validFrom: form.validFrom ? new Date(form.validFrom).toISOString() : undefined,
        validUntil: form.validUntil ? new Date(form.validUntil).toISOString() : undefined,
        active: form.active,
        usageLimit: form.usageLimit ? Number(form.usageLimit) : undefined,
        applicableCategoryIds: form.applicableCategoryIds.length > 0 ? form.applicableCategoryIds.map(Number) : undefined,
      };

      if (editingCoupon) {
        await adminCouponApi.update(editingCoupon.id, payload);
      } else {
        await adminCouponApi.create(payload);
      }

      closeModal();
      fetchCoupons();
    } catch (err) {
      setError(err?.response?.data?.message || (editingCoupon ? 'Failed to update coupon.' : 'Failed to create coupon.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await adminCouponApi.delete(deleteTarget.id);
      setDeleteTarget(null);
      fetchCoupons();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to delete coupon.');
    } finally {
      setDeleting(false);
    }
  };

  const handleToggleActive = async (coupon) => {
    setTogglingId(coupon.id);
    try {
      await adminCouponApi.update(coupon.id, {
        code: coupon.code,
        description: coupon.description || undefined,
        discountType: coupon.discountType,
        discountValue: coupon.discountValue,
        minimumOrderAmount: coupon.minimumOrderAmount ?? undefined,
        maximumDiscountAmount: coupon.maximumDiscountAmount ?? undefined,
        validFrom: coupon.validFrom || undefined,
        validUntil: coupon.validUntil || undefined,
        active: !coupon.active,
        usageLimit: coupon.usageLimit ?? undefined,
        applicableCategoryIds: coupon.applicableCategoryIds?.length > 0 ? coupon.applicableCategoryIds : undefined,
      });
      await fetchCoupons();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to toggle coupon status.');
    } finally {
      setTogglingId(null);
    }
  };

  const isExpired = (coupon) => {
    if (!coupon.validUntil) return false;
    return new Date(coupon.validUntil) < new Date();
  };

  const isNotYetValid = (coupon) => {
    if (!coupon.validFrom) return false;
    return new Date(coupon.validFrom) > new Date();
  };

  const formatDiscountValue = (coupon) => {
    if (coupon.discountType === 'PERCENTAGE') {
      return `${coupon.discountValue}%`;
    }
    return formatPrice(coupon.discountValue);
  };

  const toggleCategory = (catId) => {
    setForm((f) => ({
      ...f,
      applicableCategoryIds: f.applicableCategoryIds.includes(catId)
        ? f.applicableCategoryIds.filter((id) => id !== catId)
        : [...f.applicableCategoryIds, catId],
    }));
  };

  // --- Loading skeletons ---
  if (loading) {
    return (
      <div className="p-6 space-y-6">
        <div className="flex items-center justify-between">
          <div className="h-8 w-48 rounded-lg animate-shimmer bg-bg-hover" />
          <div className="h-10 w-36 rounded-xl animate-shimmer bg-bg-hover" />
        </div>
        <div className="bg-bg-surface rounded-xl shadow-copper-sm border border-border-subtle overflow-hidden">
          <div className="p-4 space-y-4">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-14 rounded-xl animate-shimmer bg-bg-hover" />
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 relative animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Coupons</h1>
          <p className="text-sm text-text-secondary mt-1">Manage discount coupons for your restaurant</p>
        </div>
        <button
          onClick={openAddModal}
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-copper-500 hover:bg-copper-600 text-white font-semibold rounded-xl shadow-copper-sm transition-colors cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          Add Coupon
        </button>
      </div>

      {/* Error banner */}
      {error && (
        <div className="flex items-center gap-2 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          {error}
          <button onClick={() => setError(null)} className="ml-auto text-red-400 hover:text-red-600 cursor-pointer">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Table */}
      {coupons.length === 0 ? (
        <div className="bg-bg-surface rounded-xl shadow-copper-sm border border-border-subtle py-20 flex flex-col items-center justify-center text-text-dim">
          <Tag className="w-12 h-12 mb-4 text-text-dim" />
          <p className="text-lg font-medium text-text-secondary">No coupons yet</p>
          <p className="text-sm mt-1">Create your first coupon to start offering discounts</p>
        </div>
      ) : (
        <div className="bg-bg-surface rounded-xl shadow-copper-sm border border-border-subtle overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border-subtle text-left text-text-secondary text-xs uppercase tracking-wider">
                  <th className="px-5 py-3.5 font-semibold">Code</th>
                  <th className="px-5 py-3.5 font-semibold">Discount</th>
                  <th className="px-5 py-3.5 font-semibold">Min Order</th>
                  <th className="px-5 py-3.5 font-semibold">Valid Period</th>
                  <th className="px-5 py-3.5 font-semibold">Usage</th>
                  <th className="px-5 py-3.5 font-semibold">Status</th>
                  <th className="px-5 py-3.5 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-subtle">
                {coupons.map((coupon) => {
                  const expired = isExpired(coupon);
                  const notYetValid = isNotYetValid(coupon);
                  const active = coupon.active && !expired && !notYetValid;
                  const toggling = togglingId === coupon.id;

                  return (
                    <tr
                      key={coupon.id}
                      className="hover:bg-copper-500/5 transition-colors"
                    >
                      <td className="px-5 py-3.5">
                        <span className="inline-block px-2.5 py-1 bg-bg-elevated text-white text-xs font-mono rounded-lg tracking-wider">
                          {coupon.code}
                        </span>
                      </td>
                      <td className="px-5 py-3.5">
                        <div className="flex items-center gap-1.5">
                          {coupon.discountType === 'PERCENTAGE' ? (
                            <Percent className="w-3.5 h-3.5 text-copper-500" />
                          ) : (
                            <DollarSign className="w-3.5 h-3.5 text-emerald-500" />
                          )}
                          <span className="font-semibold text-text-primary">
                            {formatDiscountValue(coupon)}
                          </span>
                          <span className="text-xs text-text-dim">
                            {coupon.discountType === 'PERCENTAGE' ? 'off' : 'off'}
                          </span>
                        </div>
                        {coupon.maximumDiscountAmount != null && coupon.discountType === 'PERCENTAGE' && (
                          <span className="text-xs text-text-dim">max {formatPrice(coupon.maximumDiscountAmount)}</span>
                        )}
                      </td>
                      <td className="px-5 py-3.5 text-text-secondary">
                        {coupon.minimumOrderAmount != null ? formatPrice(coupon.minimumOrderAmount) : '—'}
                      </td>
                      <td className="px-5 py-3.5 text-text-secondary text-xs">
                        {coupon.validFrom ? formatDate(coupon.validFrom) : '—'}
                        {' → '}
                        {coupon.validUntil ? formatDate(coupon.validUntil) : '—'}
                      </td>
                      <td className="px-5 py-3.5 text-text-secondary">
                        <span className="font-medium">{coupon.usedCount ?? 0}</span>
                        <span className="text-text-dim">/</span>
                        <span>{coupon.usageLimit ?? '∞'}</span>
                      </td>
                      <td className="px-5 py-3.5">
                        <button
                          onClick={() => handleToggleActive(coupon)}
                          disabled={toggling}
                          className="inline-flex items-center gap-1 group cursor-pointer"
                          title={active ? 'Click to deactivate' : expired ? 'Expired' : notYetValid ? 'Not yet valid' : 'Click to activate'}
                        >
                          {toggling ? (
                            <RefreshCw className="w-4 h-4 text-text-dim animate-spin" />
                          ) : active ? (
                            <>
                              <ToggleRight className="w-5 h-5 text-emerald-500 group-hover:text-emerald-500 transition" />
                              <span className="text-xs font-medium text-emerald-500">Active</span>
                            </>
                          ) : (
                            <>
                              <ToggleLeft className="w-5 h-5 text-text-dim group-hover:text-text-secondary transition" />
                              <span className={`text-xs font-medium ${expired ? 'text-red-500' : notYetValid ? 'text-blue-500' : 'text-text-dim'}`}>
                                {expired ? 'Expired' : notYetValid ? 'Scheduled' : 'Inactive'}
                              </span>
                            </>
                          )}
                        </button>
                      </td>
                      <td className="px-5 py-3.5">
                        <div className="flex items-center justify-end gap-1">
                          <button
                            onClick={() => openEditModal(coupon)}
                            className="p-2 rounded-lg text-text-dim hover:text-copper-500 hover:bg-copper-500/10 transition-colors cursor-pointer"
                            title="Edit coupon"
                          >
                            <Pencil className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => setDeleteTarget(coupon)}
                            className="p-2 rounded-lg text-text-dim hover:text-red-600 hover:bg-red-500/10 transition-colors cursor-pointer"
                            title="Delete coupon"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add / Edit Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-bg-surface z-10 flex items-center justify-between px-6 py-4 border-b border-border-subtle rounded-t-xl">
              <h2 className="text-lg font-bold text-text-primary">
                {editingCoupon ? 'Edit Coupon' : 'Add Coupon'}
              </h2>
              <button
                onClick={closeModal}
                className="p-1.5 rounded-lg hover:bg-bg-hover text-text-dim hover:text-text-secondary transition-colors cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {/* Code */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1">Coupon Code</label>
                <input
                  type="text"
                  required
                  value={form.code}
                  onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))}
                  placeholder="e.g. SUMMER25"
                  className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 placeholder-text-dim font-mono uppercase"
                />
              </div>

              {/* Description */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1">Description</label>
                <input
                  type="text"
                  value={form.description}
                  onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                  placeholder="Brief description of this coupon"
                  className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 placeholder-text-dim"
                />
              </div>

              {/* Discount Type + Value */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">Discount Type</label>
                  <select
                    value={form.discountType}
                    onChange={(e) => setForm((f) => ({ ...f, discountType: e.target.value }))}
                    className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 bg-bg-surface"
                  >
                    <option value="PERCENTAGE">Percentage</option>
                    <option value="FIXED">Fixed Amount</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">
                    Value {form.discountType === 'PERCENTAGE' ? '(%)' : '($)'}
                  </label>
                  <input
                    type="number"
                    required
                    min="0"
                    step={form.discountType === 'PERCENTAGE' ? '1' : '0.01'}
                    value={form.discountValue}
                    onChange={(e) => setForm((f) => ({ ...f, discountValue: e.target.value }))}
                    placeholder={form.discountType === 'PERCENTAGE' ? '20' : '5.00'}
                    className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 placeholder-text-dim"
                  />
                </div>
              </div>

              {/* Min Order + Max Discount */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">Min Order Amount ($)</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.minimumOrderAmount}
                    onChange={(e) => setForm((f) => ({ ...f, minimumOrderAmount: e.target.value }))}
                    placeholder="0.00"
                    className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 placeholder-text-dim"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">Max Discount ($)</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.maximumDiscountAmount}
                    onChange={(e) => setForm((f) => ({ ...f, maximumDiscountAmount: e.target.value }))}
                    placeholder="No limit"
                    className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 placeholder-text-dim"
                  />
                </div>
              </div>

              {/* Valid From + Valid Until */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">Valid From</label>
                  <input
                    type="datetime-local"
                    value={form.validFrom}
                    onChange={(e) => setForm((f) => ({ ...f, validFrom: e.target.value }))}
                    className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">Valid Until</label>
                  <input
                    type="datetime-local"
                    value={form.validUntil}
                    onChange={(e) => setForm((f) => ({ ...f, validUntil: e.target.value }))}
                    className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500"
                  />
                </div>
              </div>

              {/* Usage Limit + Active */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-1">Usage Limit</label>
                  <input
                    type="number"
                    min="1"
                    value={form.usageLimit}
                    onChange={(e) => setForm((f) => ({ ...f, usageLimit: e.target.value }))}
                    placeholder="Unlimited"
                    className="w-full px-4 py-2.5 border border-border-subtle rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 placeholder-text-dim"
                  />
                </div>
                <div className="flex items-end pb-1">
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={form.active}
                      onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
                      className="w-4 h-4 text-copper-500 border-border-subtle rounded focus:ring-copper-500/40"
                    />
                    <span className="text-sm font-medium text-text-primary">Active</span>
                  </label>
                </div>
              </div>

              {/* Applicable Categories */}
              {categories.length > 0 && (
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-2">Applicable Categories</label>
                  <div className="flex flex-wrap gap-2">
                    {categories.map((cat) => (
                      <button
                        key={cat.id}
                        type="button"
                        onClick={() => toggleCategory(cat.id)}
                        className={`inline-flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-medium transition cursor-pointer ${
                          form.applicableCategoryIds.includes(cat.id)
                            ? 'bg-copper-500/10 text-copper-500 ring-1 ring-copper-500/30'
                            : 'bg-bg-hover text-text-secondary hover:bg-bg-hover'
                        }`}
                      >
                        {cat.name}
                        {form.applicableCategoryIds.includes(cat.id) && (
                          <X className="w-3 h-3" />
                        )}
                      </button>
                    ))}
                  </div>
                  <p className="mt-1 text-xs text-text-dim">Leave empty to apply to all categories</p>
                </div>
              )}

              {/* Actions */}
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={closeModal}
                  className="px-5 py-2.5 text-sm font-medium text-text-primary bg-bg-hover hover:bg-bg-hover rounded-xl transition-colors cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="inline-flex items-center gap-2 px-5 py-2.5 text-sm font-semibold text-white bg-copper-500 hover:bg-copper-600 disabled:opacity-60 disabled:cursor-not-allowed rounded-xl transition-colors cursor-pointer"
                >
                  {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
                  {editingCoupon ? 'Update Coupon' : 'Create Coupon'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-sm mx-4 p-6 text-center">
            <div className="mx-auto w-12 h-12 flex items-center justify-center rounded-full bg-red-500/10 mb-4">
              <Trash2 className="w-6 h-6 text-red-400" />
            </div>
            <h3 className="text-lg font-bold text-text-primary">Delete Coupon</h3>
            <p className="text-sm text-text-secondary mt-2">
              Are you sure you want to delete coupon{' '}
              <span className="font-mono font-semibold text-text-primary">{deleteTarget.code}</span>?
              This action cannot be undone.
            </p>
            <div className="flex items-center justify-center gap-3 mt-6">
              <button
                onClick={() => setDeleteTarget(null)}
                className="px-5 py-2.5 text-sm font-medium text-text-primary bg-bg-hover hover:bg-bg-hover rounded-xl transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="inline-flex items-center gap-2 px-5 py-2.5 text-sm font-semibold text-white bg-red-600 hover:bg-red-700 disabled:opacity-60 disabled:cursor-not-allowed rounded-xl transition-colors cursor-pointer"
              >
                {deleting && <Loader2 className="w-4 h-4 animate-spin" />}
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
