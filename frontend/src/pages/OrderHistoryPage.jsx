import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import {
  createCourierReview,
  fetchMyCourierReviews,
} from '../services/courierReviewApi.js';
import { cancelOrder, fetchOrder, fetchUserOrders } from '../services/orderApi.js';

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

function canCancelOrder(status) {
  return status === 'PENDING';
}

const STATUS_LABELS = {
  PENDING: 'Ожидает подтверждения',
  ASSIGNED: 'Назначен курьеру',
  ACCEPTED: 'Подтверждён',
  PREPARING: 'Готовится',
  COOKING: 'Готовится',
  READY: 'Готов к выдаче',
  PICKED_UP: 'У курьера',
  IN_DELIVERY: 'В пути',
  DELIVERING: 'В пути',
  DELIVERED: 'Доставлен',
  CANCELLED: 'Отменён',
};

function statusLabel(status) {
  if (!status) return '—';
  return STATUS_LABELS[status] || status;
}

function orderIdNum(order) {
  const n = Number(order?.id);
  return Number.isFinite(n) && n > 0 ? n : null;
}

function canLeaveCourierReview(order, reviewedOrderIds) {
  const oid = orderIdNum(order);
  if (oid == null || reviewedOrderIds.has(oid)) return false;
  if (String(order.status || '').toUpperCase() !== 'DELIVERED') return false;
  if (order.courierId == null) return false;
  const dt = String(order.deliveryType || '').toUpperCase();
  return dt === 'DELIVERY';
}

