/**
 * Общий HTTP-клиент для backend: stateless + HTTP Basic на /api/**,
 * кроме POST /api/auth/register и POST /api/auth/login.
 */

const API_PREFIX = import.meta.env.VITE_API_PREFIX ?? '';

/** @type {() => { email: string, password: string } | null} */
let credentialsGetter = () => null;

/** @type {() => void} */
let unauthorizedHandler = () => {};

/**
 * @param {() => { email: string, password: string } | null} getter
 */
export function setApiCredentialsGetter(getter) {
  credentialsGetter = typeof getter === 'function' ? getter : () => null;
}

/**
 * @param {() => void} handler — сброс сессии и редирект на /login
 */
export function setApiUnauthorizedHandler(handler) {
  unauthorizedHandler = typeof handler === 'function' ? handler : () => {};
}

/**
 * @param {string} email
 * @param {string} password
 * @returns {string}
 */
export function encodeBasicAuthorization(email, password) {
  const pair = `${email}:${password}`;
  const bytes = new TextEncoder().encode(pair);
  let binary = '';
  bytes.forEach((b) => {
    binary += String.fromCharCode(b);
  });
  return `Basic ${btoa(binary)}`;
}

/**
 * @param {string} path — путь с ведущим /, без префикса VITE_API_PREFIX
 */
export function pathRequiresBasicAuth(path) {
  const pathname = (path.startsWith('/') ? path : `/${path}`).split('?')[0];
  if (!pathname.startsWith('/api/')) return false;
  if (pathname === '/api/auth/register' || pathname === '/api/auth/login') {
    return false;
  }
  return true;
}

/**
 * @param {string} path
 * @param {RequestInit} [init]
 * @returns {Promise<Response>}
 */
export async function apiFetch(path, init = {}) {
  const urlPath = path.startsWith('/') ? path : `/${path}`;
  const headers = new Headers(init.headers ?? {});
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }

  const pathname = urlPath.split('?')[0];
  const needsBasic =
    pathname.startsWith('/api/') &&
    pathname !== '/api/auth/register' &&
    pathname !== '/api/auth/login';

  if (needsBasic) {
    const creds = credentialsGetter();
    if (creds?.email != null && creds.password != null) {
      headers.set(
        'Authorization',
        encodeBasicAuthorization(creds.email, creds.password)
      );
    }
  }

  const url = `${API_PREFIX}${urlPath}`;
  const res = await fetch(url, { ...init, headers });

  if (res.status === 401 && needsBasic) {
    unauthorizedHandler();
  }

  return res;
}

export { API_PREFIX };
