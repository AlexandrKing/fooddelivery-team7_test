import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import RestaurantCard from '../components/RestaurantCard.jsx';
import { fetchRestaurants } from '../services/restaurantsApi.js';

function buildFetchParams(appliedRating, appliedDeliveryTime) {
  const params = {};
  if (appliedRating.trim() !== '') {
    const r = Number(appliedRating);
    if (Number.isFinite(r)) {
      params.rating = r;
    }
  }
  if (appliedDeliveryTime.trim() !== '') {
    const d = Number(appliedDeliveryTime);
    if (Number.isFinite(d)) {
      params.deliveryTime = Math.trunc(d);
    }
  }
  return params;
}

export default function RestaurantsPage() {
  const [draftRating, setDraftRating] = useState('');
  const [draftDeliveryTime, setDraftDeliveryTime] = useState('');
  const [appliedRating, setAppliedRating] = useState('');
  const [appliedDeliveryTime, setAppliedDeliveryTime] = useState('');

  const [status, setStatus] = useState('loading');
  const [restaurants, setRestaurants] = useState([]);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function run() {
      setStatus('loading');
      setErrorMessage('');
      const params = buildFetchParams(appliedRating, appliedDeliveryTime);
      try {
        const list = await fetchRestaurants(params);
        if (!cancelled) {
          setRestaurants(list);
          setStatus('success');
        }
      } catch (e) {
        if (!cancelled) {
          const msg =
            e?.status === 401
              ? 'Доступ запрещён (401). Войдите снова — пароль мог измениться на сервере.'
              : e?.message || 'Не удалось загрузить рестораны';
          setErrorMessage(msg);
          setRestaurants([]);
          setStatus('error');
        }
      }
    }

    run();
    return () => {
      cancelled = true;
    };
  }, [appliedRating, appliedDeliveryTime]);

  function handleApply(e) {
    e.preventDefault();
    setAppliedRating(draftRating.trim());
    setAppliedDeliveryTime(draftDeliveryTime.trim());
  }

  function handleReset() {
    setDraftRating('');
    setDraftDeliveryTime('');
    setAppliedRating('');
    setAppliedDeliveryTime('');
  }

  const busy = status === 'loading';

  return (
    <section className="page restaurants-page">
      <h2>Рестораны</h2>

      <form className="filters-form" onSubmit={handleApply}>
        <label className="filters-form__field">
          Мин. рейтинг
          <input
            type="number"
            name="rating"
            min="0"
            max="5"
            step="0.1"
            value={draftRating}
            onChange={(e) => setDraftRating(e.target.value)}
            disabled={busy}
            placeholder="например 4"
          />
        </label>
        <label className="filters-form__field">
          Макс. время доставки (мин)
          <input
            type="number"
            name="deliveryTime"
            min="1"
            step="1"
            value={draftDeliveryTime}
            onChange={(e) => setDraftDeliveryTime(e.target.value)}
            disabled={busy}
            placeholder="например 45"
          />
        </label>
        <div className="filters-form__actions">
          <button type="submit" className="filters-form__btn" disabled={busy}>
            Применить
          </button>
          <button
            type="button"
            className="filters-form__btn filters-form__btn--secondary"
            onClick={handleReset}
            disabled={busy}
          >
            Сброс
          </button>
        </div>
      </form>

      {status === 'loading' && (
        <p className="state state--loading" aria-busy="true">
          Загрузка…
        </p>
      )}

      {status === 'error' && (
        <div className="state state--error" role="alert">
          <strong>Ошибка</strong>
          <p>{errorMessage}</p>
        </div>
      )}

      {status === 'success' && restaurants.length === 0 && (
        <p className="state state--empty">Список пуст.</p>
      )}

      {status === 'success' && restaurants.length > 0 && (
        <ul className="restaurant-list">
          {restaurants.map((r) => (
            <li key={r.id} className="restaurant-list__item">
              <RestaurantCard restaurant={r} />
              <Link
                className="link-button"
                to={`/restaurants/${r.id}/menu`}
                state={{ restaurantName: r.name }}
              >
                Меню
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
