import { fireEvent, render, screen, waitFor } from '@testing-library/react';
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

  it('supports editing and deleting menu dishes', async () => {
    mockFetchRestaurantOrders.mockResolvedValueOnce([
      { id: 77, userId: 1, status: 'PENDING', totalAmount: Number.NaN, createdAt: 'bad-date' },
    ]);
    mockFetchRestaurantMenu.mockResolvedValueOnce([
      {
        id: 11,
        name: '',
        description: 'Old description',
        price: 200,
        category: '',
        available: false,
        calories: 100,
        imageUrl: 'old.jpg',
        preparationTimeMin: 15,
      },
    ]);
    mockUpdateRestaurantDish.mockResolvedValueOnce({
      id: 11,
      name: 'Updated Dish',
      description: 'New description',
      price: 250,
      category: 'Soup',
      available: true,
      calories: 120,
      imageUrl: 'new.jpg',
      preparationTimeMin: 20,
    });
    mockDeleteRestaurantDish.mockResolvedValueOnce();

    render(<RestaurantDashboardPage />);
    await waitFor(() => expect(screen.getByText('Блюдо #11')).toBeInTheDocument());
    expect(screen.getByText('Old description')).toBeInTheDocument();
    expect(screen.getByText(/bad-date/)).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Редактировать' }));
    await userEvent.clear(screen.getAllByLabelText('Название')[1]);
    await userEvent.type(screen.getAllByLabelText('Название')[1], 'Updated Dish');
    await userEvent.clear(screen.getAllByLabelText('Цена')[1]);
    await userEvent.type(screen.getAllByLabelText('Цена')[1], '250');
    await userEvent.click(screen.getByRole('button', { name: 'Сохранить' }));

    await waitFor(() => expect(screen.getByText('Updated Dish')).toBeInTheDocument());
    expect(mockUpdateRestaurantDish).toHaveBeenCalledWith(11, expect.objectContaining({
      name: 'Updated Dish',
      price: 250,
    }));

    await userEvent.click(screen.getByRole('button', { name: 'Удалить' }));
    await waitFor(() => expect(screen.queryByText('Updated Dish')).not.toBeInTheDocument());
    expect(mockDeleteRestaurantDish).toHaveBeenCalledWith(11);
  });

  it('validates dish forms and shows delete errors', async () => {
    mockFetchRestaurantOrders.mockResolvedValueOnce([]);
    mockFetchRestaurantMenu.mockResolvedValueOnce([
      { id: 12, name: 'Dish', price: 200, category: 'Main', available: true },
    ]);
    mockDeleteRestaurantDish.mockRejectedValueOnce(new Error('Delete failed'));

    render(<RestaurantDashboardPage />);
    await waitFor(() => expect(screen.getByText('Dish')).toBeInTheDocument());

    await userEvent.type(screen.getByLabelText('Название'), 'Invalid Dish');
    await userEvent.type(screen.getByLabelText('Цена'), '-1');
    fireEvent.submit(screen.getByRole('button', { name: 'Добавить блюдо' }).closest('form'));
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(mockCreateRestaurantDish).not.toHaveBeenCalled();

    await userEvent.click(screen.getByRole('button', { name: 'Удалить' }));
    await waitFor(() => expect(screen.getByText('Delete failed')).toBeInTheDocument());
  });
});
