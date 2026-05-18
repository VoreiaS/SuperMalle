import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Lock, Eye, EyeOff, ChefHat, CheckCircle } from 'lucide-react';
import { authApi } from '../../api/endpoints';
import { resetPasswordSchema } from '../../lib/validation';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!token) { setError('Invalid or missing reset token'); return; }
    const result = resetPasswordSchema.safeParse({ newPassword, confirmPassword });
    if (!result.success) {
      const first = result.error.errors[0];
      setError(first.message);
      return;
    }
    setLoading(true);
    try {
      await authApi.resetPassword(token, newPassword);
      setSuccess(true);
      setTimeout(() => navigate('/login'), 3000);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Failed to reset password');
    } finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-bg-base px-4 relative overflow-hidden animate-fade-in">
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full bg-copper-500/5 blur-[120px] pointer-events-none" />
      <div className="absolute inset-0 bg-noise pointer-events-none" />

      <div className="relative w-full max-w-md animate-fade-in">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl mb-4"
               style={{ background: 'linear-gradient(135deg, #c8663e, #a84d28)' }}>
            <ChefHat className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-3xl text-text-primary">Set new password</h1>
          <p className="font-body text-text-secondary mt-1 text-sm">
            {success ? 'Your password has been reset!' : 'Enter your new password below'}
          </p>
        </div>

        <div className="card-copper p-8 border-t-2 border-copper-500 animate-slide-up stagger-1">
          {error && (
            <div className="mb-5 p-3.5 bg-copper-500/10 border border-copper-500/30 rounded-lg text-sm text-copper-400 font-body font-medium flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-copper-500 shrink-0" />{error}
            </div>
          )}

          {success ? (
            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-xl mb-4 bg-copper-500/20">
                <CheckCircle className="w-8 h-8 text-copper-500" />
              </div>
              <p className="font-body text-text-secondary mb-2 text-sm">Your password has been successfully reset.</p>
              <p className="font-body text-text-dim text-sm mb-4">Redirecting to sign in...</p>
              <Link to="/login" className="text-copper-500 font-semibold hover:text-copper-400 transition-colors font-body">
                Sign in now
              </Link>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block font-body font-semibold text-xs text-text-secondary uppercase tracking-wider mb-1.5">New password</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
                  <input type={showPw ? 'text' : 'password'} value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="input-copper !pl-10 !pr-12" placeholder="Min 6 characters" required minLength={6} />
                  <button type="button" onClick={() => setShowPw(!showPw)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-text-dim hover:text-text-secondary transition-colors">
                    {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>
              <div>
                <label className="block font-body font-semibold text-xs text-text-secondary uppercase tracking-wider mb-1.5">Confirm new password</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
                  <input type={showPw ? 'text' : 'password'} value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="input-copper !pl-10" placeholder="Re-enter password" required minLength={6} />
                </div>
              </div>
              <button type="submit" disabled={loading} className="btn-copper w-full !py-3 !text-sm">
                {loading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Resetting...
                  </span>
                ) : 'Reset password'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
