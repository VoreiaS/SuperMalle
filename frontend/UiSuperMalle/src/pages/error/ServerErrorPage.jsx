import { Link } from 'react-router-dom';
import { Home, RefreshCw, AlertTriangle } from 'lucide-react';

export default function ServerErrorPage() {
  const handleRefresh = () => {
    window.location.reload();
  };

  return (
    <div className="min-h-screen bg-bg-base flex items-center justify-center px-4 relative animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="relative max-w-lg w-full text-center">
        <div className="mb-8">
          <div className="inline-flex items-center justify-center w-32 h-32 bg-gradient-to-br from-red-600 to-copper-600 rounded-full shadow-copper-lg">
            <AlertTriangle className="w-16 h-16 text-white" />
          </div>
        </div>

        <h1 className="font-display text-4xl text-text-primary mb-4">
          Server Error
        </h1>
        <p className="text-lg text-text-secondary mb-8">
          Something went wrong on our end. Our team has been notified and is working to fix it.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <button
            onClick={handleRefresh}
            className="btn-copper !px-8 !py-3"
          >
            <RefreshCw className="w-5 h-5" />
            Try Again
          </button>
          <Link
            to="/"
            className="btn-copper-outline !px-8 !py-3"
          >
            <Home className="w-5 h-5" />
            Go to Home
          </Link>
        </div>

        <div className="mt-12 pt-8 border-t border-border-subtle">
          <p className="text-sm text-text-dim mb-2">
            Error code: 500
          </p>
          <p className="text-sm text-text-dim">
            If this problem persists, please contact our support team.
          </p>
        </div>
      </div>
    </div>
  );
}
