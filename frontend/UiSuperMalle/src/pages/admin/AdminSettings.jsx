import { useState, useEffect } from 'react';
import {
  Settings,
  Store,
  Phone,
  Mail,
  MapPin,
  Clock,
  DollarSign,
  Truck,
  Save,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Percent,
  Hash,
  Wallet,
  Construction,
} from 'lucide-react';
import { adminSettingsApi } from '../../api/endpoints';
import { formatPrice } from '../../api/helpers';

/* ──────────────────── Constants ──────────────────── */

const STORAGE_KEY = 'supermalle_admin_settings';

const DEFAULT_SETTINGS = {
  taxRate: '',
  deliveryCharge: '',
  freeDeliveryMinOrder: '',
  maxOrdersPerSlot: '',
  workingHoursStart: '',
  workingHoursEnd: '',
  currency: 'USD',
  restaurantName: '',
  restaurantPhone: '',
  restaurantEmail: '',
  restaurantAddress: '',
  minOrderAmount: '',
};

/* ──────────────────── localStorage helpers ──────────────────── */

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return { ...DEFAULT_SETTINGS, ...JSON.parse(raw) };
  } catch { /* ignore */ }
  return null;
}

function saveToStorage(data) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  } catch { /* ignore */ }
}

/* ──────────────────── Section Card ──────────────────── */

function SectionCard({ icon: Icon, title, children }) {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-sm p-6 space-y-5">
      <div className="flex items-center gap-3">
        <div className="flex items-center justify-center w-9 h-9 rounded-xl bg-copper-500 shadow-copper-sm">
          <Icon className="w-5 h-5 text-white" />
        </div>
        <h2 className="text-lg font-semibold text-text-primary">{title}</h2>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-5">{children}</div>
    </div>
  );
}

/* ──────────────────── Form Field ──────────────────── */

function FormField({ label, icon: Icon, type = 'text', value, onChange, placeholder, step, min }) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-xs font-medium text-text-secondary">{label}</label>
      <div className="relative">
        {Icon && (
          <Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
        )}
        <input
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          step={step}
          min={min}
          className={`w-full rounded-xl border border-border-subtle bg-bg-surface py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition ${
            Icon ? 'pl-10 pr-3' : 'px-3'
          }`}
        />
      </div>
    </div>
  );
}

/* ──────────────────── Skeleton ──────────────────── */

