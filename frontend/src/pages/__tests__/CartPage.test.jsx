import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CartPage from '../CartPage.jsx';

const mockUseAuth = vi.fn();
const mockFetchCart = vi.fn();
const mockUpdateCartItemQuantity = vi.fn();
const mockRemoveCartItem = vi.fn();
const mockAddCartItem = vi.fn();
const mockClearCart = vi.fn();
const mockCreateOrder = vi.fn();

vi.mock('../../context/AuthContext.jsx', () => ({
  useAuth: () => mockUseAuth(),
}));

vi.mock('../../services/cartApi.js', () => ({
  fetchCart: (...args) => mockFetchCart(...args),
  updateCartItemQuantity: (...args) => mockUpdateCartItemQuantity(...args),
  removeCartItem: (...args) => mockRemoveCartItem(...args),
  addCartItem: (...args) => mockAddCartItem(...args),
  clearCart: (...args) => mockClearCart(...args),
}));

vi.mock('../../services/orderApi.js', () => ({
  createOrder: (...args) => mockCreateOrder(...args),
}));

function renderCartPage() {
  mockUseAuth.mockReturnValue({ user: { id: 1, role: 'USER' } });
  return render(
    <MemoryRouter initialEntries={['/cart']}>
      <Routes>
        <Route path="/cart" element={<CartPage />} />
        <Route path="/orders" element={<div>Orders page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('CartPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows empty state after successful load', async () => {
    mockFetchCart.mockResolvedValueOnce({ userId: 1, restaurantId: 2, totalAmount: 0, items: [] });
    renderCartPage();
    expect(screen.getByText('Загрузка корзины…')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Корзина пуста.')).toBeInTheDocument());
  });

  it('shows error state when loading fails', async () => {
    mockFetchCart.mockRejectedValueOnce(new Error('Backend недоступен'));
    renderCartPage();
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Backend недоступен')).toBeInTheDocument();
  });

  it('updates and removes cart items', async () => {
    mockFetchCart.mockResolvedValueOnce({
      userId: 1,
      restaurantId: 2,
      totalAmount: 700,
      items: [{ id: 11, menuItemId: 101, quantity: 2, name: 'Burger', price: 350 }],
    });
    mockAddCartItem.mockResolvedValueOnce({
      userId: 1,
      restaurantId: 2,
      totalAmount: 1050,
      items: [{ id: 11, menuItemId: 101, quantity: 3, name: 'Burger', price: 350 }],
    });
    mockRemoveCartItem.mockResolvedValueOnce({
      userId: 1,
      restaurantId: 2,
      totalAmount: 0,
      items: [],
    });

    renderCartPage();
    await waitFor(() => expect(screen.getByText('Burger')).toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: '+' }));
    expect(mockAddCartItem).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ restaurantId: 2, dishId: 101, quantity: 1 })
    );

    await userEvent.click(screen.getByRole('button', { name: 'Удалить' }));
    expect(mockRemoveCartItem).toHaveBeenCalledWith(1, 11);
  });

  it('places order successfully and navigates to orders page', async () => {
    mockFetchCart.mockResolvedValueOnce({
      userId: 1,
      restaurantId: 2,
      totalAmount: 700,
      items: [{ id: 11, menuItemId: 101, quantity: 2, name: 'Burger', price: 350 }],
    });
    mockCreateOrder.mockResolvedValueOnce({ id: 500 });

    renderCartPage();
    await waitFor(() => expect(screen.getByText('Burger')).toBeInTheDocument());

    await userEvent.type(screen.getByPlaceholderText('ул. Пример, д. 1'), 'Lenina 10');
    const datetimeInput = screen.getByLabelText('Время');
    await userEvent.type(datetimeInput, '2026-05-01T12:30');
    await userEvent.click(screen.getByRole('button', { name: 'Оформить заказ' }));

    await waitFor(() => expect(screen.getByText('Orders page')).toBeInTheDocument());
    expect(mockCreateOrder).toHaveBeenCalledTimes(1);
  });

  it('shows backend domain error on checkout failure', async () => {
    mockFetchCart.mockResolvedValueOnce({
      userId: 1,
      restaurantId: 2,
      totalAmount: 700,
      items: [{ id: 11, menuItemId: 101, quantity: 2, name: 'Burger', price: 350 }],
    });
    mockCreateOrder.mockRejectedValueOnce(new Error('Корзина пуста'));

    renderCartPage();
    await waitFor(() => expect(screen.getByText('Burger')).toBeInTheDocument());
    await userEvent.type(screen.getByPlaceholderText('ул. Пример, д. 1'), 'Lenina 10');
    await userEvent.type(screen.getByLabelText('Время'), '2026-05-01T12:30');
    await userEvent.click(screen.getByRole('button', { name: 'Оформить заказ' }));

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Корзина пуста')).toBeInTheDocument();
  });
});
