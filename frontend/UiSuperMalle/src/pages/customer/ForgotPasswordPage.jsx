import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, ChefHat, ArrowLeft, CheckCircle } from 'lucide-react';
import { authApi } from '../../api/endpoints';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [sent, setSent] = useState(false);
  const [resetToken, setResetToken] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!email.trim()) { setError('Please enter your email address'); return; }
    setLoading(true);
    try {
      const res = await authApi.forgotPassword(email);
      const token = res.data?.token || res.data?.resetToken;
      if (token) setResetToken(token);
      setSent(true);
    } catch { setSent(true); } finally { setLoading(false); }
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
          <h1 className="text-3xl text-text-primary">Reset password</h1>
          <p className="font-body text-text-secondary mt-1 text-sm">
            {sent ? 'Check your email for instructions' : "Enter your email and we'll send you a reset link"}
          </p>
        </div>

        <div className="card-copper p-8 border-t-2 border-copper-500 animate-slide-up stagger-1">
          {error && (
            <div className="mb-5 p-3.5 bg-copper-500/10 border border-copper-500/30 rounded-lg text-sm text-copper-400 font-body font-medium flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-copper-500 shrink-0" />{error}
            </div>
          )}

          {sent ? (
            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-xl mb-4 bg-copper-500/20">
                <CheckCircle className="w-8 h-8 text-copper-500" />
              </div>
              <p className="font-body text-text-secondary mb-2 text-sm">
                If an account exists with <span className="font-semibold text-text-primary">{email}</span>,
                you'll receive a password reset link.
              </p>
              {resetToken && (
                <div className="mt-4 p-4 bg-copper-500/10 border border-copper-500/20 rounded-xl text-sm text-left">
                  <p className="text-copper-500 font-body font-semibold text-xs uppercase tracking-wider mb-2">Dev Mode: Reset Token</p>
                  <p className="text-text-secondary break-all font-mono text-xs mb-3">{resetToken}</p>
                  <Link to={`/reset-password?token=${encodeURIComponent(resetToken)}`}
                    className="inline-flex items-center gap-1.5 text-copper-500 hover:text-copper-400 transition-colors font-body font-medium text-sm">
                    Click here to reset password →
                  </Link>
                </div>
              )}
              <Link to="/login"
                className="inline-flex items-center gap-2 mt-6 text-copper-500 hover:text-copper-400 transition-colors font-body font-medium text-sm">
                <ArrowLeft className="w-4 h-4" /> Back to sign in
              </Link>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block font-body font-semibold text-xs text-text-secondary uppercase tracking-wider mb-1.5">Email address</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
                  <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
                    className="input-copper !pl-10" placeholder="you@example.com" required />
                </div>
              </div>
              <button type="submit" disabled={loading} className="btn-copper w-full !py-3 !text-sm">
                {loading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Sending...
                  </span>
                ) : 'Send reset link'}
              </button>
            </form>
          )}
        </div>

        {!sent && (
          <Link to="/login"
            className="flex items-center justify-center gap-2 mt-6 text-sm font-body text-text-secondary hover:text-copper-500 transition-colors">
            <ArrowLeft className="w-4 h-4" /> Back to sign in
          </Link>
        )}
      </div>
    </div>
  );
}
