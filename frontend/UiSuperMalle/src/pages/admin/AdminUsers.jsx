import { useState, useEffect, useCallback } from 'react';
import {
  Search, ChevronLeft, ChevronRight, Plus, Pencil, Trash2, RefreshCw,
  Shield, ShieldOff, X, AlertCircle, KeyRound, Loader2,
  Eye, EyeOff, Sparkles,
  User, Users,
} from 'lucide-react';
import { adminUserApi } from '../../api/endpoints';
import { extractPage } from '../../api/helpers';

const PAGE_SIZE = 15;

const ROLE_OPTIONS = ['CUSTOMER', 'ADMIN', 'DELIVERY'];
const EMPTY_USER = { name: '', email: '', phone: '', role: 'CUSTOMER', isActive: true };

function ErrorBanner({ message, onDismiss }) {
  return (
    <div className="flex items-center gap-3 bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl px-5 py-4">
      <AlertCircle className="w-5 h-5 shrink-0" />
      <p className="text-sm flex-1">{message}</p>
      {onDismiss && <button onClick={onDismiss} className="p-1 rounded-lg hover:bg-red-500/10 transition"><X className="w-4 h-4" /></button>}
    </div>
  );
}

function SkeletonRow() {
  return (
    <tr className="animate-pulse">
      {Array.from({ length: 7 }).map((_, i) => (
        <td key={i} className="px-4 py-4"><div className="h-4 bg-bg-hover rounded w-24" /></td>
      ))}
    </tr>
  );
}

