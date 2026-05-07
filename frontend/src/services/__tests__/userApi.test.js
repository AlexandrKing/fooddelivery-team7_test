import { afterEach, describe, expect, it, vi } from 'vitest';
import { fetchUserProfile, updateUserProfile } from '../userApi.js';

const mockApiFetch = vi.fn();
vi.mock('../apiClient.js', () => ({
  apiFetch: (...args) => mockApiFetch(...args),
}));

function response({ ok = true, status = 200, statusText = 'OK', body = {} }) {
  return {
    ok,
    status,
    statusText,
    text: async () => (typeof body === 'string' ? body : JSON.stringify(body)),
  };
}

describe('userApi', () => {
  afterEach(() => vi.clearAllMocks());

  it('loads current user profile', async () => {
    const profile = { id: 1, fullName: 'User One', email: 'u@test.local', phone: '+79990000000' };
    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: profile } }));

    await expect(fetchUserProfile()).resolves.toEqual(profile);
    expect(mockApiFetch).toHaveBeenCalledWith('/api/users/me', undefined);
  });

  it('updates allowed profile fields only', async () => {
    const updated = { id: 1, fullName: 'New Name', email: 'u@test.local', phone: '+79991112233' };
    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: updated } }));

    await expect(
      updateUserProfile({ fullName: 'New Name', phone: '+79991112233', email: 'ignored@test.local' })
    ).resolves.toEqual(updated);

    expect(mockApiFetch).toHaveBeenCalledWith('/api/users/me', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName: 'New Name', phone: '+79991112233' }),
    });
  });

  it('throws for http errors, invalid json and failed api responses', async () => {
    mockApiFetch.mockResolvedValueOnce(
      response({ ok: false, status: 403, statusText: 'Forbidden', body: { message: 'Forbidden' } })
    );
    await expect(fetchUserProfile()).rejects.toThrow('Forbidden');

    mockApiFetch.mockResolvedValueOnce(response({ body: '{bad-json' }));
    await expect(fetchUserProfile()).rejects.toThrow('Некорректный JSON от сервера');

    mockApiFetch.mockResolvedValueOnce(
      response({ ok: true, status: 200, body: { success: false, message: 'Domain error' } })
    );
    await expect(fetchUserProfile()).rejects.toThrow('Domain error');
  });

  it('throws for invalid envelope or profile shapes', async () => {
    mockApiFetch.mockResolvedValueOnce(response({ body: { data: {} } }));
    await expect(fetchUserProfile()).rejects.toThrow('Неожиданный формат ответа API');

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: [] } }));
    await expect(fetchUserProfile()).rejects.toThrow(
      'Ожидался объект профиля пользователя в data'
    );

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: null } }));
    await expect(updateUserProfile({ fullName: 'User', phone: '+7' })).rejects.toThrow(
      'Ожидался объект профиля пользователя в data'
    );
  });
});