export default function OrderHistoryPage() {
  const location = useLocation();
  const { user } = useAuth();
  const userId = useMemo(() => {
    const id = Number(user?.id);
    return Number.isFinite(id) && id > 0 ? id : null;
  }, [user]);

  const [status, setStatus] = useState('loading');
  const [orders, setOrders] = useState([]);
  const [errorMessage, setErrorMessage] = useState('');
  const [detailsById, setDetailsById] = useState({});
  const [loadingDetailsId, setLoadingDetailsId] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);
  const [reviewedOrderIds, setReviewedOrderIds] = useState(() => new Set());
  const [reviewDraftOrderId, setReviewDraftOrderId] = useState(null);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReviewOrderId, setSubmittingReviewOrderId] = useState(null);

  const loadOrders = useCallback(async () => {
    if (userId == null) {
      setStatus('error');
      setErrorMessage('Не удалось определить пользователя для заказов.');
      setOrders([]);
      return;
    }

    setStatus('loading');
    setErrorMessage('');
    try {
      const list = await fetchUserOrders(userId);
      let mine = [];
      try {
        mine = await fetchMyCourierReviews();
      } catch {
        mine = [];
      }
      setOrders(list);
      setReviewedOrderIds(
        new Set(
          mine
            .map((r) => Number(r.orderId))
            .filter((x) => Number.isFinite(x) && x > 0)
        )
      );
      setStatus('success');
    } catch (e) {
      setOrders([]);
      setStatus('error');
      setErrorMessage(e?.message || 'Не удалось загрузить заказы');
    }
  }, [userId]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders, location.key]);

  async function handleLoadDetails(orderId) {
    setLoadingDetailsId(orderId);
    setErrorMessage('');
    try {
      const details = await fetchOrder(orderId);
      setDetailsById((prev) => ({ ...prev, [orderId]: details }));
    } catch (e) {
      setErrorMessage(e?.message || 'Не удалось загрузить детали заказа');
    } finally {
      setLoadingDetailsId(null);
    }
  }

  async function handleSubmitCourierReview(orderId) {
    const oid = Number(orderId);
    if (!Number.isFinite(oid) || oid < 1) return;
    setSubmittingReviewOrderId(oid);
    setErrorMessage('');
    try {
      await createCourierReview({
        orderId: oid,
        rating: reviewRating,
        comment: reviewComment,
      });
      setReviewedOrderIds((prev) => new Set([...prev, oid]));
      setReviewDraftOrderId(null);
      setReviewRating(5);
      setReviewComment('');
    } catch (e) {
      setErrorMessage(e?.message || 'Не удалось отправить отзыв');
    } finally {
      setSubmittingReviewOrderId(null);
    }
  }

  async function handleCancelOrder(orderId) {
    setCancellingId(orderId);
    setErrorMessage('');
    try {
      const updated = await cancelOrder(orderId);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      setDetailsById((prev) => ({ ...prev, [orderId]: updated }));
    } catch (e) {
      setErrorMessage(e?.message || 'Не удалось отменить заказ');
    } finally {
      setCancellingId(null);
    }
  }

  return (
    <section className="page orders-page">
      <nav className="page-nav">
        <Link to="/">← К ресторанам</Link>
      </nav>

      <div className="orders-page__toolbar">
        <h2>Мои заказы</h2>
        <button
          type="button"
          className="filters-form__btn filters-form__btn--secondary"
          onClick={() => loadOrders()}
          disabled={status === 'loading' || userId == null}
        >
          {status === 'loading' ? 'Обновление…' : 'Обновить список'}
        </button>
      </div>

      {status === 'loading' && (
        <p className="state state--loading" aria-busy="true">
          Загрузка заказов…
        </p>
      )}

      {status === 'error' && (
        <div className="state state--error" role="alert">
          <strong>Ошибка</strong>
          <p>{errorMessage}</p>
        </div>
      )}

      {status === 'success' && orders.length === 0 && (
        <p className="state state--empty">Заказов пока нет.</p>
      )}

      {status === 'success' && orders.length > 0 && (
        <ul className="order-list">
          {orders.map((order) => {
            const oid = orderIdNum(order);
            const details = detailsById[order.id];
            const items = Array.isArray(details?.items) ? details.items : null;
            const isLoadingDetails = loadingDetailsId === order.id;
            const isCancelling = cancellingId === order.id;
            const canCancel = canCancelOrder(order.status);
            const showReviewForm = oid != null && reviewDraftOrderId === oid;
            const canReview = canLeaveCourierReview(order, reviewedOrderIds);
            const hasCourierReview = oid != null && reviewedOrderIds.has(oid);
            const delivered = String(order.status || '').toUpperCase() === 'DELIVERED';

            return (
              <li key={order.id} className="order-card">
                <div className="order-card__head">
                  <h3>Заказ #{order.id}</h3>
                  <span className="badge" title={order.status || ''}>
                    {statusLabel(order.status)}
                  </span>
                </div>
                {hasCourierReview && delivered && (
                  <p className="order-card__line order-card__line--muted">
                    Отзыв на курьера оставлен
                  </p>
                )}
                <p className="order-card__line">
                  Сумма: <strong>{asCurrency(order.totalAmount)}</strong>
                </p>
                <p className="order-card__line">Дата: {asDate(order.createdAt)}</p>
                <p className="order-card__line">
                  Доставка: {order.deliveryType || '—'} · {order.deliveryAddress || '—'}
                </p>

                <div className="order-card__actions">
                  <button
                    type="button"
                    className="filters-form__btn filters-form__btn--secondary"
                    onClick={() => handleLoadDetails(order.id)}
                    disabled={isLoadingDetails}
                  >
                    {isLoadingDetails ? 'Загрузка…' : 'Показать позиции'}
                  </button>
                  {canCancel && (
                    <button
                      type="button"
                      className="filters-form__btn filters-form__btn--secondary"
                      onClick={() => handleCancelOrder(order.id)}
                      disabled={isCancelling}
                    >
                      {isCancelling ? 'Отмена…' : 'Отменить'}
                    </button>
                  )}
                  {canReview && !showReviewForm && oid != null && (
                    <button
                      type="button"
                      className="filters-form__btn filters-form__btn--secondary"
                      onClick={() => {
                        setReviewDraftOrderId(oid);
                        setReviewRating(5);
                        setReviewComment('');
                      }}
                    >
                      Оставить отзыв
                    </button>
                  )}
                </div>

                {showReviewForm && (
                  <div className="courier-review-form">
                    <p className="courier-review-form__title">Отзыв о курьере</p>
                    <label className="checkout-form__field">
                      Оценка (1–5)
                      <select
                        value={reviewRating}
                        onChange={(e) => setReviewRating(Number(e.target.value))}
                      >
                        {[1, 2, 3, 4, 5].map((n) => (
                          <option key={n} value={n}>
                            {n}
                          </option>
                        ))}
                      </select>
                    </label>
                    <label className="checkout-form__field">
                      Комментарий
                      <textarea
                        rows={3}
                        value={reviewComment}
                        onChange={(e) => setReviewComment(e.target.value)}
                        placeholder="Необязательно"
                      />
                    </label>
                    <div className="order-card__actions">
                      <button
                        type="button"
                        className="filters-form__btn"
                        onClick={() => handleSubmitCourierReview(oid)}
                        disabled={submittingReviewOrderId === oid}
                      >
                        {submittingReviewOrderId === oid ? 'Отправка…' : 'Отправить'}
                      </button>
                      <button
                        type="button"
                        className="filters-form__btn filters-form__btn--secondary"
                        onClick={() => setReviewDraftOrderId(null)}
                        disabled={submittingReviewOrderId === oid}
                      >
                        Отмена
                      </button>
                    </div>
                  </div>
                )}

                {items && (
                  <ul className="order-items">
                    {items.length === 0 && <li>Позиции не найдены.</li>}
                    {items.map((item) => (
                      <li key={item.id}>
                        {item.name || `Блюдо #${item.menuItemId}`} · {item.quantity} ×{' '}
                        {asCurrency(item.price)}
                      </li>
                    ))}
                  </ul>
                )}
              </li>
            );
          })}
        </ul>
      )}

      {errorMessage && status === 'success' && (
        <div className="state state--error" role="alert">
          <p>{errorMessage}</p>
        </div>
      )}
    </section>
  );
}
