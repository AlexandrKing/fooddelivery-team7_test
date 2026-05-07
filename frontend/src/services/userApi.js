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
    throw asError(res.status, json?.message || `HTTP ${res.status}: ${res.statusText || 'ошибка'}`);
  }
  if (!json || typeof json.success !== 'boolean') {
    throw asError(res.status, 'Неожиданный формат ответа API');
  }
  if (!json.success) {
    throw asError(res.status, json.message || 'Запрос не выполнен');
  }
  return json.data;
}

export async function fetchUserProfile() {
  const data = await fetchApiSuccess('/api/users/me');
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект профиля пользователя в data');
  }
  return data;
}

export async function updateUserProfile(profile) {
  const data = await fetchApiSuccess('/api/users/me', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fullName: profile.fullName,
      phone: profile.phone,
    }),
  });
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    throw new Error('Ожидался объект профиля пользователя в data');
  }
  return data;
}
