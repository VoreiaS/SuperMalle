import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Mail, Phone, Lock, Save, LogOut, Calendar, Shield } from 'lucide-react';
import useAuthStore from '../../store/authStore';
import { userApi } from '../../api/endpoints';
import { updateProfileSchema, changePasswordSchema } from '../../lib/validation';

export default function ProfilePage() {
  const { user, setUser, logout } = useAuthStore();
  const navigate = useNavigate();
  const [tab, setTab] = useState('profile');
  const [form, setForm] = useState({
    name: user?.name || '',
    email: user?.email || '',
    phone: user?.phone || '',
  });

  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});
  const [msg, setMsg] = useState('');
  const [msgType, setMsgType] = useState('success');

  useEffect(() => {
    if (user) {
      setForm({ name: user.name || '', email: user.email || '', phone: user.phone || '' });
    }
  }, [user]);

  const showMessage = (text, type = 'success') => {
    setMsg(text);
    setMsgType(type);
    setTimeout(() => setMsg(''), 4000);
  };

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setErrors({});
    setMsg('');

    const result = updateProfileSchema.safeParse(form);
    if (!result.success) {
      const fieldErrors = {};
      result.error.errors.forEach((err) => {
        fieldErrors[err.path[0]] = err.message;
      });
      setErrors(fieldErrors);
      showMessage('Please fix the highlighted fields', 'error');
      return;
    }

    setSaving(true);
    try {
      const res = await userApi.updateProfile(result.data);
      const updatedUser = res.data;
      const normalized = { id: updatedUser.userId, name: updatedUser.name, email: updatedUser.email, role: updatedUser.role };
      localStorage.setItem('user', JSON.stringify(normalized));
      setUser(normalized);
      showMessage('Profile updated successfully!');
    } catch (err) {
      showMessage(err.response?.data?.message || 'Profile update failed', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setErrors({});
    setMsg('');

    const result = changePasswordSchema.safeParse(pwForm);
    if (!result.success) {
      const fieldErrors = {};
      result.error.errors.forEach((err) => {
        fieldErrors[err.path[0]] = err.message;
      });
      setErrors(fieldErrors);
      showMessage('Please fix the highlighted fields', 'error');
      return;
    }

    setSaving(true);
    try {
      await userApi.updatePassword({
        currentPassword: result.data.currentPassword,
        newPassword: result.data.newPassword,
      });
      showMessage('Password changed successfully!');
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      showMessage(err.response?.data?.message || 'Password change failed', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  };

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />

      <div className="relative max-w-2xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-copper-500 rounded-xl flex items-center justify-center shadow-copper-sm">
              <User className="w-5 h-5 text-white" />
            </div>
            <h1 className="font-display text-2xl text-text-primary">My Profile</h1>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-red-400 bg-red-500/10 border border-red-500/20 rounded-lg hover:bg-red-500/20 transition-all"
          >
            <LogOut className="w-4 h-4" /> Logout
          </button>
        </div>

        <div className="card-copper p-6 mb-6">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 bg-gradient-to-br from-copper-500 to-amber-600 rounded-xl flex items-center justify-center shadow-copper-sm">
              <span className="text-xl font-bold text-white font-display">
                {(user?.name || '').split(' ').map(s => s[0]).filter(Boolean).join('').toUpperCase().slice(0, 2)}
              </span>
            </div>
            <div className="flex-1">
              <h2 className="text-lg font-semibold text-text-primary">{user?.name}</h2>
              <p className="text-sm text-text-secondary">{user?.email}</p>
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 bg-copper-500/10 border border-copper-500/20 rounded-lg">
              <Shield className="w-4 h-4 text-copper-500" />
              <span className="text-sm font-medium text-copper-500">{user?.role}</span>
            </div>
          </div>
          <div className="mt-4 flex items-center gap-2 text-sm text-text-dim">
            <Calendar className="w-4 h-4" />
            Member since {formatDate(user?.createdAt)}
          </div>
        </div>

        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setTab('profile')}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              tab === 'profile'
                ? 'bg-copper-500 text-white shadow-copper-sm'
                : 'bg-bg-surface border border-border-subtle text-text-secondary hover:bg-copper-500/10 hover:text-copper-500'
            }`}
          >
            Edit Profile
          </button>
          <button
            onClick={() => setTab('password')}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              tab === 'password'
                ? 'bg-copper-500 text-white shadow-copper-sm'
                : 'bg-bg-surface border border-border-subtle text-text-secondary hover:bg-copper-500/10 hover:text-copper-500'
            }`}
          >
            Change Password
          </button>
        </div>

        {msg && (
          <div className={`mb-4 p-3 rounded-lg text-sm font-medium border ${
            msgType === 'error'
              ? 'bg-red-500/10 text-red-400 border-red-500/20'
              : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
          }`}>
            {msg}
          </div>
        )}

        {tab === 'profile' ? (
          <form onSubmit={handleProfileSave} className="card-copper p-6 space-y-5">
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5 font-body">Name</label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-text-dim" />
                <input
                  type="text"
                  value={form.name}
                  onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))}
                  className={`input-copper !pl-10 ${errors.name ? 'border-red-500' : ''}`}
                  required
                />
              </div>
              {errors.name && <p className="mt-1 text-xs text-red-400">{errors.name}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5 font-body">Email</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-text-dim" />
                <input
                  type="email"
                  value={form.email !== undefined ? form.email : user?.email || ''}
                  onChange={(e) => setForm(f => ({ ...f, email: e.target.value }))}
                  className={`input-copper !pl-10 ${errors.email ? 'border-red-500' : ''}`}
                  required
                />
              </div>
              {errors.email && <p className="mt-1 text-xs text-red-400">{errors.email}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5 font-body">Phone</label>
              <div className="relative">
                <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-text-dim" />
                <input
                  type="tel"
                  value={form.phone}
                  onChange={(e) => setForm(f => ({ ...f, phone: e.target.value }))}
                  className="input-copper !pl-10"
                />
              </div>
            </div>
            <div className="flex items-center gap-2 text-sm text-text-dim">
              <Shield className="w-4 h-4" /> Role: <span className="font-medium text-text-secondary">{user?.role}</span>
            </div>
            <button type="submit" disabled={saving} className="btn-copper">
              <Save className="w-4 h-4" /> {saving ? 'Saving...' : 'Save Changes'}
            </button>
          </form>
        ) : (
          <form onSubmit={handlePasswordChange} className="card-copper p-6 space-y-5">
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5 font-body">Current password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-text-dim" />
                <input
                  type="password"
                  value={pwForm.currentPassword}
                  onChange={(e) => setPwForm(f => ({ ...f, currentPassword: e.target.value }))}
                  className={`input-copper !pl-10 ${errors.currentPassword ? 'border-red-500' : ''}`}
                  required
                />
              </div>
              {errors.currentPassword && <p className="mt-1 text-xs text-red-400">{errors.currentPassword}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5 font-body">New password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-text-dim" />
                <input
                  type="password"
                  value={pwForm.newPassword}
                  onChange={(e) => setPwForm(f => ({ ...f, newPassword: e.target.value }))}
                  className={`input-copper !pl-10 ${errors.newPassword ? 'border-red-500' : ''}`}
                  required
                />
              </div>
              {errors.newPassword && <p className="mt-1 text-xs text-red-400">{errors.newPassword}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5 font-body">Confirm new password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-text-dim" />
                <input
                  type="password"
                  value={pwForm.confirmPassword}
                  onChange={(e) => setPwForm(f => ({ ...f, confirmPassword: e.target.value }))}
                  className={`input-copper !pl-10 ${errors.confirmPassword ? 'border-red-500' : ''}`}
                  required
                />
              </div>
              {errors.confirmPassword && <p className="mt-1 text-xs text-red-400">{errors.confirmPassword}</p>}
            </div>
            <button type="submit" disabled={saving} className="btn-copper">
              <Save className="w-4 h-4" /> {saving ? 'Changing...' : 'Change Password'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
