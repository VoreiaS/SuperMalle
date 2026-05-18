export const LoadingSpinner = ({ size = 'md', color = 'copper' }) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
    xl: 'w-16 h-16',
  };

  const colorClasses = {
    copper: 'text-copper-500',
    white: 'text-white',
    gray: 'text-text-dim',
  };

  return (
    <svg
      className={`animate-spin ${sizeClasses[size]} ${colorClasses[color] || 'text-copper-500'}`}
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
    </svg>
  );
};

export const FullPageLoading = ({ message = 'Loading...' }) => {
  return (
    <div className="min-h-screen bg-bg-base flex items-center justify-center">
      <div className="text-center">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-copper-500/10 border border-copper-500/20 mb-4">
          <LoadingSpinner size="lg" />
        </div>
        <p className="text-text-secondary">{message}</p>
      </div>
    </div>
  );
};

export const CardSkeleton = ({ count = 1 }) => {
  return (
    <>
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="bg-bg-surface rounded-xl shadow-copper-sm p-4 animate-shimmer">
          <div className="flex items-start space-x-4">
            <div className="w-24 h-24 bg-bg-hover rounded-lg" />
            <div className="flex-1 space-y-2">
              <div className="h-4 bg-bg-hover rounded w-3/4" />
              <div className="h-3 bg-bg-hover rounded w-1/2" />
              <div className="h-3 bg-bg-hover rounded w-1/4" />
            </div>
          </div>
        </div>
      ))}
    </>
  );
};

export const MenuItemSkeleton = ({ count = 1 }) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="bg-bg-surface rounded-xl shadow-copper-sm overflow-hidden animate-shimmer">
          <div className="w-full h-48 bg-bg-hover" />
          <div className="p-4 space-y-3">
            <div className="h-5 bg-bg-hover rounded w-3/4" />
            <div className="h-4 bg-bg-hover rounded w-full" />
            <div className="h-4 bg-bg-hover rounded w-1/2" />
            <div className="flex justify-between items-center pt-2">
              <div className="h-6 bg-bg-hover rounded w-1/4" />
              <div className="h-10 bg-bg-hover rounded w-1/3" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export const OrderItemSkeleton = ({ count = 1 }) => {
  return (
    <div className="space-y-4">
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="flex items-center justify-between py-3 border-b border-border-subtle animate-shimmer">
          <div className="flex items-center space-x-3">
            <div className="w-16 h-16 bg-bg-hover rounded-lg" />
            <div className="space-y-2">
              <div className="h-4 bg-bg-hover rounded w-32" />
              <div className="h-3 bg-bg-hover rounded w-20" />
            </div>
          </div>
          <div className="h-5 bg-bg-hover rounded w-16" />
        </div>
      ))}
    </div>
  );
};

export const TableSkeleton = ({ rows = 5, columns = 4 }) => {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-sm overflow-hidden">
      <div className="border-b border-border-subtle">
        <div className="grid grid-cols-4 gap-4 p-4">
          {Array.from({ length: columns }).map((_, index) => (
            <div key={index} className="h-4 bg-bg-hover rounded" />
          ))}
        </div>
      </div>
      <div className="divide-y divide-border-subtle">
        {Array.from({ length: rows }).map((_, rowIndex) => (
          <div key={rowIndex} className="grid grid-cols-4 gap-4 p-4 animate-shimmer">
            {Array.from({ length: columns }).map((_, colIndex) => (
              <div key={colIndex} className="h-4 bg-bg-hover rounded" />
            ))}
          </div>
        ))}
      </div>
    </div>
  );
};

export const ProfileSkeleton = () => {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-sm p-6 animate-shimmer">
      <div className="flex items-center space-x-4 mb-6">
        <div className="w-20 h-20 bg-bg-hover rounded-full" />
        <div className="space-y-2">
          <div className="h-5 bg-bg-hover rounded w-32" />
          <div className="h-4 bg-bg-hover rounded w-48" />
        </div>
      </div>
      <div className="space-y-4">
        <div className="h-4 bg-bg-hover rounded w-full" />
        <div className="h-4 bg-bg-hover rounded w-3/4" />
        <div className="h-4 bg-bg-hover rounded w-1/2" />
      </div>
    </div>
  );
};

export const ButtonLoading = ({ text = 'Loading...' }) => {
  return (
    <span className="flex items-center">
      <LoadingSpinner size="sm" color="white" />
      <span className="ml-2">{text}</span>
    </span>
  );
};

export const InlineLoading = ({ text = 'Loading...' }) => {
  return (
    <div className="flex items-center space-x-2 text-text-secondary">
      <LoadingSpinner size="sm" />
      <span className="text-sm">{text}</span>
    </div>
  );
};

export const DotsLoading = ({ color = 'copper' }) => {
  const colorClasses = {
    copper: 'bg-copper-500',
    white: 'bg-white',
    gray: 'bg-text-dim',
  };

  return (
    <div className="flex space-x-1">
      <div className={`w-2 h-2 rounded-full ${colorClasses[color] || 'bg-copper-500'} animate-bounce`} style={{ animationDelay: '0ms' }} />
      <div className={`w-2 h-2 rounded-full ${colorClasses[color] || 'bg-copper-500'} animate-bounce`} style={{ animationDelay: '150ms' }} />
      <div className={`w-2 h-2 rounded-full ${colorClasses[color] || 'bg-copper-500'} animate-bounce`} style={{ animationDelay: '300ms' }} />
    </div>
  );
};

export const ProgressBarLoading = ({ progress = 0 }) => {
  return (
    <div className="w-full bg-bg-hover rounded-full h-2.5">
      <div className="bg-copper-500 h-2.5 rounded-full transition-all duration-300" style={{ width: `${Math.min(100, Math.max(0, progress))}%` }} />
    </div>
  );
};

export const Shimmer = ({ className = '' }) => {
  return (
    <div className={`animate-shimmer bg-gradient-to-r from-bg-hover via-bg-elevated to-bg-hover bg-[length:200%_100%] ${className}`} />
  );
};

export const LoadingOverlay = ({ message = 'Loading...' }) => {
  return (
    <div className="fixed inset-0 bg-bg-base/75 flex items-center justify-center z-50 backdrop-blur-sm">
      <div className="text-center">
        <LoadingSpinner size="xl" />
        <p className="mt-4 text-text-secondary">{message}</p>
      </div>
    </div>
  );
};

export const EmptyStateLoading = ({ message = 'Loading data...' }) => {
  return (
    <div className="text-center py-12">
      <LoadingSpinner size="lg" />
      <p className="mt-4 text-text-secondary">{message}</p>
    </div>
  );
};
