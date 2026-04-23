import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  apiFetch,
  encodeBasicAuthorization,
  pathRequiresBasicAuth,
  setApiCredentialsGetter,
  setApiUnauthorizedHandler,
} from '../apiClient.js';

describe('apiClient', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    setApiCredentialsGetter(() => null);
    setApiUnauthorizedHandler(() => {});
  });

  it('encodes basic auth header', () => {
    const encoded = encodeBasicAuthorization('user@test.local', 'secret');
    expect(encoded.startsWith('Basic ')).toBe(true);
  });

  it('detects protected and public api paths', () => {
    expect(pathRequiresBasicAuth('/api/orders')).toBe(true);
    expect(pathRequiresBasicAuth('/api/auth/login')).toBe(false);
    expect(pathRequiresBasicAuth('/health')).toBe(false);
  });

  it('adds authorization header for protected endpoints', async () => {
    const fetchSpy = vi.spyOn(global, 'fetch').mockResolvedValue({ status: 200 });
    setApiCredentialsGetter(() => ({ email: 'user@test.local', password: 'secret' }));

    await apiFetch('/api/orders');

    const [, init] = fetchSpy.mock.calls[0];
    const auth = new Headers(init.headers).get('Authorization');
    expect(auth).toMatch(/^Basic /);
  });

  it('does not add authorization header for auth endpoints', async () => {
    const fetchSpy = vi.spyOn(global, 'fetch').mockResolvedValue({ status: 200 });
    setApiCredentialsGetter(() => ({ email: 'user@test.local', password: 'secret' }));

    await apiFetch('/api/auth/login', { method: 'POST' });

    const [, init] = fetchSpy.mock.calls[0];
    const auth = new Headers(init.headers).get('Authorization');
    expect(auth).toBeNull();
  });

  it('calls unauthorized handler on protected 401', async () => {
    const handler = vi.fn();
    vi.spyOn(global, 'fetch').mockResolvedValue({ status: 401 });
    setApiCredentialsGetter(() => ({ email: 'user@test.local', password: 'secret' }));
    setApiUnauthorizedHandler(handler);

    await apiFetch('/api/orders');

    expect(handler).toHaveBeenCalledTimes(1);
  });
});
