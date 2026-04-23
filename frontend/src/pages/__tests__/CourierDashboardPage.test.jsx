import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CourierDashboardPage from '../CourierDashboardPage.jsx';

const mockFetchAssignedCourierOrders = vi.fn();
const mockFetchAvailableCourierOrders = vi.fn();
const mockClaimCourierOrder = vi.fn();
const mockUpdateCourierOrderStatus = vi.fn();

vi.mock('../../services/courierApi.js', () => ({
  fetchAssignedCourierOrders: (...args) => mockFetchAssignedCourierOrders(...args),
  fetchAvailableCourierOrders: (...args) => mockFetchAvailableCourierOrders(...args),
  claimCourierOrder: (...args) => mockClaimCourierOrder(...args),
  updateCourierOrderStatus: (...args) => mockUpdateCourierOrderStatus(...args),
}));

describe('CourierDashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders loading then empty states for both lists', async () => {
    mockFetchAssignedCourierOrders.mockResolvedValueOnce([]);
    mockFetchAvailableCourierOrders.mockResolvedValueOnce([]);

    render(<CourierDashboardPage />);
    expect(screen.getByText('Загрузка…')).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.getByText('Нет свободных заказов на доставку.')).toBeInTheDocument()
    );
    expect(
      screen.getByText(/Назначенных заказов пока нет\. Возьмите заказ из списка выше/i)
    ).toBeInTheDocument();
  });

  it('shows error state when dashboard loading fails', async () => {
    mockFetchAssignedCourierOrders.mockRejectedValueOnce(new Error('Не удалось загрузить данные курьера'));
    mockFetchAvailableCourierOrders.mockResolvedValueOnce([]);

    render(<CourierDashboardPage />);
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Не удалось загрузить данные курьера')).toBeInTheDocument();
  });

  it('supports claim happy-path and refreshes UI', async () => {
    mockFetchAssignedCourierOrders
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        {
          assignmentId: 1,
          courierId: 9,
          orderId: 101,
          status: 'ASSIGNED',
        },
      ]);
    mockFetchAvailableCourierOrders
      .mockResolvedValueOnce([{ id: 101, restaurantId: 5, status: 'READY', totalAmount: 1000 }])
      .mockResolvedValueOnce([]);
    mockClaimCourierOrder.mockResolvedValueOnce({ assignmentId: 1, orderId: 101, status: 'ASSIGNED' });

    render(<CourierDashboardPage />);
    await waitFor(() => expect(screen.getByText('Заказ #101')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Взять заказ' }));

    await waitFor(() => expect(screen.getByText('Заказ #101 назначен вам')).toBeInTheDocument());
    expect(mockClaimCourierOrder).toHaveBeenCalledWith(101);
    expect(mockFetchAssignedCourierOrders).toHaveBeenCalledTimes(2);
  });

  it('shows claim domain error', async () => {
    mockFetchAssignedCourierOrders.mockResolvedValueOnce([]);
    mockFetchAvailableCourierOrders.mockResolvedValueOnce([{ id: 201, restaurantId: 5, status: 'READY' }]);
    mockClaimCourierOrder.mockRejectedValueOnce(new Error('Заказ уже назначен курьеру'));

    render(<CourierDashboardPage />);
    await waitFor(() => expect(screen.getByText('Заказ #201')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Взять заказ' }));
    await waitFor(() => expect(screen.getByText('Заказ уже назначен курьеру')).toBeInTheDocument());
  });

  it('updates courier order status and shows backend error', async () => {
    mockFetchAssignedCourierOrders.mockResolvedValueOnce([
      {
        assignmentId: 10,
        courierId: 9,
        orderId: 301,
        status: 'ASSIGNED',
      },
    ]);
    mockFetchAvailableCourierOrders.mockResolvedValueOnce([]);
    mockUpdateCourierOrderStatus.mockResolvedValueOnce({
      status: 'DELIVERED',
      deliveryTime: '2026-01-01T12:00:00',
    });

    render(<CourierDashboardPage />);
    await waitFor(() => expect(screen.getByText('Заказ #301')).toBeInTheDocument());
    await userEvent.selectOptions(screen.getByRole('combobox'), 'DELIVERED');
    await waitFor(() =>
      expect(screen.getByText('Статус заказа #301 обновлен: Доставлен')).toBeInTheDocument()
    );

    mockUpdateCourierOrderStatus.mockRejectedValueOnce(new Error('Assigned order not found'));
    await userEvent.selectOptions(screen.getByRole('combobox'), 'CANCELLED');
    await waitFor(() => expect(screen.getByText('Assigned order not found')).toBeInTheDocument());
  });
});