function UserFormModal({ user, onClose, onSaved }) {
  const isEdit = !!user;
  const [form, setForm] = useState(isEdit
    ? { name: user.name || '', email: user.email || '', phone: user.phone || '', role: user.role || 'CUSTOMER', isActive: user.isActive ?? true }
    : { ...EMPTY_USER });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [showPw, setShowPw] = useState(false);
  const [initialPassword, setInitialPassword] = useState(null);

  const generatePassword = () => {
    const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lower = 'abcdefghijklmnopqrstuvwxyz';
    const digits = '0123456789';
    const special = '!@#$%^&*()_+-=[]{}|;:,.<>?';
    const all = upper + lower + digits + special;
    let pw = '';
    pw += upper[Math.floor(Math.random() * upper.length)];
    pw += lower[Math.floor(Math.random() * lower.length)];
    pw += digits[Math.floor(Math.random() * digits.length)];
    pw += special[Math.floor(Math.random() * special.length)];
    for (let i = 4; i < 14; i++) {
      pw += all[Math.floor(Math.random() * all.length)];
    }
    pw = pw.split('').sort(() => Math.random() - 0.5).join('');
    setForm(f => ({ ...f, password: pw }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      if (isEdit) {
        await adminUserApi.update(user.id, form);
        onSaved();
      } else {
        const res = await adminUserApi.create(form);
        if (res.data?.initialPassword) {
          setInitialPassword(res.data.initialPassword);
        } else {
          onSaved();
        }
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save user');
    } finally {
      setSaving(false);
    }
  };

  if (initialPassword) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => { setInitialPassword(null); onSaved(); }} />
        <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-md p-6 text-center">
          <div className="mx-auto w-14 h-14 flex items-center justify-center rounded-full bg-emerald-500/10 mb-4">
            <KeyRound className="w-7 h-7 text-emerald-500" />
          </div>
          <h2 className="text-lg font-bold text-text-primary mb-2">User Created</h2>
          <p className="text-sm text-text-secondary mb-4">
            Share this initial password with the user. They should change it on first login.
          </p>
          <div className="bg-bg-hover rounded-xl px-4 py-3 mb-5">
            <code className="text-lg font-mono font-bold text-copper-500 select-all">{initialPassword}</code>
          </div>
          <button onClick={() => { setInitialPassword(null); onSaved(); }}
            className="w-full px-5 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-400 transition">
            Done
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-bold text-text-primary">{isEdit ? 'Edit User' : 'Create User'}</h2>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-bg-hover transition text-text-dim"><X className="w-5 h-5" /></button>
        </div>
        {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}
        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Name</label>
            <input type="text" required value={form.name}
              onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))}
              className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition" />
          </div>
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Email</label>
            <input type="email" required value={form.email}
              onChange={(e) => setForm(f => ({ ...f, email: e.target.value }))}
              className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition" />
          </div>
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Phone</label>
            <input type="tel" value={form.phone}
              onChange={(e) => setForm(f => ({ ...f, phone: e.target.value }))}
              className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition" />
          </div>
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Role</label>
            <select value={form.role}
              onChange={(e) => setForm(f => ({ ...f, role: e.target.value }))}
              className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition">
              {ROLE_OPTIONS.map(r => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>
          {!isEdit && (
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">Password</label>
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
                  <input type={showPw ? 'text' : 'password'} value={form.password || ''}
                    onChange={(e) => setForm(f => ({ ...f, password: e.target.value }))}
                    placeholder="Leave empty to auto-generate"
                    className="w-full rounded-xl border border-border-subtle bg-bg-surface pl-10 pr-10 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition" />
                  <button type="button" onClick={() => setShowPw(!showPw)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-text-dim hover:text-text-secondary transition-colors">
                    {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
                <button type="button" onClick={generatePassword}
                  title="Generate secure password"
                  className="px-3 py-2.5 rounded-xl border border-border-subtle bg-bg-surface text-text-secondary hover:bg-bg-hover transition hover:text-copper-500">
                  <Sparkles className="w-4 h-4" />
                </button>
              </div>
              <p className="text-xs text-text-dim mt-1">
                {form.password ? 'Custom password set' : 'A secure 14-char password will be auto-generated'}
              </p>
            </div>
          )}
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose}
              className="px-4 py-2.5 text-sm font-medium text-text-secondary bg-bg-surface hover:bg-bg-hover rounded-xl transition">Cancel</button>
            <button type="submit" disabled={saving}
              className="px-5 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-400 transition disabled:opacity-50 flex items-center gap-2">
              {saving && <Loader2 className="w-4 h-4 animate-spin" />}
              {isEdit ? 'Save Changes' : 'Create User'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function ResetPasswordModal({ user, onClose, onSaved }) {
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (password.length < 8 || !/[A-Z]/.test(password) || !/[a-z]/.test(password) || !/[0-9]/.test(password) || !/[^A-Za-z0-9]/.test(password)) {
      setError('Password must be 8+ chars with uppercase, lowercase, number, and special character');
      return;
    }
    if (password !== confirm) {
      setError('Passwords do not match');
      return;
    }
    setSaving(true);
    try {
      await adminUserApi.resetPassword(user.id, { newPassword: password });
      onSaved();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to reset password');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-bold text-text-primary">Reset Password</h2>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-bg-hover transition text-text-dim"><X className="w-5 h-5" /></button>
        </div>
        <p className="text-sm text-text-secondary mb-4">Resetting password for <span className="font-semibold text-text-primary">{user?.name}</span></p>
        {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}
        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">New Password</label>
            <div className="relative">
              <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
              <input type="password" required value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full rounded-xl border border-border-subtle bg-bg-surface pl-10 pr-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
                placeholder="New password" />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Confirm Password</label>
            <div className="relative">
              <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
              <input type="password" required value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                className="w-full rounded-xl border border-border-subtle bg-bg-surface pl-10 pr-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
                placeholder="Confirm password" />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose}
              className="px-4 py-2.5 text-sm font-medium text-text-secondary bg-bg-surface hover:bg-bg-hover rounded-xl transition">Cancel</button>
            <button type="submit" disabled={saving}
              className="px-5 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-400 transition disabled:opacity-50 flex items-center gap-2">
              {saving && <Loader2 className="w-4 h-4 animate-spin" />}
              Reset Password
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function DeleteConfirmModal({ user, onConfirm, onCancel, deleting }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onCancel} />
      <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-sm p-6 text-center">
        <div className="mx-auto w-12 h-12 flex items-center justify-center rounded-full bg-red-500/10 mb-4">
          <Trash2 className="w-6 h-6 text-red-500" />
        </div>
        <h3 className="text-lg font-bold text-text-primary">Delete User</h3>
        <p className="text-sm text-text-secondary mt-2">
          Are you sure you want to delete <span className="font-semibold text-text-primary">{user?.name}</span>?
          Their orders will be orphaned. This cannot be undone.
        </p>
        <div className="flex gap-3 mt-6">
          <button onClick={onCancel}
            className="flex-1 px-4 py-2.5 text-sm font-medium text-text-secondary bg-bg-surface hover:bg-bg-hover rounded-xl transition">Cancel</button>
          <button onClick={onConfirm} disabled={deleting}
            className="flex-1 px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white text-sm font-semibold rounded-xl transition disabled:opacity-50 flex items-center justify-center gap-2">
            {deleting && <Loader2 className="w-4 h-4 animate-spin" />}
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [resetPwUser, setResetPwUser] = useState(null);
  const [deletingUser, setDeletingUser] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [togglingId, setTogglingId] = useState(null);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE };
      if (search) params.search = search;
      const res = await adminUserApi.list(params);
      const p = extractPage(res);
      setUsers(p.items);
      setTotalPages(p.totalPages);
      setTotalElements(p.total);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load users');
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleToggleActive = async (u) => {
    setTogglingId(u.id);
    try {
      await adminUserApi.toggleActive(u.id);
      await fetchUsers();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to toggle user status');
    } finally {
      setTogglingId(null);
    }
  };

  const handleDelete = async () => {
    if (!deletingUser) return;
    setDeleting(true);
    try {
      await adminUserApi.delete(deletingUser.id);
      setDeletingUser(null);
      await fetchUsers();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to delete user');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-text-primary">Users</h1>
            <p className="mt-1 text-text-secondary">{totalElements} user{totalElements !== 1 ? 's' : ''} total</p>
          </div>
          <button onClick={() => { setEditingUser(null); setShowForm(true); }}
            className="inline-flex items-center gap-2 px-5 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-400 transition shadow-copper-sm">
            <Plus className="w-4 h-4" /> Add User
          </button>
        </div>

        {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

        <div className="relative">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
          <input type="text" value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0); }}
            placeholder="Search by name or email..."
            className="w-full sm:w-80 pl-10 pr-4 py-2.5 rounded-xl border border-border-subtle bg-bg-surface text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition" />
        </div>

        {loading ? (
          <div className="bg-bg-surface rounded-xl shadow-copper-sm overflow-hidden">
            <table className="w-full"><tbody>{Array.from({ length: 6 }).map((_, i) => <SkeletonRow key={i} />)}</tbody></table>
          </div>
        ) : users.length === 0 ? (
          <div className="bg-bg-surface rounded-xl shadow-copper-sm py-16 flex flex-col items-center text-text-dim">
            <Users className="w-12 h-12 mb-3" />
            <p className="text-sm font-medium">No users found</p>
          </div>
        ) : (
          <div className="bg-bg-surface rounded-xl shadow-copper-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-bg-hover text-text-secondary text-xs uppercase tracking-wide border-b border-border-subtle">
                    <th className="text-left px-4 py-3.5 font-medium">Name</th>
                    <th className="text-left px-4 py-3.5 font-medium">Email</th>
                    <th className="text-left px-4 py-3.5 font-medium">Phone</th>
                    <th className="text-center px-4 py-3.5 font-medium">Role</th>
                    <th className="text-center px-4 py-3.5 font-medium">Status</th>
                    <th className="text-center px-4 py-3.5 font-medium">Orders</th>
                    <th className="text-right px-4 py-3.5 font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-subtle">
                  {users.map((u) => (
                    <tr key={u.id} className="hover:bg-bg-hover transition">
                      <td className="px-4 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-full bg-copper-500/10 flex items-center justify-center">
                            <User className="w-4 h-4 text-copper-500" />
                          </div>
                          <span className="font-medium text-text-primary">{u.name}</span>
                        </div>
                      </td>
                      <td className="px-4 py-4 text-text-secondary">{u.email}</td>
                      <td className="px-4 py-4 text-text-secondary">{u.phone || '--'}</td>
                      <td className="px-4 py-4 text-center">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          u.role === 'ADMIN' ? 'bg-copper-500/10 text-copper-500' :
                          u.role === 'DELIVERY' ? 'bg-blue-500/10 text-blue-400' :
                          'bg-bg-hover text-text-secondary'
                        }`}>{u.role}</span>
                      </td>
                      <td className="px-4 py-4 text-center">
                        <button onClick={() => handleToggleActive(u)}
                          disabled={togglingId === u.id}
                          className="inline-flex items-center gap-1 group" title={u.isActive ? 'Active — click to suspend' : 'Suspended — click to activate'}>
                          {togglingId === u.id ? (
                            <RefreshCw className="w-4 h-4 text-text-dim animate-spin" />
                          ) : u.isActive ? (
                            <><Shield className="w-4 h-4 text-emerald-500" /><span className="text-xs font-medium text-emerald-500">Active</span></>
                          ) : (
                            <><ShieldOff className="w-4 h-4 text-red-400" /><span className="text-xs font-medium text-red-400">Suspended</span></>
                          )}
                        </button>
                      </td>
                      <td className="px-4 py-4 text-center text-text-secondary">{u.orderCount ?? 0}</td>
                      <td className="px-4 py-4 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button onClick={() => { setEditingUser(u); setShowForm(true); }}
                            className="p-2 rounded-lg hover:bg-copper-500/10 text-text-dim hover:text-copper-500 transition" title="Edit">
                            <Pencil className="w-4 h-4" />
                          </button>
                          <button onClick={() => setResetPwUser(u)}
                            className="p-2 rounded-lg hover:bg-copper-500/10 text-text-dim hover:text-copper-500 transition" title="Reset Password">
                            <KeyRound className="w-4 h-4" />
                          </button>
                          <button onClick={() => setDeletingUser(u)}
                            className="p-2 rounded-lg hover:bg-red-500/10 text-text-dim hover:text-red-500 transition" title="Delete">
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {totalPages > 1 && (
              <div className="flex items-center justify-between px-6 py-4 border-t border-border-subtle">
                <p className="text-sm text-text-secondary">Page {page + 1} of {totalPages}</p>
                <div className="flex items-center gap-2">
                  <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
                    className="p-2 rounded-lg border border-border-subtle bg-bg-surface text-text-secondary hover:bg-bg-hover transition disabled:opacity-40 disabled:cursor-not-allowed">
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
                    className="p-2 rounded-lg border border-border-subtle bg-bg-surface text-text-secondary hover:bg-bg-hover transition disabled:opacity-40 disabled:cursor-not-allowed">
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {showForm && (
        <UserFormModal
          user={editingUser}
          onClose={() => { setShowForm(false); setEditingUser(null); }}
          onSaved={() => { setShowForm(false); setEditingUser(null); fetchUsers(); }}
        />
      )}

      {resetPwUser && (
        <ResetPasswordModal
          user={resetPwUser}
          onClose={() => setResetPwUser(null)}
          onSaved={() => { setResetPwUser(null); fetchUsers(); }}
        />
      )}

      {deletingUser && (
        <DeleteConfirmModal
          user={deletingUser}
          onConfirm={handleDelete}
          onCancel={() => setDeletingUser(null)}
          deleting={deleting}
        />
      )}
    </div>
  );
}