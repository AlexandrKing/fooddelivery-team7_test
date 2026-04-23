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

export async function fetchAdminAccounts() {
  const data = await fetchApiSuccess('/api/admin/accounts');
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив аккаунтов в data');
  }
  return data;
}

export async function setAdminAccountActive(accountId, active) {
  const data = await fetchApiSuccess(`/api/admin/accounts/${encodeId(accountId)}/active`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ active }),
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект аккаунта в data');
  }
  return data;
}

export async function fetchAdminOrders() {
  const data = await fetchApiSuccess('/api/admin/orders');
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив заказов в data');
  }
  return data;
}

export async function fetchAdminCourierReviews() {
  const data = await fetchApiSuccess('/api/admin/courier-reviews');
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив отзывов в data');
  }
  return data;
}

export async function deleteAdminCourierReview(reviewId) {
  const data = await fetchApiSuccess(`/api/admin/courier-reviews/${encodeId(reviewId)}`, {
    method: 'DELETE',
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект ответа в data');
  }
  return data;
}

