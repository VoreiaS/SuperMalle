import { useEffect } from 'react';
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react';
import useToastStore from '../../store/toastStore';
import { TOAST_TYPES } from '../../store/toastStore';

const TOAST_ICONS = {
  [TOAST_TYPES.SUCCESS]: CheckCircle,
  [TOAST_TYPES.ERROR]: AlertCircle,
  [TOAST_TYPES.WARNING]: AlertTriangle,
  [TOAST_TYPES.INFO]: Info,
};

const TOAST_STYLES = {
  [TOAST_TYPES.SUCCESS]: {
    bg: 'bg-emerald-500/10',
    border: 'border-emerald-500/20',
    icon: 'text-emerald-500',
    title: 'text-emerald-400',
  },
  [TOAST_TYPES.ERROR]: {
    bg: 'bg-red-500/10',
    border: 'border-red-500/20',
    icon: 'text-red-500',
    title: 'text-red-400',
  },
  [TOAST_TYPES.WARNING]: {
    bg: 'bg-copper-500/10',
    border: 'border-copper-500/20',
    icon: 'text-copper-500',
    title: 'text-copper-500',
  },
  [TOAST_TYPES.INFO]: {
    bg: 'bg-bg-elevated',
    border: 'border-border-subtle',
    icon: 'text-text-secondary',
    title: 'text-text-primary',
  },
};

function Toast({ toast, onClose }) {
  const { id, type } = toast;
  const styles = TOAST_STYLES[type] || TOAST_STYLES[TOAST_TYPES.INFO];
  const Icon = TOAST_ICONS[type] || Info;

  return (
    <div
      id={`toast-${id}`}
      role="alert"
      aria-live="polite"
      aria-atomic="true"
      className={`flex items-start gap-3 p-4 rounded-lg border shadow-copper-md ${styles.bg} ${styles.border} animate-slide-up backdrop-blur-md`}
      tabIndex={-1}
    >
      <div className={`flex-shrink-0 w-5 h-5 ${styles.icon}`}>
        <Icon className="w-full h-full" />
      </div>

      <div className={`flex-1 text-sm font-medium ${styles.title}`}>
        {toast.message}
      </div>

      <button
        onClick={() => onClose(id)}
        className={`flex-shrink-0 p-1 rounded-lg hover:bg-black/10 transition-colors ${styles.icon} opacity-60 hover:opacity-100`}
        aria-label="Close notification"
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  );
}

export default function ToastContainer() {
  const { toasts, removeToast } = useToastStore();

  if (toasts.length === 0) return null;

  return (
    <div
      className="fixed top-4 right-4 z-[9999] flex flex-col gap-2 max-w-sm w-full pointer-events-none"
      aria-live="polite"
      aria-atomic="true"
    >
      {toasts.map((toast) => (
        <div key={toast.id} className="pointer-events-auto">
          <Toast toast={toast} onClose={removeToast} />
        </div>
      ))}
    </div>
  );
}
