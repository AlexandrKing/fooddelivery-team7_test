import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RestaurantMenuPage from '../RestaurantMenuPage.jsx';

const mockUseAuth = vi.fn();
const mockFetchRestaurantMenu = vi.fn();
const mockFetchRestaurantById = vi.fn();
const mockAddCartItem = vi.fn();

vi.mock('../../context/AuthContext.jsx', () => ({
  useAuth: () => mockUseAuth(),
}));

vi.mock('../../services/restaurantsApi.js', () => ({
  fetchRestaurantMenu: (...args) => mockFetchRestaurantMenu(...args),
  fetchRestaurantById: (...args) => mockFetchRestaurantById(...args),
}));

vi.mock('../../services/cartApi.js', () => ({
  addCartItem: (...args) => mockAddCartItem(...args),
}));

vi.mock('../../components/MenuItemCard.jsx', () => ({
  default: ({ item }) => <div>{item.name}</div>,
}));

function renderPage(path = '/restaurants/5/menu', user = { id: 1, role: 'USER' }) {
  mockUseAuth.mockReturnValue({ user });
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/restaurants/:restaurantId/menu" element={<RestaurantMenuPage />} />
        <Route path="/cart" element={<div>Cart page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('RestaurantMenuPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading then empty menu', async () => {
    mockFetchRestaurantMenu.mockResolvedValueOnce([]);
    mockFetchRestaurantById.mockResolvedValueOnce({ id: 5, name: 'Pizza Place' });
    renderPage();
    expect(screen.getByText('Загрузка меню…')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('В меню пока нет доступных блюд.')).toBeInTheDocument());
  });

  it('shows error state on load failure', async () => {
    mockFetchRestaurantMenu.mockRejectedValueOnce(new Error('Ошибка меню'));
    mockFetchRestaurantById.mockResolvedValueOnce({ id: 5, name: 'Pizza Place' });
    renderPage();
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Ошибка меню')).toBeInTheDocument();
  });

  it('renders menu and adds item to cart', async () => {
    mockFetchRestaurantMenu.mockResolvedValueOnce([{ id: 11, name: 'Burger', available: true }]);
    mockFetchRestaurantById.mockResolvedValueOnce({ id: 5, name: 'Pizza Place' });
    mockAddCartItem.mockResolvedValueOnce({ ok: true });
    renderPage();

    await waitFor(() => expect(screen.getByText('Burger')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Добавить в корзину' }));
    await waitFor(() => expect(screen.getByText('Cart page')).toBeInTheDocument());
    expect(mockAddCartItem).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ restaurantId: 5, dishId: 11, quantity: 1 })
    );
  });

  it('shows add-to-cart backend domain error', async () => {
    mockFetchRestaurantMenu.mockResolvedValueOnce([{ id: 11, name: 'Burger', available: true }]);
    mockFetchRestaurantById.mockResolvedValueOnce({ id: 5, name: 'Pizza Place' });
    mockAddCartItem.mockRejectedValueOnce(new Error('Блюдо не найдено'));
    renderPage();

    await waitFor(() => expect(screen.getByText('Burger')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Добавить в корзину' }));
    await waitFor(() => expect(screen.getByText('Блюдо не найдено')).toBeInTheDocument());
  });

  it('renders invalid restaurant id error without fetching menu', async () => {
    renderPage('/restaurants/not-a-number/menu');

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Некорректный идентификатор ресторана.')).toBeInTheDocument();
    expect(mockFetchRestaurantMenu).not.toHaveBeenCalled();
    expect(mockFetchRestaurantById).not.toHaveBeenCalled();
  });

  it('adds auth hint for unauthorized menu load and keeps state title fallback', async () => {
    const error = new Error('Unauthorized');
    error.status = 401;
    mockFetchRestaurantMenu.mockRejectedValueOnce(error);
    mockFetchRestaurantById.mockResolvedValueOnce({ id: 5, name: 'Pizza Place' });
    mockUseAuth.mockReturnValue({ user: { id: 1, role: 'USER' } });

    render(
      <MemoryRouter initialEntries={[{ pathname: '/restaurants/5/menu', state: { restaurantName: 'State Pizza' } }]}>
        <Routes>
          <Route path="/restaurants/:restaurantId/menu" element={<RestaurantMenuPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('State Pizza')).toBeInTheDocument();
    expect(screen.getByText(/Доступ запрещён \(401\)/)).toBeInTheDocument();
  });

  it('shows user id error and disables unavailable menu items', async () => {
    mockFetchRestaurantMenu.mockResolvedValueOnce([
      { id: 11, name: 'Burger', available: true },
      { id: 12, name: 'Soup', available: false },
    ]);
    mockFetchRestaurantById.mockResolvedValueOnce({ id: 5, name: 'Pizza Place' });
    renderPage('/restaurants/5/menu', null);

    await waitFor(() => expect(screen.getByText('Burger')).toBeInTheDocument());
    expect(screen.getAllByRole('button', { name: 'Добавить в корзину' })[0]).toBeEnabled();
    expect(screen.getAllByRole('button', { name: 'Добавить в корзину' })[1]).toBeDisabled();

    await userEvent.click(screen.getAllByRole('button', { name: 'Добавить в корзину' })[0]);
    await waitFor(() =>
      expect(screen.getByText('Не удалось определить пользователя для корзины.')).toBeInTheDocument()
    );
    expect(mockAddCartItem).not.toHaveBeenCalled();
  });
});
