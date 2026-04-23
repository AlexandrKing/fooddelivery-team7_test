import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RestaurantsPage from '../RestaurantsPage.jsx';

const mockFetchRestaurants = vi.fn();

vi.mock('../../services/restaurantsApi.js', () => ({
  fetchRestaurants: (...args) => mockFetchRestaurants(...args),
}));

vi.mock('../../components/RestaurantCard.jsx', () => ({
  default: ({ restaurant }) => <div>{restaurant.name}</div>,
}));

describe('RestaurantsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  function renderPage() {
    return render(
      <MemoryRouter>
        <RestaurantsPage />
      </MemoryRouter>
    );
  }

  it('shows loading then empty state', async () => {
    mockFetchRestaurants.mockResolvedValueOnce([]);
    renderPage();
    expect(screen.getByText('Загрузка…')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Список пуст.')).toBeInTheDocument());
  });

  it('shows error state on load failure', async () => {
    mockFetchRestaurants.mockRejectedValueOnce(new Error('Ошибка загрузки'));
    renderPage();
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Ошибка загрузки')).toBeInTheDocument();
  });

  it('renders restaurant list and applies filters', async () => {
    mockFetchRestaurants
      .mockResolvedValueOnce([{ id: 1, name: 'Pizza Place' }])
      .mockResolvedValueOnce([{ id: 2, name: 'Sushi Bar' }]);
    renderPage();
    await waitFor(() => expect(screen.getByText('Pizza Place')).toBeInTheDocument());

    await userEvent.type(screen.getByLabelText('Мин. рейтинг'), '4.5');
    await userEvent.type(screen.getByLabelText('Макс. время доставки (мин)'), '30');
    await userEvent.click(screen.getByRole('button', { name: 'Применить' }));

    await waitFor(() => expect(screen.getByText('Sushi Bar')).toBeInTheDocument());
    expect(mockFetchRestaurants).toHaveBeenLastCalledWith({ rating: 4.5, deliveryTime: 30 });
  });
});
