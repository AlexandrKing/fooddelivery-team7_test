import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  addCartItem,
  clearCart,
  fetchCart,
  removeCartItem,
  updateCartItemQuantity,
} from '../cartApi.js';

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

describe('cartApi', () => {
  afterEach(() => vi.clearAllMocks());

  it('handles 200 success for fetch and mutations', async () => {
    mockApiFetch.mockResolvedValueOnce(
      response({ body: { success: true, data: { userId: 1, items: [] } } })
    );
    await expect(fetchCart(1)).resolves.toEqual({ userId: 1, items: [] });

    mockApiFetch.mockResolvedValueOnce(
      response({ body: { success: true, data: { userId: 1, items: [{ id: 1 }] } } })
    );
    await expect(addCartItem(1, { restaurantId: 2, dishId: 10, quantity: 1 })).resolves.toEqual(
      { userId: 1, items: [{ id: 1 }] }
    );

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: { userId: 1 } } }));
    await expect(updateCartItemQuantity(1, 1, 2)).resolves.toEqual({ userId: 1 });

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: { userId: 1 } } }));
    await expect(removeCartItem(1, 1)).resolves.toEqual({ userId: 1 });

    mockApiFetch.mockResolvedValueOnce(response({ body: { success: true, data: { userId: 1 } } }));
    await expect(clearCart(1)).resolves.toEqual({ userId: 1 });
  });

  it('throws on success:false and unexpected shape', async () => {
    mockApiFetch.mockResolvedValueOnce(
      response({ status: 400, ok: true, body: { success: false, message: 'Запрос не выполнен' } })
    );
    await expect(fetchCart(1)).rejects.toThrow('Запрос не выполнен');

    mockApiFetch.mockResolvedValueOnce(response({ body: { foo: 'bar' } }));
    await expect(fetchCart(1)).rejects.toThrow('Неожиданный формат ответа API');
  });

  it('throws for 400/401/403/500 responses', async () => {
    for (const code of [400, 401, 403, 500]) {
      mockApiFetch.mockResolvedValueOnce(
        response({
          ok: false,
          status: code,
          statusText: 'ERR',
          body: { message: `E${code}` },
        })
      );
      await expect(fetchCart(1)).rejects.toThrow(`E${code}`);
    }
  });

  it('throws for invalid JSON', async () => {
    mockApiFetch.mockResolvedValueOnce(
      response({ ok: true, status: 200, body: '{bad-json' })
    );
    await expect(fetchCart(1)).rejects.toThrow('Некорректный JSON от сервера');
  });
});
