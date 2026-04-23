import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AdminDashboardPage from '../AdminDashboardPage.jsx';

const mockFetchAdminAccounts = vi.fn();
const mockSetAdminAccountActive = vi.fn();
const mockFetchAdminOrders = vi.fn();
const mockFetchAdminCourierReviews = vi.fn();
const mockDeleteAdminCourierReview = vi.fn();

vi.mock('../../services/adminApi.js', () => ({
  fetchAdminAccounts: (...args) => mockFetchAdminAccounts(...args),
  setAdminAccountActive: (...args) => mockSetAdminAccountActive(...args),
  fetchAdminOrders: (...args) => mockFetchAdminOrders(...args),
  fetchAdminCourierReviews: (...args) => mockFetchAdminCourierReviews(...args),
  deleteAdminCourierReview: (...args) => mockDeleteAdminCourierReview(...args),
}));

describe('AdminDashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
  });

  it('shows loading then empty states', async () => {
    mockFetchAdminAccounts.mockResolvedValueOnce([]);
    mockFetchAdminOrders.mockResolvedValueOnce([]);
    mockFetchAdminCourierReviews.mockResolvedValueOnce([]);
    render(<AdminDashboardPage />);
    expect(screen.getByText('Загрузка аккаунтов…')).toBeInTheDocument();
    await waitFor(() =>
      expect(screen.getByText('По текущим фильтрам аккаунты не найдены.')).toBeInTheDocument()
    );
    expect(screen.getByText('Заказы не найдены.')).toBeInTheDocument();
    expect(screen.getByText('Отзывов пока нет.')).toBeInTheDocument();
  });

  it('shows error state when initial load fails', async () => {
    mockFetchAdminAccounts.mockRejectedValueOnce(new Error('Не удалось загрузить админ-панель'));
    mockFetchAdminOrders.mockResolvedValueOnce([]);
    mockFetchAdminCourierReviews.mockResolvedValueOnce([]);
    render(<AdminDashboardPage />);
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Не удалось загрузить админ-панель')).toBeInTheDocument();
  });

  it('loads data and toggles account active happy-path with UI refresh', async () => {
    mockFetchAdminAccounts.mockResolvedValueOnce([
      { id: 1, email: 'user@test.local', role: 'USER', active: true },
    ]);
    mockFetchAdminOrders.mockResolvedValueOnce([
      { id: 10, userId: 1, restaurantId: 2, status: 'PENDING', totalAmount: 500 },
    ]);
    mockFetchAdminCourierReviews.mockResolvedValueOnce([]);
    mockSetAdminAccountActive.mockResolvedValueOnce({
      id: 1,
      email: 'user@test.local',
      role: 'USER',
      active: false,
    });

    render(<AdminDashboardPage />);
    await waitFor(() => expect(screen.getByText('Account #1')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Деактивировать' }));
    await waitFor(() =>
      expect(screen.getByText('Аккаунт user@test.local деактивирован')).toBeInTheDocument()
    );
    expect(screen.getByText('INACTIVE')).toBeInTheDocument();
  });

  it('shows toggle account error', async () => {
    mockFetchAdminAccounts.mockResolvedValueOnce([
      { id: 2, email: 'blocked@test.local', role: 'USER', active: false },
    ]);
    mockFetchAdminOrders.mockResolvedValueOnce([]);
    mockFetchAdminCourierReviews.mockResolvedValueOnce([]);
    mockSetAdminAccountActive.mockRejectedValueOnce(new Error('Account not found'));

    render(<AdminDashboardPage />);
    await waitFor(() => expect(screen.getByText('Account #2')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Активировать' }));
    await waitFor(() => expect(screen.getByText('Account not found')).toBeInTheDocument());
  });
});
