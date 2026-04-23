import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  fetchRestaurantById,
  fetchRestaurantMenu,
  fetchRestaurants,
} from '../restaurantsApi.js';

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

describe('restaurantsApi', () => {
  afterEach(() => vi.clearAllMocks());

  it('loads restaurants and forwards filter params', async () => {
    mockApiFetch.mockResolvedValueOnce(
      response({ body: { success: true, data: [{ id: 1, name: 'Pizza' }] } })
    );
    await expect(fetchRestaurants({ rating: 4.5, deliveryTime: 30 })).resolves.toEqual([
      { id: 1, name: 'Pizza' },
    ]);
    expect(mockApiFetch.mock.calls[0][0]).toContain('/api/restaurants?rating=4.5&deliveryTime=30');
  });

  it('loads restaurant and menu successfully', async () => {
    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: { id: 1 } } }));
    await expect(fetchRestaurantById(1)).resolves.toEqual({ id: 1 });

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: [{ id: 10 }] } }));
    await expect(fetchRestaurantMenu(1)).resolves.toEqual([{ id: 10 }]);
  });

  it('throws for http errors and success:false', async () => {
    mockApiFetch.mockResolvedValueOnce(
      response({ ok: false, status: 403, statusText: 'Forbidden', body: { message: 'Forbidden' } })
    );
    await expect(fetchRestaurants()).rejects.toThrow('Forbidden');

    mockApiFetch.mockResolvedValueOnce(
      response({ ok: true, status: 200, body: { success: false, message: 'Domain error' } })
    );
    await expect(fetchRestaurants()).rejects.toThrow('Domain error');
  });

  it('throws for invalid json and invalid response shape', async () => {
    mockApiFetch.mockResolvedValueOnce(response({ body: '{bad-json' }));
    await expect(fetchRestaurants()).rejects.toThrow('Некорректный JSON от сервера');

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: {} } }));
    await expect(fetchRestaurants()).rejects.toThrow('Ожидался массив ресторанов в data');
  });
});
