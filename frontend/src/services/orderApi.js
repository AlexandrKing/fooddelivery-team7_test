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

export async function createOrder(payload) {
  return fetchApiSuccess('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

export async function fetchOrder(orderId) {
  const data = await fetchApiSuccess(`/api/orders/${encodeId(orderId)}`);
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект заказа в data');
  }
  return data;
}

export async function fetchUserOrders(userId) {
  const data = await fetchApiSuccess(`/api/orders/user/${encodeId(userId)}`);
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив заказов в data');
  }
  return data;
}

export async function cancelOrder(orderId) {
  return fetchApiSuccess(`/api/orders/${encodeId(orderId)}/cancel`, {
    method: 'POST',
  });
}
