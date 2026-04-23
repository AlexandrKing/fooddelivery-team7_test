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

export async function fetchMyCourierReviews() {
  const data = await fetchApiSuccess('/api/client/courier-reviews/mine');
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив отзывов в data');
  }
  return data;
}

/**
 * @param {{ orderId: number, rating: number, comment?: string }} payload
 */
export async function createCourierReview(payload) {
  const data = await fetchApiSuccess('/api/client/courier-reviews', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      orderId: payload.orderId,
      rating: payload.rating,
      comment: payload.comment ?? '',
    }),
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект отзыва в data');
  }
  return data;
}
