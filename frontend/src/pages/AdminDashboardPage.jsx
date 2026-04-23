import { useEffect, useMemo, useState } from 'react';
import {
  deleteAdminCourierReview,
  fetchAdminAccounts,
  fetchAdminCourierReviews,
  fetchAdminOrders,
  setAdminAccountActive,
} from '../services/adminApi.js';

function asCurrency(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  return `${n.toLocaleString('ru-RU')} ₽`;
}

function asDate(value) {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return String(value);
  return d.toLocaleString('ru-RU');
}

export default function AdminDashboardPage() {
  const [accountsStatus, setAccountsStatus] = useState('loading');
  const [ordersStatus, setOrdersStatus] = useState('loading');
  const [reviewsStatus, setReviewsStatus] = useState('loading');
  const [accounts, setAccounts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [courierReviews, setCourierReviews] = useState([]);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [updatingAccountId, setUpdatingAccountId] = useState(null);
  const [deletingReviewId, setDeletingReviewId] = useState(null);
  const [emailQuery, setEmailQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [activeFilter, setActiveFilter] = useState('ALL');

  const sortedAccounts = useMemo(() => {
    const q = emailQuery.trim().toLowerCase();
    return [...accounts]
      .filter((a) => {
        if (q && !(a.email || '').toLowerCase().includes(q)) return false;
        if (roleFilter !== 'ALL' && a.role !== roleFilter) return false;
        if (activeFilter === 'ACTIVE' && !a.active) return false;
        if (activeFilter === 'INACTIVE' && a.active) return false;
        return true;
      })
      .sort((a, b) => Number(a.id) - Number(b.id));
  }, [accounts, emailQuery, roleFilter, activeFilter]);

  const sortedOrders = useMemo(
    () => [...orders].sort((a, b) => Number(b.id) - Number(a.id)),
    [orders]
  );

  const sortedCourierReviews = useMemo(
    () => [...courierReviews].sort((a, b) => Number(b.id) - Number(a.id)),
    [courierReviews]
  );

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setAccountsStatus('loading');
      setOrdersStatus('loading');
      setReviewsStatus('loading');
      setError('');
      setSuccessMessage('');
      try {
        const [accountsData, ordersData, reviewsData] = await Promise.all([
          fetchAdminAccounts(),
          fetchAdminOrders(),
          fetchAdminCourierReviews(),
        ]);
        if (cancelled) return;
        setAccounts(accountsData);
        setOrders(ordersData);
        setCourierReviews(reviewsData);
        setAccountsStatus('success');
        setOrdersStatus('success');
        setReviewsStatus('success');
      } catch (e) {
        if (cancelled) return;
        setAccountsStatus('error');
        setOrdersStatus('error');
        setReviewsStatus('error');
        setAccounts([]);
        setOrders([]);
        setCourierReviews([]);
        setError(e?.message || 'Не удалось загрузить админ-панель');
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  async function handleToggleAccountActive(account) {
    setUpdatingAccountId(account.id);
    setError('');
    setSuccessMessage('');
    try {
      const updated = await setAdminAccountActive(account.id, !Boolean(account.active));
      setAccounts((prev) => prev.map((a) => (a.id === account.id ? updated : a)));
      setSuccessMessage(
        updated.active
          ? `Аккаунт ${updated.email} активирован`
          : `Аккаунт ${updated.email} деактивирован`
      );
    } catch (e) {
      setError(e?.message || 'Не удалось изменить статус аккаунта');
    } finally {
      setUpdatingAccountId(null);
    }
  }

  async function handleDeleteCourierReview(review) {
    if (
      !window.confirm(
        `Удалить отзыв #${review.id} по заказу #${review.orderId}? Действие необратимо.`
      )
    ) {
      return;
    }
    setDeletingReviewId(review.id);
    setError('');
    setSuccessMessage('');
    try {
      await deleteAdminCourierReview(review.id);
      setCourierReviews((prev) => prev.filter((r) => r.id !== review.id));
      setSuccessMessage(`Отзыв #${review.id} удалён`);
    } catch (e) {
      setError(e?.message || 'Не удалось удалить отзыв');
    } finally {
      setDeletingReviewId(null);
    }
  }

  return (
    <section className="page dashboard-page">
      <h2>Админ-панель</h2>

      {error && (
        <div className="state state--error" role="alert">
          <p>{error}</p>
        </div>
      )}
      {successMessage && (
        <div className="state state--success" role="status">
          <p>{successMessage}</p>
        </div>
      )}

      <section className="dashboard-block">
        <h3>Аккаунты</h3>
        <form className="dashboard-form dashboard-form--filters" onSubmit={(e) => e.preventDefault()}>
          <div className="dashboard-form__grid">
            <label className="checkout-form__field">
              Поиск по email
              <input
                value={emailQuery}
                onChange={(e) => setEmailQuery(e.target.value)}
                placeholder="например admin@test.local"
              />
            </label>
            <label className="checkout-form__field">
              Роль
              <select value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
                <option value="ALL">Все роли</option>
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
                <option value="COURIER">COURIER</option>
                <option value="RESTAURANT">RESTAURANT</option>
              </select>
            </label>
            <label className="checkout-form__field">
              Активность
              <select value={activeFilter} onChange={(e) => setActiveFilter(e.target.value)}>
                <option value="ALL">Все</option>
                <option value="ACTIVE">Только active</option>
                <option value="INACTIVE">Только inactive</option>
              </select>
            </label>
          </div>
        </form>

        {accountsStatus === 'loading' && (
          <p className="state state--loading" aria-busy="true">
            Загрузка аккаунтов…
          </p>
        )}
        {accountsStatus === 'error' && (
          <p className="state state--error">Не удалось загрузить аккаунты.</p>
        )}
        {accountsStatus === 'success' && sortedAccounts.length === 0 && (
          <p className="state state--empty">
            По текущим фильтрам аккаунты не найдены.
          </p>
        )}
        {accountsStatus === 'success' && sortedAccounts.length > 0 && (
          <ul className="order-list">
            {sortedAccounts.map((account) => (
              <li key={account.id} className="order-card">
                <div className="order-card__head">
                  <h3>Account #{account.id}</h3>
                  <span className="badge">{account.role || '—'}</span>
                </div>
                <p className="order-card__line">Email: {account.email || '—'}</p>
                <p className="order-card__line">
                  Status: <strong>{account.active ? 'ACTIVE' : 'INACTIVE'}</strong>
                </p>
                <p className="order-card__line">
                  Links: user={account.linkedUserId ?? '—'}, restaurant={account.linkedRestaurantId ?? '—'},
                  courier={account.linkedCourierId ?? '—'}, admin={account.linkedAdminId ?? '—'}
                </p>
                <div className="order-card__actions">
                  <button
                    type="button"
                    className="filters-form__btn filters-form__btn--secondary"
                    onClick={() => handleToggleAccountActive(account)}
                    disabled={updatingAccountId === account.id}
                  >
                    {updatingAccountId === account.id
                      ? 'Сохранение…'
                      : account.active
                        ? 'Деактивировать'
                        : 'Активировать'}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="dashboard-block">
        <h3>Отзывы на курьеров</h3>

        {reviewsStatus === 'loading' && (
          <p className="state state--loading" aria-busy="true">
            Загрузка отзывов…
          </p>
        )}
        {reviewsStatus === 'error' && (
          <p className="state state--error">Не удалось загрузить отзывы.</p>
        )}
        {reviewsStatus === 'success' && sortedCourierReviews.length === 0 && (
          <p className="state state--empty">Отзывов пока нет.</p>
        )}
        {reviewsStatus === 'success' && sortedCourierReviews.length > 0 && (
          <ul className="order-list">
            {sortedCourierReviews.map((rev) => (
              <li key={rev.id} className="order-card">
                <div className="order-card__head">
                  <h3>Отзыв #{rev.id}</h3>
                  <span className="badge">★ {rev.rating ?? '—'}</span>
                </div>
                <p className="order-card__line">
                  Заказ: <strong>#{rev.orderId}</strong> · Клиент: {rev.userLabel ?? rev.userId} · Курьер:{' '}
                  {rev.courierLabel ?? rev.courierId}
                </p>
                <p className="order-card__line">Дата: {asDate(rev.createdAt)}</p>
                <p className="order-card__line">
                  Текст: {rev.comment?.trim() ? rev.comment : '—'}
                </p>
                <div className="order-card__actions">
                  <button
                    type="button"
                    className="filters-form__btn filters-form__btn--secondary"
                    onClick={() => handleDeleteCourierReview(rev)}
                    disabled={deletingReviewId === rev.id}
                  >
                    {deletingReviewId === rev.id ? 'Удаление…' : 'Удалить'}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="dashboard-block">
        <h3>Заказы</h3>

        {ordersStatus === 'loading' && (
          <p className="state state--loading" aria-busy="true">
            Загрузка заказов…
          </p>
        )}
        {ordersStatus === 'error' && (
          <p className="state state--error">Не удалось загрузить заказы.</p>
        )}
        {ordersStatus === 'success' && sortedOrders.length === 0 && (
          <p className="state state--empty">Заказы не найдены.</p>
        )}
        {ordersStatus === 'success' && sortedOrders.length > 0 && (
          <ul className="order-list">
            {sortedOrders.map((order) => (
              <li key={order.id} className="order-card">
                <div className="order-card__head">
                  <h3>Заказ #{order.id}</h3>
                  <span className="badge">{order.status || '—'}</span>
                </div>
                <p className="order-card__line">
                  Клиент: {order.userId ?? '—'} · Ресторан: {order.restaurantId ?? '—'}
                </p>
                <p className="order-card__line">
                  Сумма: <strong>{asCurrency(order.totalAmount)}</strong>
                </p>
                <p className="order-card__line">Создан: {asDate(order.createdAt)}</p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </section>
  );
}

