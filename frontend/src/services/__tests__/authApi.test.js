import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  formatAuthErrorMessage,
  loginRequest,
  registerRequest,
} from '../authApi.js';

function response({ ok = true, status = 200, body = {} }) {
  return {
    ok,
    status,
    text: async () => (typeof body === 'string' ? body : JSON.stringify(body)),
  };
}

describe('authApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('returns ok=true data for successful login/register', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValueOnce(
      response({ ok: true, status: 200, body: { success: true, data: { id: 1, role: 'USER' } } })
    );
    await expect(loginRequest({ email: 'u@test.local', password: 'secret' })).resolves.toEqual({
      ok: true,
      data: { id: 1, role: 'USER' },
    });

    vi.spyOn(global, 'fetch').mockResolvedValueOnce(
      response({ ok: true, status: 200, body: { success: true, data: { id: 2, role: 'USER' } } })
    );
    await expect(
      registerRequest({
        name: 'Alex',
        email: 'a@test.local',
        phone: '+79991234567',
        password: 'secret123',
        confirmPassword: 'secret123',
      })
    ).resolves.toEqual({ ok: true, data: { id: 2, role: 'USER' } });
  });

  it('returns ok=false for 400/401/403 and success:false payload', async () => {
    for (const code of [400, 401, 403]) {
      vi.spyOn(global, 'fetch').mockResolvedValueOnce(
        response({ ok: false, status: code, body: { success: false, message: `E${code}` } })
      );
      await expect(loginRequest({ email: 'u@test.local', password: 'x' })).resolves.toEqual({
        ok: false,
        status: code,
        body: { success: false, message: `E${code}` },
      });
      vi.restoreAllMocks();
    }
  });

  it('returns parseError on invalid JSON', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValueOnce(
      response({ ok: false, status: 500, body: '{bad-json' })
    );
    await expect(loginRequest({ email: 'u@test.local', password: 'x' })).resolves.toEqual(
      expect.objectContaining({
        ok: false,
        status: 500,
        parseError: 'Некорректный JSON от сервера',
      })
    );
  });

  it('treats unexpected response shape as ok=false body passthrough', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValueOnce(
      response({ ok: true, status: 200, body: { foo: 'bar' } })
    );
    await expect(loginRequest({ email: 'u@test.local', password: 'x' })).resolves.toEqual({
      ok: false,
      status: 200,
      body: { foo: 'bar' },
    });
  });

  it('formats auth error messages from message/details/fallback', () => {
    expect(formatAuthErrorMessage({ message: 'Bad credentials' }, 'fallback')).toBe(
      'Bad credentials'
    );
    expect(
      formatAuthErrorMessage({ details: ['email: invalid', 'password: required'] }, 'fallback')
    ).toBe('email: invalid; password: required');
    expect(formatAuthErrorMessage(null, 'fallback')).toBe('fallback');
  });
});
