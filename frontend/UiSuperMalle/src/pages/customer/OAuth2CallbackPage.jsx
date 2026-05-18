import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Loader2, CheckCircle2, XCircle, AlertCircle } from 'lucide-react';
import useAuthStore from '../../store/authStore';
import useToastStore from '../../store/toastStore';

export default function OAuth2CallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const { error: showError } = useToastStore();

  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const handleOAuthCallback = async () => {
      try {
        const code = searchParams.get('code');
        const state = searchParams.get('state');
        const error = searchParams.get('error');
        const errorDescription = searchParams.get('error_description');

        if (error) {
          setStatus('error');
          setMessage(errorDescription || 'Authentication failed. Please try again.');
          showError(errorDescription || 'Authentication failed. Please try again.');
          setTimeout(() => navigate('/login'), 3000);
          return;
        }

        if (!code) {
          setStatus('error');
          setMessage('Invalid authentication response. Please try again.');
          showError('Invalid authentication response. Please try again.');
          setTimeout(() => navigate('/login'), 3000);
          return;
        }

        const savedState = sessionStorage.getItem('oauth_state');
        if (state && savedState && state !== savedState) {
          setStatus('error');
          setMessage('Security validation failed. Please try again.');
          showError('Security validation failed. Please try again.');
          sessionStorage.removeItem('oauth_state');
          setTimeout(() => navigate('/login'), 3000);
          return;
        }

        sessionStorage.removeItem('oauth_state');

        const response = await fetch(
          `${import.meta.env.VITE_API_URL}/oauth2/callback/google`,
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code, state }),
          }
        );

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || 'Authentication failed');
        }

        const data = await response.json();
        const { token, userId, name, email, role } = data;
        const user = { id: userId, name, email, role };

        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));

        setStatus('success');
        setMessage('Authentication successful! Redirecting...');

        setTimeout(() => {
          navigate(role === 'ADMIN' ? '/admin' : '/');
        }, 1500);
      } catch (err) {
        setStatus('error');
        setMessage(err.message || 'An error occurred during authentication. Please try again.');
        showError(err.message || 'An error occurred during authentication. Please try again.');
        setTimeout(() => navigate('/login'), 3000);
      }
    };

    handleOAuthCallback();
  }, [searchParams, navigate, login, showError]);

  return (
    <div className="min-h-screen bg-bg-base flex items-center justify-center px-4 relative animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="relative w-full max-w-md">
        <div className="card-copper p-8 text-center">
          <div className="flex justify-center mb-6">
            {status === 'loading' && (
              <div className="w-16 h-16 bg-copper-500/10 rounded-full flex items-center justify-center border border-copper-500/20">
                <Loader2 className="w-8 h-8 text-copper-500 animate-spin" />
              </div>
            )}
            {status === 'success' && (
              <div className="w-16 h-16 bg-emerald-500/10 rounded-full flex items-center justify-center border border-emerald-500/20">
                <CheckCircle2 className="w-8 h-8 text-emerald-500" />
              </div>
            )}
            {status === 'error' && (
              <div className="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center border border-red-500/20">
                <XCircle className="w-8 h-8 text-red-500" />
              </div>
            )}
          </div>

          <h1 className="text-2xl font-bold text-text-primary mb-2">
            {status === 'loading' && 'Authenticating...'}
            {status === 'success' && 'Success!'}
            {status === 'error' && 'Authentication Failed'}
          </h1>

          <p className="text-text-secondary mb-6">{message}</p>

          {status === 'loading' && (
            <div className="flex items-center justify-center gap-2 text-sm text-text-dim">
              <AlertCircle className="w-4 h-4" />
              <span>Please wait while we complete your sign-in</span>
            </div>
          )}

          {status === 'error' && (
            <button
              onClick={() => navigate('/login')}
              className="btn-copper"
            >
              Back to Login
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
