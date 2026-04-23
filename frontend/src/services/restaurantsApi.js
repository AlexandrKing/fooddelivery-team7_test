/**
 * RestaurantController @RequestMapping("/api/restaurants")
 * - GET /api/restaurants
 * - GET /api/restaurants/{id}
 * - GET /api/restaurants/{id}/menu
 */

import { apiFetch } from './apiClient.js';

/**
 * @param {string} path - абсолютный путь от корня сайта, напр. /api/restaurants/1/menu
 * @returns {Promise<unknown>} поле data из ApiSuccessResponse
 */
async function fetchApiSuccess(path) {
  const res = await apiFetch(path, {
    headers: { Accept: 'application/json' },
  });

  const text = await res.text();
  let json;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    throw new Error('Некорректный JSON от сервера');
  }

  if (!res.ok) {
    const err = new Error(
      json?.message || `HTTP ${res.status}: ${res.statusText || 'ошибка'}`
    );
    err.status = res.status;
    throw err;
  }

  if (!json || typeof json.success !== 'boolean') {
    throw new Error('Неожиданный формат ответа API');
  }

  if (!json.success) {
    throw new Error(json.message || 'Запрос не выполнен');
  }

  return json.data;
}

/**
 * GET /api/restaurants[?rating=&deliveryTime=]
 * Backend: rating = минимальный рейтинг, deliveryTime = максимальное время доставки (мин).
 * @param {{ rating?: number, deliveryTime?: number }} [params]
 * @returns {Promise<object[]>}
 */
export async function fetchRestaurants(params = {}) {
  const search = new URLSearchParams();
  if (params.rating != null && params.rating !== '') {
    search.set('rating', String(params.rating));
  }
  if (params.deliveryTime != null && params.deliveryTime !== '') {
    search.set('deliveryTime', String(params.deliveryTime));
  }
  const qs = search.toString();
  const path = `/api/restaurants${qs ? `?${qs}` : ''}`;
  const data = await fetchApiSuccess(path);
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив ресторанов в data');
  }
  return data;
}

/**
 * GET /api/restaurants/{id}
 * @returns {Promise<object>}
 */
export async function fetchRestaurantById(restaurantId) {
  const id = encodeURIComponent(String(restaurantId));
  const data = await fetchApiSuccess(`/api/restaurants/${id}`);
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект ресторана в data');
  }
  return data;
}

/**
 * GET /api/restaurants/{id}/menu → MenuItemResponse[]
 * @returns {Promise<object[]>}
 */
export async function fetchRestaurantMenu(restaurantId) {
  const id = encodeURIComponent(String(restaurantId));
  const data = await fetchApiSuccess(`/api/restaurants/${id}/menu`);
  if (!Array.isArray(data)) {
    throw new Error('Ожидался массив блюд в data');
  }
  return data;
}
