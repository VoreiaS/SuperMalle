import { Link } from 'react-router-dom';
import { Home, Search, ArrowLeft } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-bg-base flex items-center justify-center px-4 relative animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="relative max-w-lg w-full text-center">
        <div className="mb-8">
          <div className="inline-flex items-center justify-center w-32 h-32 bg-copper-500 rounded-full shadow-copper-lg">
            <span className="text-6xl font-bold text-white font-display">404</span>
          </div>
        </div>

        <h1 className="font-display text-4xl text-text-primary mb-4">
          Page Not Found
        </h1>
        <p className="text-lg text-text-secondary mb-8">
          Oops! The page you're looking for doesn't exist or has been moved.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link to="/" className="btn-copper !px-8 !py-3">
            <Home className="w-5 h-5" />
            Go to Home
          </Link>
          <button
            onClick={() => window.history.back()}
            className="btn-copper-outline !px-8 !py-3"
          >
            <ArrowLeft className="w-5 h-5" />
            Go Back
          </button>
        </div>

        <div className="mt-12 pt-8 border-t border-border-subtle">
          <p className="text-sm text-text-dim mb-4">Looking for something specific?</p>
          <Link
            to="/menu"
            className="inline-flex items-center gap-2 text-copper-500 hover:text-copper-400 font-medium transition"
          >
            <Search className="w-4 h-4" />
            Browse our menu
          </Link>
        </div>
      </div>
    </div>
  );
}
