import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RestaurantDashboardPage from '../RestaurantDashboardPage.jsx';

const mockFetchRestaurantOrders = vi.fn();
const mockUpdateRestaurantOrderStatus = vi.fn();
const mockFetchRestaurantMenu = vi.fn();
const mockCreateRestaurantDish = vi.fn();
const mockUpdateRestaurantDish = vi.fn();
const mockDeleteRestaurantDish = vi.fn();

vi.mock('../../services/restaurantApi.js', () => ({
  fetchRestaurantOrders: (...args) => mockFetchRestaurantOrders(...args),
  updateRestaurantOrderStatus: (...args) => mockUpdateRestaurantOrderStatus(...args),
  fetchRestaurantMenu: (...args) => mockFetchRestaurantMenu(...args),
  createRestaurantDish: (...args) => mockCreateRestaurantDish(...args),
  updateRestaurantDish: (...args) => mockUpdateRestaurantDish(...args),
  deleteRestaurantDish: (...args) => mockDeleteRestaurantDish(...args),
}));

describe('RestaurantDashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading then empty states', async () => {
    mockFetchRestaurantOrders.mockResolvedValueOnce([]);
    mockFetchRestaurantMenu.mockResolvedValueOnce([]);
    render(<RestaurantDashboardPage />);
    expect(screen.getByText('Загрузка заказов…')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('У ресторана пока нет заказов.')).toBeInTheDocument());
    expect(screen.getByText('В меню пока нет блюд.')).toBeInTheDocument();
  });

  it('shows global error state on initial load failure', async () => {
    mockFetchRestaurantOrders.mockRejectedValueOnce(new Error('Не удалось загрузить кабинет ресторана'));
    mockFetchRestaurantMenu.mockResolvedValueOnce([]);
    render(<RestaurantDashboardPage />);
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Не удалось загрузить кабинет ресторана')).toBeInTheDocument();
  });

  it('loads orders and updates order status with ui refresh', async () => {
    mockFetchRestaurantOrders.mockResolvedValueOnce([
      { id: 77, userId: 1, status: 'PENDING', totalAmount: 500, createdAt: '2026-01-01T10:00:00' },
    ]);
    mockFetchRestaurantMenu.mockResolvedValueOnce([]);
    mockUpdateRestaurantOrderStatus.mockResolvedValueOnce({
      id: 77,
      userId: 1,
      status: 'READY',
      totalAmount: 500,
      createdAt: '2026-01-01T10:00:00',
    });

    render(<RestaurantDashboardPage />);
    await waitFor(() => expect(screen.getByText('Заказ #77')).toBeInTheDocument());
    await userEvent.selectOptions(screen.getByRole('combobox'), 'READY');
    await waitFor(() => expect(screen.getByText('Статус заказа #77 обновлен')).toBeInTheDocument());
    expect(screen.getAllByText('READY').length).toBeGreaterThan(0);
  });

  it('shows backend/domain error when order status update fails', async () => {
    mockFetchRestaurantOrders.mockResolvedValueOnce([
      { id: 88, userId: 1, status: 'PENDING', totalAmount: 500, createdAt: '2026-01-01T10:00:00' },
    ]);
    mockFetchRestaurantMenu.mockResolvedValueOnce([]);
    mockUpdateRestaurantOrderStatus.mockRejectedValueOnce(
      new Error('Order does not belong to restaurant')
    );

    render(<RestaurantDashboardPage />);
    await waitFor(() => expect(screen.getByText('Заказ #88')).toBeInTheDocument());
    await userEvent.selectOptions(screen.getByRole('combobox'), 'READY');
    await waitFor(() =>
      expect(screen.getByText('Order does not belong to restaurant')).toBeInTheDocument()
    );
  });

  it('supports menu create dish happy-path and error-path', async () => {
    mockFetchRestaurantOrders.mockResolvedValueOnce([]);
    mockFetchRestaurantMenu.mockResolvedValueOnce([]);
    mockCreateRestaurantDish
      .mockResolvedValueOnce({
        id: 501,
        name: 'New Dish',
        price: 300,
        category: 'Main',
        available: true,
      })
      .mockRejectedValueOnce(new Error('Dish not found'));

    render(<RestaurantDashboardPage />);
    await waitFor(() => expect(screen.getByText('Добавить блюдо')).toBeInTheDocument());

    await userEvent.type(screen.getByLabelText('Название'), 'New Dish');
    await userEvent.type(screen.getByLabelText('Цена'), '300');
    await userEvent.click(screen.getByRole('button', { name: 'Добавить блюдо' }));
    await waitFor(() => expect(screen.getByText(/добавлено/i)).toBeInTheDocument());
    expect(screen.getByText('New Dish')).toBeInTheDocument();

    await userEvent.type(screen.getByLabelText('Название'), 'Another');
    await userEvent.type(screen.getByLabelText('Цена'), '350');
    await userEvent.click(screen.getByRole('button', { name: 'Добавить блюдо' }));
    await waitFor(() => expect(screen.getByText('Dish not found')).toBeInTheDocument());
  });
});
