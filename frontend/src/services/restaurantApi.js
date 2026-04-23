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

export async function fetchRestaurantOrders() {
  const data = await fetchApiSuccess('/api/restaurant/orders');
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив заказов ресторана в data');
  }
  return data;
}

export async function updateRestaurantOrderStatus(orderId, status) {
  const data = await fetchApiSuccess(`/api/restaurant/orders/${encodeId(orderId)}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект заказа в data');
  }
  return data;
}

export async function fetchRestaurantMenu() {
  const data = await fetchApiSuccess('/api/restaurant/menu');
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив меню ресторана в data');
  }
  return data;
}

export async function createRestaurantDish(payload) {
  const data = await fetchApiSuccess('/api/restaurant/menu', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект блюда в data');
  }
  return data;
}

export async function updateRestaurantDish(dishId, payload) {
  const data = await fetchApiSuccess(`/api/restaurant/menu/${encodeId(dishId)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект блюда в data');
  }
  return data;
}

export async function deleteRestaurantDish(dishId) {
  return fetchApiSuccess(`/api/restaurant/menu/${encodeId(dishId)}`, {
    method: 'DELETE',
  });
}

