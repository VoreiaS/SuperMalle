import { useState, useEffect } from 'react';
import {
  Megaphone,
  Send,
  Clock,
  Users,
  MessageSquare,
  AlertCircle,
  CheckCircle2,
  Construction,
  RefreshCw,
  Trash2,
} from 'lucide-react';
import { adminNotificationApi } from '../../api/endpoints';
import { formatDate } from '../../api/helpers';

/* ──────────────────── Constants ──────────────────── */

const STORAGE_KEY = 'supermalle_announcements';

/* ──────────────────── localStorage helpers ──────────────────── */

function loadAnnouncements() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch { /* ignore */ }
  return [];
}

function saveAnnouncements(list) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
  } catch { /* ignore */ }
}

/* ──────────────────── Default demo announcements ──────────────────── */

const DEMO_ANNOUNCEMENTS = [
  {
    id: 'demo-1',
    message:
      'Grand opening celebration! Get 20% off your first order this weekend. Use code WELCOME20 at checkout.',
    sentAt: '2025-04-12T14:30:00Z',
    recipientCount: 1248,
    source: 'demo',
  },
  {
    id: 'demo-2',
    message:
      'New menu items available! Try our signature Truffle Risotto and Creme Brulee, now live on the menu.',
    sentAt: '2025-04-10T09:15:00Z',
    recipientCount: 1310,
    source: 'demo',
  },
  {
    id: 'demo-3',
    message:
      'Holiday hours update: We will be closed on Easter Sunday. Normal hours resume Monday.',
    sentAt: '2025-04-05T16:00:00Z',
    recipientCount: 1185,
    source: 'demo',
  },
];

/* ──────────────────── Helpers ──────────────────── */

function formatNumber(n) {
  if (n == null) return '--';
  return n.toLocaleString();
}

/* ──────────────────── Announcement Card ──────────────────── */

