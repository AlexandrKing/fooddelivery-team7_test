import { API_PREFIX } from './apiClient.js';

/**
 * @param {Response} res
 * @param {unknown} json
 * @returns {{ ok: true, data: unknown } | { ok: false, status: number, body: unknown }}
 */
function interpretJsonResponse(res, json) {
  if (res.ok && json && typeof json === 'object' && json.success === true) {
    return { ok: true, data: json.data };
  }
  return { ok: false, status: res.status, body: json };
}

/**
 * @param {string} path
 * @param {object} body
 */
async function postJson(path, body) {
  const url = `${API_PREFIX}${path.startsWith('/') ? path : `/${path}`}`;
  const res = await fetch(url, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });

  const text = await res.text();
  let json;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    return {
      ok: false,
      status: res.status,
      body: null,
      parseError: 'Некорректный JSON от сервера',
    };
  }

  const result = interpretJsonResponse(res, json);
  if (result.ok) return result;
  return { ok: false, status: res.status, body: json };
}

/**
 * @param {object} payload — name, email, phone, password, confirmPassword
 */
export function registerRequest(payload) {
  return postJson('/api/auth/register', payload);
}

/**
 * @param {object} payload — email, password
 */
export function loginRequest(payload) {
  return postJson('/api/auth/login', payload);
}

/**
 * Сообщение для UI из тела ApiErrorResponse или fallback.
 * @param {unknown} body
 * @param {string} [fallback]
 */
export function formatAuthErrorMessage(body, fallback = 'Запрос не выполнен') {
  if (!body || typeof body !== 'object') return fallback;
  const msg = body.message;
  if (typeof msg === 'string' && msg.trim()) return msg;
  const details = body.details;
  if (Array.isArray(details) && details.length > 0) {
    return details.filter((d) => typeof d === 'string').join('; ') || fallback;
  }
  return fallback;
}
