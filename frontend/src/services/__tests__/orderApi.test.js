import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  cancelOrder,
  createOrder,
  fetchOrder,
  fetchUserOrders,
} from '../orderApi.js';

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

describe('orderApi', () => {
  afterEach(() => vi.clearAllMocks());

  it('handles successful order endpoints', async () => {
    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: { id: 10 } } }));
    await expect(createOrder({ userId: 1 })).resolves.toEqual({ id: 10 });

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: { id: 11 } } }));
    await expect(fetchOrder(11)).resolves.toEqual({ id: 11 });

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: [{ id: 12 }] } }));
    await expect(fetchUserOrders(1)).resolves.toEqual([{ id: 12 }]);

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: { id: 13, status: 'CANCELLED' } } }));
    await expect(cancelOrder(13)).resolves.toEqual({ id: 13, status: 'CANCELLED' });
  });

  it('throws for success:false and bad response shape', async () => {
    mockApiFetch.mockResolvedValueOnce(
      response({ ok: true, status: 200, body: { success: false, message: 'Conflict status' } })
    );
    await expect(cancelOrder(1)).rejects.toThrow('Conflict status');

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: [] } }));
    await expect(fetchOrder(1)).rejects.toThrow('Ожидался объект заказа в data');
  });

  it('throws for 400/401/403/500', async () => {
    for (const code of [400, 401, 403, 500]) {
      mockApiFetch.mockResolvedValueOnce(
        response({ ok: false, status: code, statusText: 'ERR', body: { message: `E${code}` } })
      );
      await expect(fetchOrder(1)).rejects.toThrow(`E${code}`);
    }
  });

  it('throws for invalid JSON or unexpected envelope', async () => {
    mockApiFetch.mockResolvedValueOnce(response({ body: '{bad-json' }));
    await expect(fetchOrder(1)).rejects.toThrow('Некорректный JSON от сервера');

    mockApiFetch.mockResolvedValueOnce(response({ body: { foo: 1 } }));
    await expect(fetchOrder(1)).rejects.toThrow('Неожиданный формат ответа API');
  });
});
