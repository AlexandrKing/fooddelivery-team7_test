import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  claimCourierOrder,
  fetchAssignedCourierOrders,
  fetchAvailableCourierOrders,
  updateCourierOrderStatus,
} from '../services/courierApi.js';

const COURIER_STATUSES = [
  'ASSIGNED',
  'PICKED_UP',
  'DELIVERING',
  'DELIVERED',
  'CANCELLED',
];

const STATUS_LABELS = {
  ASSIGNED: 'Назначен',
  PICKED_UP: 'Забран',
  DELIVERING: 'В пути',
  DELIVERED: 'Доставлен',
  CANCELLED: 'Отменен',
};

function asDate(value) {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return String(value);
  return d.toLocaleString('ru-RU');
}

function asCurrency(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  return `${n.toLocaleString('ru-RU')} ₽`;
}

export default function CourierDashboardPage() {
  const [listStatus, setListStatus] = useState('loading');
  const [orders, setOrders] = useState([]);
  const [available, setAvailable] = useState([]);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [updatingOrderId, setUpdatingOrderId] = useState(null);
  const [claimingOrderId, setClaimingOrderId] = useState(null);

  const sortedOrders = useMemo(
    () => [...orders].sort((a, b) => Number(b.assignmentId) - Number(a.assignmentId)),
    [orders]
  );

  const reloadLists = useCallback(async () => {
    const [assignedData, availableData] = await Promise.all([
      fetchAssignedCourierOrders(),
      fetchAvailableCourierOrders(),
    ]);
    setOrders(assignedData);
    setAvailable(availableData);
  }, []);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setListStatus('loading');
      setError('');
      setSuccessMessage('');
      try {
        await reloadLists();
        if (cancelled) return;
        setListStatus('success');
      } catch (e) {
        if (cancelled) return;
        setOrders([]);
        setAvailable([]);
        setListStatus('error');
        setError(e?.message || 'Не удалось загрузить данные курьера');
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [reloadLists]);

  async function handleChangeStatus(orderId, nextStatus) {
    setUpdatingOrderId(orderId);
    setError('');
    setSuccessMessage('');
    try {
      const updated = await updateCourierOrderStatus(orderId, nextStatus);
      setOrders((prev) =>
        prev.map((item) =>
          item.orderId === orderId
            ? {
                ...item,
                status: updated.status ?? nextStatus,
                pickedUpAt: updated.pickedUpAt ?? item.pickedUpAt,
                deliveredAt: updated.deliveredAt ?? updated.deliveryTime ?? item.deliveredAt,
              }
            : item
        )
      );
      setSuccessMessage(`Статус заказа #${orderId} обновлен: ${STATUS_LABELS[nextStatus] || nextStatus}`);
    } catch (e) {
      setError(e?.message || 'Не удалось обновить статус доставки');
    } finally {
      setUpdatingOrderId(null);
    }
  }

  async function handleClaim(orderId) {
    setClaimingOrderId(orderId);
    setError('');
    setSuccessMessage('');
    try {
      await claimCourierOrder(orderId);
      await reloadLists();
      setSuccessMessage(`Заказ #${orderId} назначен вам`);
    } catch (e) {
      setError(e?.message || 'Не удалось взять заказ');
    } finally {
      setClaimingOrderId(null);
    }
  }

  return (
    <section className="page dashboard-page">
      <h2>Кабинет курьера</h2>

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
        <h3>Доступные заказы (доставка)</h3>
        <p className="order-card__line" style={{ marginTop: 0 }}>
          Заказы с типом доставки DELIVERY без назначенного курьера. Нажмите «Взять заказ», чтобы они
          появились в блоке ниже.
        </p>

        {listStatus === 'loading' && (
          <p className="state state--loading" aria-busy="true">
            Загрузка…
          </p>
        )}

        {listStatus === 'success' && available.length === 0 && (
          <p className="state state--empty">Нет свободных заказов на доставку.</p>
        )}

        {listStatus === 'success' && available.length > 0 && (
          <ul className="order-list">
            {available.map((o) => (
              <li key={o.id} className="order-card">
                <div className="order-card__head">
                  <h3>Заказ #{o.id}</h3>
                  <span className="badge">{o.status || '—'}</span>
                </div>
                <p className="order-card__line">
                  Ресторан: {o.restaurantId ?? '—'} · Сумма: <strong>{asCurrency(o.totalAmount)}</strong>
                </p>
                <p className="order-card__line">Адрес: {o.deliveryAddress || '—'}</p>
                <p className="order-card__line">Создан: {asDate(o.createdAt)}</p>
                <div className="order-card__actions">
                  <button
                    type="button"
                    className="filters-form__btn"
                    onClick={() => handleClaim(o.id)}
                    disabled={claimingOrderId === o.id}
                  >
                    {claimingOrderId === o.id ? 'Назначение…' : 'Взять заказ'}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="dashboard-block">
        <h3>Мои назначенные заказы</h3>

        {listStatus === 'loading' && (
          <p className="state state--loading" aria-busy="true">
            Загрузка назначений…
          </p>
        )}

        {listStatus === 'error' && (
          <p className="state state--error">
            Не удалось загрузить назначения. Проверьте авторизацию курьера и попробуйте обновить страницу.
          </p>
        )}

        {listStatus === 'success' && sortedOrders.length === 0 && (
          <p className="state state--empty">
            Назначенных заказов пока нет. Возьмите заказ из списка выше или дождитесь назначения от
            администратора.
          </p>
        )}

        {listStatus === 'success' && sortedOrders.length > 0 && (
          <ul className="order-list">
            {sortedOrders.map((item) => (
              <li key={item.assignmentId} className="order-card">
                <div className="order-card__head">
                  <h3>Заказ #{item.orderId}</h3>
                  <span className="badge">{STATUS_LABELS[item.status] || item.status || '—'}</span>
                </div>
                <p className="order-card__line">
                  Assignment: #{item.assignmentId} · Курьер: {item.courierId}
                </p>
                <p className="order-card__line">
                  Назначен: {asDate(item.assignedAt)} · Забран: {asDate(item.pickedUpAt)}
                </p>
                <p className="order-card__line">
                  Доставлен: {asDate(item.deliveredAt ?? item.deliveryTime)}
                </p>
                <div className="order-card__actions">
                  <select
                    className="restaurant-status-select"
                    value={item.status || 'ASSIGNED'}
                    onChange={(e) => handleChangeStatus(item.orderId, e.target.value)}
                    disabled={updatingOrderId === item.orderId}
                  >
                    {COURIER_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {STATUS_LABELS[s] || s}
                      </option>
                    ))}
                  </select>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </section>
  );
}
