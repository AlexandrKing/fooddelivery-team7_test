import { apiFetch } from './apiClient.js';

function asError(status, message) {
  const err = new Error(message);
  err.status = status;
  return err;
}

async function fetchApiSuccess(path, init) {
  const res = await apiFetch(path, init);
  const text = await res.text();
  let json;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    throw asError(res.status, 'Некорректный JSON от сервера');
  }

  if (!res.ok) {
    throw asError(
      res.status,
      json?.message || `HTTP ${res.status}: ${res.statusText || 'ошибка'}`
    );
  }

  if (!json || typeof json.success !== 'boolean') {
    throw asError(res.status, 'Неожиданный формат ответа API');
  }
  if (!json.success) {
    throw asError(res.status, json.message || 'Запрос не выполнен');
  }
  return json.data;
}

function encodeId(id) {
  return encodeURIComponent(String(id));
}

export async function fetchAssignedCourierOrders() {
  const data = await fetchApiSuccess('/api/courier/orders/assigned');
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив назначенных заказов в data');
  }
  return data;
}

export async function fetchAvailableCourierOrders() {
  const data = await fetchApiSuccess('/api/courier/orders/available');
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив доступных заказов в data');
  }
  return data;
}

export async function claimCourierOrder(orderId) {
  const data = await fetchApiSuccess(`/api/courier/orders/${encodeId(orderId)}/claim`, {
    method: 'POST',
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект назначенного заказа в data');
  }
  return data;
}

export async function updateCourierOrderStatus(orderId, status) {
  const data = await fetchApiSuccess(`/api/courier/orders/${encodeId(orderId)}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект назначенного заказа в data');
  }
  return data;
}

