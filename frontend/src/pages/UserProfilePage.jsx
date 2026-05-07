import { useEffect, useState } from 'react';
import { fetchUserProfile, updateUserProfile } from '../services/userApi.js';

export default function UserProfilePage() {
  const [status, setStatus] = useState('loading');
  const [saving, setSaving] = useState(false);
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ fullName: '', phone: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setStatus('loading');
      setError('');
      try {
        const data = await fetchUserProfile();
        if (cancelled) return;
        setProfile(data);
        setForm({
          fullName: data.fullName || '',
          phone: data.phone || '',
        });
        setStatus('success');
      } catch (e) {
        if (cancelled) return;
        setError(e?.message || 'Не удалось загрузить профиль');
        setStatus('error');
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const updated = await updateUserProfile(form);
      setProfile(updated);
      setForm({
        fullName: updated.fullName || '',
        phone: updated.phone || '',
      });
      setSuccess('Профиль обновлен');
    } catch (err) {
      setError(err?.message || 'Не удалось обновить профиль');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page dashboard-page">
      <h2>Профиль</h2>

      {status === 'loading' && (
        <p className="state state--loading" aria-busy="true">
          Загрузка...
        </p>
      )}

      {error && (
        <div className="state state--error" role="alert">
          <p>{error}</p>
        </div>
      )}

      {success && (
        <div className="state state--success" role="status">
          <p>{success}</p>
        </div>
      )}

      {status === 'success' && profile && (
        <section className="dashboard-block">
          <form className="filters-form" onSubmit={handleSubmit}>
            <label className="filters-form__field">
              Имя
              <input
                type="text"
                value={form.fullName}
                onChange={(e) => setForm((prev) => ({ ...prev, fullName: e.target.value }))}
                disabled={saving}
              />
            </label>
            <label className="filters-form__field">
              Email
              <input type="email" value={profile.email || ''} disabled readOnly />
            </label>
            <label className="filters-form__field">
              Телефон
              <input
                type="tel"
                value={form.phone}
                onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))}
                disabled={saving}
              />
            </label>
            <div className="filters-form__actions">
              <button type="submit" className="filters-form__btn" disabled={saving}>
                {saving ? 'Сохранение...' : 'Сохранить'}
              </button>
            </div>
          </form>

          <h3>Адреса</h3>
          {profile.addresses?.length > 0 ? (
            <ul className="order-list">
              {profile.addresses.map((address) => (
                <li key={address.id} className="order-card">
                  <p className="order-card__line">
                    <strong>{address.label || 'Адрес'}</strong>
                  </p>
                  <p className="order-card__line">{address.address}</p>
                  {address.apartment && (
                    <p className="order-card__line">Квартира: {address.apartment}</p>
                  )}
                </li>
              ))}
            </ul>
          ) : (
            <p className="state state--empty">Адресов пока нет.</p>
          )}
        </section>
      )}
    </section>
  );
}
