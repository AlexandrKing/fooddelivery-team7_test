import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import OrderHistoryPage from '../OrderHistoryPage.jsx';

const mockUseAuth = vi.fn();
const mockFetchUserOrders = vi.fn();
const mockFetchOrder = vi.fn();
const mockCancelOrder = vi.fn();
const mockFetchMyCourierReviews = vi.fn();
const mockCreateCourierReview = vi.fn();

vi.mock('../../context/AuthContext.jsx', () => ({
  useAuth: () => mockUseAuth(),
}));

vi.mock('../../services/orderApi.js', () => ({
  fetchUserOrders: (...args) => mockFetchUserOrders(...args),
  fetchOrder: (...args) => mockFetchOrder(...args),
  cancelOrder: (...args) => mockCancelOrder(...args),
}));

vi.mock('../../services/courierReviewApi.js', () => ({
  fetchMyCourierReviews: (...args) => mockFetchMyCourierReviews(...args),
  createCourierReview: (...args) => mockCreateCourierReview(...args),
}));

function renderPage() {
  mockUseAuth.mockReturnValue({ user: { id: 1, role: 'USER' } });
  return render(
    <MemoryRouter>
      <OrderHistoryPage />
    </MemoryRouter>
  );
}

describe('OrderHistoryPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockFetchMyCourierReviews.mockResolvedValue([]);
  });

  it('shows loading then empty state', async () => {
    mockFetchUserOrders.mockResolvedValueOnce([]);
    renderPage();
    expect(screen.getByText('Загрузка заказов…')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Заказов пока нет.')).toBeInTheDocument());
  });

  it('shows error state when loading fails', async () => {
    mockFetchUserOrders.mockRejectedValueOnce(new Error('Ошибка загрузки'));
    renderPage();
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Ошибка загрузки')).toBeInTheDocument();
  });

  it('renders orders and supports loading details', async () => {
    mockFetchUserOrders.mockResolvedValueOnce([
      { id: 101, status: 'PENDING', totalAmount: 700, createdAt: '2026-01-01T10:00:00', deliveryType: 'DELIVERY', deliveryAddress: 'Lenina 1' },
    ]);
    mockFetchOrder.mockResolvedValueOnce({
      id: 101,
      items: [{ id: 1, menuItemId: 10, name: 'Burger', quantity: 2, price: 350 }],
    });
    renderPage();

    await waitFor(() => expect(screen.getByText('Заказ #101')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Показать позиции' }));
    await waitFor(() => expect(screen.getByText('Burger · 2 × 350 ₽')).toBeInTheDocument());
  });

  it('handles cancel action and updates UI', async () => {
    mockFetchUserOrders.mockResolvedValueOnce([
      { id: 101, status: 'PENDING', totalAmount: 700, createdAt: '2026-01-01T10:00:00', deliveryType: 'DELIVERY', deliveryAddress: 'Lenina 1' },
    ]);
    mockCancelOrder.mockResolvedValueOnce({
      id: 101,
      status: 'CANCELLED',
      totalAmount: 700,
      createdAt: '2026-01-01T10:00:00',
      deliveryType: 'DELIVERY',
      deliveryAddress: 'Lenina 1',
    });
    renderPage();

    await waitFor(() => expect(screen.getByText('Заказ #101')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Отменить' }));
    await waitFor(() => expect(screen.getByText('Отменён')).toBeInTheDocument());
  });

  it('shows backend domain error for cancel/review actions', async () => {
    mockFetchUserOrders.mockResolvedValueOnce([
      { id: 201, status: 'PENDING', totalAmount: 500, createdAt: '2026-01-01T10:00:00', deliveryType: 'DELIVERY', deliveryAddress: 'Lenina 1' },
    ]);
    mockCancelOrder.mockRejectedValueOnce(new Error('Нельзя отменить заказ в текущем статусе'));
    renderPage();
    await waitFor(() => expect(screen.getByText('Заказ #201')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Отменить' }));
    await waitFor(() =>
      expect(screen.getByText('Нельзя отменить заказ в текущем статусе')).toBeInTheDocument()
    );
  });
});
