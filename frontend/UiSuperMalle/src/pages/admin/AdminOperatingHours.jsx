import { useState, useEffect, useCallback } from 'react';
import { Clock, Loader2, RefreshCw, Save } from 'lucide-react';
import { adminOperatingHoursApi } from '../../api/endpoints';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

const DAY_LABELS = {
  MONDAY: 'Monday', TUESDAY: 'Tuesday', WEDNESDAY: 'Wednesday',
  THURSDAY: 'Thursday', FRIDAY: 'Friday', SATURDAY: 'Saturday', SUNDAY: 'Sunday',
};

export default function AdminOperatingHours() {
  const [hoursMap, setHoursMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(null);
  const [message, setMessage] = useState(null);

  const fetchHours = useCallback(async () => {
    setLoading(true);
    setMessage(null);
    try {
      const res = await adminOperatingHoursApi.getAll();
      const map = {};
      (res.data || []).forEach((h) => {
        map[h.dayOfWeek] = {
          openTime: h.openTime ? h.openTime.substring(0, 5) : '09:00',
          closeTime: h.closeTime ? h.closeTime.substring(0, 5) : '22:00',
          isClosed: h.isClosed || false,
        };
      });
      DAYS.forEach((d) => { if (!map[d]) map[d] = { openTime: '09:00', closeTime: '22:00', isClosed: false }; });
      setHoursMap(map);
    } catch {
      setMessage({ type: 'error', text: 'Failed to load operating hours.' });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchHours(); }, [fetchHours]);

  const update = (day, field, value) => {
    setHoursMap((prev) => ({ ...prev, [day]: { ...prev[day], [field]: value } }));
  };

  const handleSave = async (day) => {
    setSaving(day);
    setMessage(null);
    try {
      const data = hoursMap[day];
      await adminOperatingHoursApi.update(day, {
        dayOfWeek: day,
        openTime: data.openTime + ':00',
        closeTime: data.closeTime + ':00',
        isClosed: data.isClosed,
      });
      setMessage({ type: 'success', text: `${DAY_LABELS[day]} updated.` });
    } catch {
      setMessage({ type: 'error', text: `Failed to update ${DAY_LABELS[day]}.` });
    } finally {
      setSaving(null);
    }
  };

  if (loading) {
    return <div className="p-6 flex items-center justify-center min-h-[400px]"><Loader2 className="w-8 h-8 animate-spin text-copper-500" /></div>;
  }

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in relative">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="relative max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="font-display text-2xl text-text-primary">Operating Hours</h1>
          <p className="text-text-secondary mt-1">Set opening and closing times per day</p>
        </div>
        <button onClick={fetchHours} className="btn-copper !px-4 !py-2">
          <RefreshCw className="w-4 h-4" /> Refresh
        </button>
      </div>

      {message && (
        <div className={`mb-4 px-4 py-3 rounded-lg text-sm font-medium ${
          message.type === 'success' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
            : 'bg-red-500/10 text-red-400 border border-red-500/20'
        }`}>
          {message.text}
        </div>
      )}

      <div className="grid gap-4">
        {DAYS.map((day) => {
          const h = hoursMap[day] || { openTime: '09:00', closeTime: '22:00', isClosed: false };
          const isToday = new Date().toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase() === day;
          return (
            <div key={day} className={`card-copper p-5 ${isToday ? 'ring-1 ring-copper-500/30' : ''}`}>
              <div className="flex items-center justify-between gap-4 flex-wrap">
                <div className="flex items-center gap-3 min-w-0">
                  <Clock className={`w-5 h-5 ${isToday ? 'text-copper-500' : 'text-text-dim'}`} />
                  <span className={`font-semibold ${isToday ? 'text-copper-500' : 'text-text-primary'}`}>
                    {DAY_LABELS[day]}
                    {isToday && <span className="text-xs ml-2 text-text-dim">(Today)</span>}
                  </span>
                </div>

                <div className="flex items-center gap-4 flex-wrap">
                  <label className="flex items-center gap-2 text-sm text-text-secondary cursor-pointer">
                    <input
                      type="checkbox"
                      checked={h.isClosed}
                      onChange={(e) => update(day, 'isClosed', e.target.checked)}
                      className="rounded border-border-subtle text-copper-500 focus:ring-copper-500"
                    />
                    Closed
                  </label>

                  {!h.isClosed && (
                    <>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-text-dim">Open</span>
                        <input type="time" value={h.openTime}
                          onChange={(e) => update(day, 'openTime', e.target.value)}
                          className="input-copper !w-32" />
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-text-dim">Close</span>
                        <input type="time" value={h.closeTime}
                          onChange={(e) => update(day, 'closeTime', e.target.value)}
                          className="input-copper !w-32" />
                      </div>
                    </>
                  )}

                  <button onClick={() => handleSave(day)}
                    disabled={saving === day}
                    className="btn-copper !px-4 !py-2">
                    {saving === day ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                    Save
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>
      </div>
    </div>
  );
}