function SettingsSkeleton() {
  return (
    <div className="space-y-6">
      {Array.from({ length: 3 }).map((_, i) => (
        <div key={i} className="bg-bg-surface rounded-xl shadow-copper-sm p-6">
          <div className="flex items-center gap-3 mb-6">
            <div className="animate-shimmer h-9 w-9 rounded-xl bg-bg-hover" />
            <div className="animate-shimmer h-5 w-36 rounded bg-bg-hover" />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-5">
            {Array.from({ length: 4 }).map((_, j) => (
              <div key={j} className="space-y-2">
                <div className="animate-shimmer h-3 w-20 rounded bg-bg-hover" />
                <div className="animate-shimmer h-10 rounded-xl bg-bg-hover" />
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}

/* ──────────────────── Main Page ──────────────────── */

export default function AdminSettings() {
  const [form, setForm] = useState(DEFAULT_SETTINGS);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [usingFallback, setUsingFallback] = useState(false);

  /* ── Load settings ── */

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        // Try backend API first
        const res = await adminSettingsApi.list();
        if (!cancelled) {
          // Backend returns array of { key, value } or flat object
          const data = res.data;
          let mapped;
          if (Array.isArray(data)) {
            // Key-value array format: [{ key: 'taxRate', value: '8.5' }, ...]
            mapped = { ...DEFAULT_SETTINGS };
            data.forEach((item) => {
              if (item.key && item.key in DEFAULT_SETTINGS) {
                mapped[item.key] = item.value ?? '';
              }
            });
          } else if (data && typeof data === 'object') {
            // Flat object format: { taxRate: '8.5', ... }
            mapped = {
              taxRate: data.taxRate ?? '',
              deliveryCharge: data.deliveryCharge ?? '',
              freeDeliveryMinOrder: data.freeDeliveryMinOrder ?? '',
              maxOrdersPerSlot: data.maxOrdersPerSlot ?? '',
              workingHoursStart: data.workingHoursStart ?? '',
              workingHoursEnd: data.workingHoursEnd ?? '',
              currency: data.currency || 'USD',
              restaurantName: data.restaurantName ?? '',
              restaurantPhone: data.restaurantPhone ?? '',
              restaurantEmail: data.restaurantEmail ?? '',
              restaurantAddress: data.restaurantAddress ?? '',
              minOrderAmount: data.minOrderAmount ?? '',
            };
          } else {
            mapped = { ...DEFAULT_SETTINGS };
          }
          setForm(mapped);
          saveToStorage(mapped);
          setUsingFallback(false);
        }
      } catch {
        // Backend unavailable — fall back to localStorage
        if (!cancelled) {
          const stored = loadFromStorage();
          if (stored) {
            setForm(stored);
          }
          setUsingFallback(true);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, []);

  /* ── Update field ── */

  const update = (key) => (e) => {
    setForm((prev) => ({ ...prev, [key]: e.target.value }));
    if (success) setSuccess(null);
    if (error) setError(null);
  };

  /* ── Save ── */

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const payload = { ...form };
      // Convert numeric strings to numbers, keep empty as null
      const numericFields = [
        'taxRate',
        'deliveryCharge',
        'freeDeliveryMinOrder',
        'maxOrdersPerSlot',
        'minOrderAmount',
      ];
      numericFields.forEach((f) => {
        if (payload[f] === '' || payload[f] == null) {
          payload[f] = null;
        } else {
          payload[f] = Number(payload[f]);
        }
      });

      try {
        const res = await adminSettingsApi.update(payload);
        const data = res.data;
        let mapped;
        if (Array.isArray(data)) {
          mapped = { ...DEFAULT_SETTINGS };
          data.forEach((item) => {
            if (item.key && item.key in DEFAULT_SETTINGS) {
              mapped[item.key] = item.value ?? '';
            }
          });
        } else if (data && typeof data === 'object') {
          mapped = {
            taxRate: data.taxRate ?? '',
            deliveryCharge: data.deliveryCharge ?? '',
            freeDeliveryMinOrder: data.freeDeliveryMinOrder ?? '',
            maxOrdersPerSlot: data.maxOrdersPerSlot ?? '',
            workingHoursStart: data.workingHoursStart ?? '',
            workingHoursEnd: data.workingHoursEnd ?? '',
            currency: data.currency || 'USD',
            restaurantName: data.restaurantName ?? '',
            restaurantPhone: data.restaurantPhone ?? '',
            restaurantEmail: data.restaurantEmail ?? '',
            restaurantAddress: data.restaurantAddress ?? '',
            minOrderAmount: data.minOrderAmount ?? '',
          };
        } else {
          mapped = { ...form };
        }
        setForm(mapped);
        saveToStorage(mapped);
        setUsingFallback(false);
        setSuccess('Settings saved successfully!');
      } catch {
        // Backend save failed — save to localStorage instead
        saveToStorage(payload);
        setUsingFallback(true);
        setSuccess('Settings saved locally. Backend sync will be available when the server is connected.');
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save settings. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  /* ── Render ── */

  return (
    <div className="relative min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* ── Header ── */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-copper-500 shadow-copper-sm">
              <Settings className="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-text-primary">Settings</h1>
              <p className="text-sm text-text-secondary">
                Manage your restaurant configuration
              </p>
            </div>
          </div>
          <button
            onClick={handleSave}
            disabled={saving}
            className="inline-flex items-center gap-2 px-6 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-600 transition shadow-copper-sm disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {saving ? (
              <RefreshCw className="w-4 h-4 animate-spin" />
            ) : (
              <Save className="w-4 h-4" />
            )}
            Save Settings
          </button>
        </div>

        {/* ── Fallback notice ── */}
        {usingFallback && (
          <div className="flex items-start gap-3 bg-copper-500/10 border border-copper-500/20 text-copper-500 rounded-xl px-5 py-4">
            <Construction className="w-5 h-5 shrink-0 mt-0.5" />
            <div>
              <p className="text-sm font-medium">Backend settings sync unavailable</p>
              <p className="text-xs mt-0.5 text-copper-500">
                Settings are being saved locally in your browser. They will sync with the server once the backend settings endpoint is available.
              </p>
            </div>
          </div>
        )}

        {/* ── Success ── */}
        {success && (
          <div className="flex items-center gap-3 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-xl px-5 py-4">
            <CheckCircle2 className="w-5 h-5 shrink-0" />
            <p className="text-sm">{success}</p>
          </div>
        )}

        {/* ── Error ── */}
        {error && (
          <div className="flex items-center gap-3 bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl px-5 py-4">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <p className="text-sm">{error}</p>
          </div>
        )}

        {/* ── Content ── */}
        {loading ? (
          <SettingsSkeleton />
        ) : (
          <div className="space-y-6">
            {/* Restaurant Info */}
            <SectionCard icon={Store} title="Restaurant Info">
              <FormField
                label="Restaurant Name"
                icon={Store}
                value={form.restaurantName}
                onChange={update('restaurantName')}
                placeholder="SuperMalle"
              />
              <FormField
                label="Phone"
                icon={Phone}
                type="tel"
                value={form.restaurantPhone}
                onChange={update('restaurantPhone')}
                placeholder="+1 (555) 123-4567"
              />
              <FormField
                label="Email"
                icon={Mail}
                type="email"
                value={form.restaurantEmail}
                onChange={update('restaurantEmail')}
                placeholder="hello@supermalle.com"
              />
              <FormField
                label="Address"
                icon={MapPin}
                value={form.restaurantAddress}
                onChange={update('restaurantAddress')}
                placeholder="123 Main St, City, State"
              />
            </SectionCard>

            {/* Order Settings */}
            <SectionCard icon={DollarSign} title="Order Settings">
              <FormField
                label="Tax Rate (%)"
                icon={Percent}
                type="number"
                step="0.01"
                min="0"
                value={form.taxRate}
                onChange={update('taxRate')}
                placeholder="8.5"
              />
              <FormField
                label="Delivery Charge"
                icon={Truck}
                type="number"
                step="0.01"
                min="0"
                value={form.deliveryCharge}
                onChange={update('deliveryCharge')}
                placeholder="3.99"
              />
              <FormField
                label="Free Delivery Min Order"
                icon={Wallet}
                type="number"
                step="0.01"
                min="0"
                value={form.freeDeliveryMinOrder}
                onChange={update('freeDeliveryMinOrder')}
                placeholder="25.00"
              />
              <FormField
                label="Max Orders Per Slot"
                icon={Hash}
                type="number"
                min="1"
                value={form.maxOrdersPerSlot}
                onChange={update('maxOrdersPerSlot')}
                placeholder="20"
              />
              <FormField
                label="Min Order Amount"
                icon={DollarSign}
                type="number"
                step="0.01"
                min="0"
                value={form.minOrderAmount}
                onChange={update('minOrderAmount')}
                placeholder="10.00"
              />
              <FormField
                label="Currency"
                icon={DollarSign}
                value={form.currency}
                onChange={update('currency')}
                placeholder="USD"
              />
            </SectionCard>

            {/* Working Hours */}
            <SectionCard icon={Clock} title="Working Hours">
              <FormField
                label="Opening Time"
                icon={Clock}
                type="time"
                value={form.workingHoursStart}
                onChange={update('workingHoursStart')}
                placeholder="09:00"
              />
              <FormField
                label="Closing Time"
                icon={Clock}
                type="time"
                value={form.workingHoursEnd}
                onChange={update('workingHoursEnd')}
                placeholder="22:00"
              />
            </SectionCard>

            {/* Bottom save (for scroll) */}
            <div className="flex justify-end pt-2">
              <button
                onClick={handleSave}
                disabled={saving}
                className="inline-flex items-center gap-2 px-6 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-600 transition shadow-copper-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {saving ? (
                  <RefreshCw className="w-4 h-4 animate-spin" />
                ) : (
                  <Save className="w-4 h-4" />
                )}
                Save Settings
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
