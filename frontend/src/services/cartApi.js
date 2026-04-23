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

export async function fetchCart(userId) {
  const data = await fetchApiSuccess(`/api/carts/${encodeId(userId)}`);
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект корзины в data');
  }
  return data;
}

export async function addCartItem(userId, payload) {
  return fetchApiSuccess(`/api/carts/${encodeId(userId)}/items`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

export async function updateCartItemQuantity(userId, itemId, quantity) {
  return fetchApiSuccess(`/api/carts/${encodeId(userId)}/items/${encodeId(itemId)}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ quantity }),
  });
}

export async function removeCartItem(userId, itemId) {
  return fetchApiSuccess(`/api/carts/${encodeId(userId)}/items/${encodeId(itemId)}`, {
    method: 'DELETE',
  });
}

export async function clearCart(userId) {
  return fetchApiSuccess(`/api/carts/${encodeId(userId)}/items`, {
    method: 'DELETE',
  });
}
