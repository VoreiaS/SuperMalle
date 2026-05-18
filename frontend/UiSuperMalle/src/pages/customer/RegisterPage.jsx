import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff, ChefHat, User, Phone } from 'lucide-react';
import { authApi } from '../../api/endpoints';
import useAuthStore from '../../store/authStore';
import { registerSchema } from '../../lib/validation';

export default function RegisterPage() {
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '', confirmPassword: '' });
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const setUser = useAuthStore((s) => s.setUser);
  const navigate = useNavigate();

  const update = (f, v) => setForm((s) => ({ ...s, [f]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});

    const result = registerSchema.safeParse(form);
    if (!result.success) {
      const errors = {};
      if (result.error?.errors) {
        result.error.errors.forEach((err) => { errors[err.path[0]] = err.message; });
      }
      setFieldErrors(errors);
      return;
    }

    setLoading(true);
    try {
      const { confirmPassword, ...payload } = result.data;
      const res = await authApi.register(payload);
      const { token, userId, name, email, role } = res.data;
      const user = { id: userId, name, email, role };
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      setUser(user);
      navigate(role === 'ADMIN' ? '/admin' : '/');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const inputClasses = 'input-copper !pl-10';

  return (
    <div className="min-h-screen flex items-center justify-center bg-bg-base px-4 py-8 relative overflow-hidden animate-fade-in">
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[700px] rounded-full bg-copper-500/5 blur-[120px] pointer-events-none" />
      <div className="absolute inset-0 bg-noise pointer-events-none" />

      <div className="relative w-full max-w-md animate-fade-in">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl mb-4"
               style={{ background: 'linear-gradient(135deg, #c8663e, #a84d28)' }}>
            <ChefHat className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-3xl text-text-primary">Create account</h1>
          <p className="font-body text-text-secondary mt-1 text-sm">Join us for delicious food</p>
        </div>

        <div className="card-copper p-8 border-t-2 border-copper-500 animate-slide-up stagger-1">
          {error && (
            <div className="mb-5 p-3.5 bg-copper-500/10 border border-copper-500/30 rounded-lg text-sm text-copper-400 font-body font-medium flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-copper-500 shrink-0" />{error}
            </div>
          )}

          <button type="button"
            onClick={() => window.location.href = '/api/v1/oauth2/authorization/google'}
            className="w-full flex items-center justify-center gap-3 py-3 border border-border-subtle rounded-lg text-text-primary font-body font-medium hover:bg-bg-hover transition-all duration-200 copper-press">
            <svg className="w-5 h-5" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            Sign up with Google
          </button>

          <div className="relative my-5">
            <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-border-subtle" /></div>
            <div className="relative flex justify-center text-xs font-body font-medium"><span className="px-3 bg-bg-surface text-text-dim">or</span></div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {[
              { icon: User, field: 'name', type: 'text', placeholder: 'John Doe' },
              { icon: Mail, field: 'email', type: 'email', placeholder: 'you@example.com' },
              { icon: Phone, field: 'phone', type: 'tel', placeholder: '+1 234 567 8900' },
            ].map(({ icon: Icon, field, type, placeholder }) => (
              <div key={field}>
                <label className="block font-body font-semibold text-xs text-text-secondary uppercase tracking-wider mb-1.5">
                  {field === 'phone' ? 'Phone (optional)' : field.charAt(0).toUpperCase() + field.slice(1)}
                </label>
                <div className="relative">
                  <Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
                  <input type={type} value={form[field]} onChange={(e) => update(field, e.target.value)}
                    className={`${inputClasses} ${fieldErrors[field] ? '!border-red-500' : ''}`} placeholder={placeholder}
                    required={field !== 'phone'} />
                </div>
                {fieldErrors[field] && <p className="mt-1 text-xs text-red-400">{fieldErrors[field]}</p>}
              </div>
            ))}

            <div>
              <label className="block font-body font-semibold text-xs text-text-secondary uppercase tracking-wider mb-1.5">Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
                <input type={showPw ? 'text' : 'password'} value={form.password}
                  onChange={(e) => update('password', e.target.value)}
                  className={`input-copper !pl-10 !pr-12 ${fieldErrors.password ? '!border-red-500' : ''}`} placeholder="Min 8 characters, upper, lower, number, special" required />
                <button type="button" onClick={() => setShowPw(!showPw)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-text-dim hover:text-text-secondary transition-colors">
                  {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {fieldErrors.password && <p className="mt-1 text-xs text-red-400">{fieldErrors.password}</p>}
            </div>

            <div>
              <label className="block font-body font-semibold text-xs text-text-secondary uppercase tracking-wider mb-1.5">Confirm password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
                <input type={showPw ? 'text' : 'password'} value={form.confirmPassword}
                  onChange={(e) => update('confirmPassword', e.target.value)}
                  className={`input-copper !pl-10 ${fieldErrors.confirmPassword ? '!border-red-500' : ''}`} placeholder="Re-enter password" required />
              </div>
              {fieldErrors.confirmPassword && <p className="mt-1 text-xs text-red-400">{fieldErrors.confirmPassword}</p>}
            </div>

            <button type="submit" disabled={loading}
              className="btn-copper w-full !py-3 !text-sm mt-2">
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Creating account...
                </span>
              ) : 'Create account'}
            </button>
          </form>
        </div>

        <p className="text-center mt-6 text-sm font-body text-text-secondary animate-slide-up stagger-2">
          Already have an account?{' '}
          <Link to="/login" className="text-copper-500 font-semibold hover:text-copper-400 transition-colors">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