function AnnouncementCard({ announcement, onDelete }) {
  const isLocal = announcement.source === 'local';
  const isDemo = announcement.source === 'demo';

  return (
    <div className={`bg-bg-surface rounded-xl shadow-copper-sm p-5 hover:shadow-copper-md transition-shadow ${isLocal ? 'ring-1 ring-copper-500/30' : ''}`}>
      <div className="flex items-start gap-4">
        <div className="flex items-center justify-center w-9 h-9 rounded-xl bg-copper-500 shadow-copper-sm shrink-0 mt-0.5">
          <MessageSquare className="w-4 h-4 text-white" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm text-text-primary leading-relaxed">
            {announcement.message}
          </p>
          <div className="flex flex-wrap items-center gap-4 mt-3">
            <div className="flex items-center gap-1.5 text-xs text-text-dim">
              <Clock className="w-3.5 h-3.5" />
              <span>{formatDate(announcement.sentAt)}</span>
            </div>
            {announcement.recipientCount != null && (
              <div className="flex items-center gap-1.5 text-xs text-text-dim">
                <Users className="w-3.5 h-3.5" />
                <span>{formatNumber(announcement.recipientCount)} recipients</span>
              </div>
            )}
            {isLocal && (
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-copper-500/10 text-copper-500">
                Local draft
              </span>
            )}
            {isDemo && (
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-bg-hover text-text-secondary">
                Demo
              </span>
            )}
          </div>
        </div>
        {isLocal && onDelete && (
          <button
            onClick={() => onDelete(announcement.id)}
            className="p-1.5 rounded-lg hover:bg-red-500/10 transition text-text-dim hover:text-red-400 shrink-0"
            title="Delete"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
}

/* ──────────────────── Main Page ──────────────────── */

export default function AdminAnnounce() {
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [toast, setToast] = useState(null);
  const [announcements, setAnnouncements] = useState([]);
  const [backendAvailable, setBackendAvailable] = useState(null); // null = unknown

  /* ── Load announcements ── */

  useEffect(() => {
    // Start with locally stored announcements
    const local = loadAnnouncements();
    setAnnouncements(local.length > 0 ? local : DEMO_ANNOUNCEMENTS);
  }, []);

  /* ── Dismiss toast ── */

  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(null), 5000);
    return () => clearTimeout(timer);
  }, [toast]);

  /* ── Send announcement ── */

  const handleSend = async () => {
    if (!message.trim()) return;

    setSending(true);
    setToast(null);

    try {
      // Attempt to send via backend API
      await adminNotificationApi.announce(message.trim());
      setBackendAvailable(true);

      // Add to local list on success
      const newAnnouncement = {
        id: `api-${Date.now()}`,
        message: message.trim(),
        sentAt: new Date().toISOString(),
        recipientCount: null, // will be populated by backend in future
        source: 'api',
      };

      setAnnouncements((prev) => {
        const updated = [newAnnouncement, ...prev.filter((a) => a.source !== 'demo')];
        saveAnnouncements(updated);
        return updated;
      });

      setToast({
        type: 'success',
        text: 'Announcement sent to all users!',
      });
      setMessage('');
    } catch (err) {
      // Backend unavailable — save locally
      setBackendAvailable(false);

      const newAnnouncement = {
        id: `local-${Date.now()}`,
        message: message.trim(),
        sentAt: new Date().toISOString(),
        recipientCount: null,
        source: 'local',
      };

      setAnnouncements((prev) => {
        const updated = [newAnnouncement, ...prev.filter((a) => a.source !== 'demo')];
        saveAnnouncements(updated);
        return updated;
      });

      setToast({
        type: 'info',
        text: 'Broadcast messaging is not yet available on the server. Your announcement has been saved locally and will be sent once the feature is live.',
      });
      setMessage('');
    } finally {
      setSending(false);
    }
  };

  /* ── Delete local announcement ── */

  const handleDelete = (id) => {
    setAnnouncements((prev) => {
      const updated = prev.filter((a) => a.id !== id);
      saveAnnouncements(updated);
      return updated;
    });
  };

  /* ── Render ── */

  return (
    <div className="relative min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* ── Header ── */}
        <div className="flex items-center gap-3">
          <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-copper-500 shadow-copper-sm">
            <Megaphone className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-text-primary">Announcements</h1>
            <p className="text-sm text-text-secondary">
              Send broadcast messages to all your customers
            </p>
          </div>
        </div>

        {/* ── Backend status notice ── */}
        {backendAvailable === false && (
          <div className="flex items-start gap-3 bg-copper-500/10 border border-copper-500/20 text-copper-500 rounded-xl px-5 py-4">
            <Construction className="w-5 h-5 shrink-0 mt-0.5" />
            <div>
              <p className="text-sm font-medium">Broadcast feature coming soon</p>
              <p className="text-xs mt-0.5 text-copper-500">
                The server does not yet support broadcast notifications. Announcements are saved locally and will be delivered once this feature is enabled on the backend.
              </p>
            </div>
          </div>
        )}

        {/* ── Toast ── */}
        {toast && (
          <div
            className={`flex items-center gap-3 rounded-xl px-5 py-4 border ${
              toast.type === 'info'
                ? 'bg-copper-500/10 border-copper-500/20 text-copper-500'
                : toast.type === 'error'
                  ? 'bg-red-500/10 border-red-500/20 text-red-400'
                  : 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400'
            }`}
          >
            {toast.type === 'info' && (
              <AlertCircle className="w-5 h-5 shrink-0" />
            )}
            {toast.type === 'error' && (
              <AlertCircle className="w-5 h-5 shrink-0" />
            )}
            {toast.type === 'success' && (
              <CheckCircle2 className="w-5 h-5 shrink-0" />
            )}
            <p className="text-sm">{toast.text}</p>
          </div>
        )}

        {/* ── Compose ── */}
        <div className="bg-bg-surface rounded-xl shadow-copper-sm p-6 space-y-4">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-9 h-9 rounded-xl bg-copper-500/10">
              <Send className="w-4 h-4 text-copper-500" />
            </div>
            <h2 className="text-lg font-semibold text-text-primary">
              Compose Announcement
            </h2>
          </div>

          <textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            rows={4}
            placeholder="Type your announcement message here..."
            className="w-full rounded-xl border border-border-subtle bg-bg-surface px-4 py-3 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition resize-y placeholder:text-text-dim"
          />

          <div className="flex items-center justify-between">
            <p className="text-xs text-text-dim">
              {message.length > 0
                ? `${message.length} character${message.length !== 1 ? 's' : ''}`
                : 'Your message will be sent to all registered users'}
            </p>
            <button
              onClick={handleSend}
              disabled={!message.trim() || sending}
              className="inline-flex items-center gap-2 px-6 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:opacity-90 transition shadow-copper-sm disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {sending ? (
                <RefreshCw className="w-4 h-4 animate-spin" />
              ) : (
                <Send className="w-4 h-4" />
              )}
              Send to all users
            </button>
          </div>
        </div>

        {/* ── Recent Announcements ── */}
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-text-dim" />
            <h2 className="text-lg font-semibold text-text-primary">
              Recent Announcements
            </h2>
          </div>

          {announcements.length === 0 ? (
            <div className="bg-bg-surface rounded-xl shadow-copper-sm p-12 flex flex-col items-center justify-center text-text-dim">
              <Megaphone className="w-14 h-14 mb-4 text-text-dim" />
              <p className="text-base font-medium text-text-secondary">No announcements yet</p>
              <p className="text-sm mt-1">Compose your first announcement above.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {announcements.map((a) => (
                <AnnouncementCard
                  key={a.id}
                  announcement={a}
                  onDelete={a.source === 'local' ? handleDelete : undefined}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
