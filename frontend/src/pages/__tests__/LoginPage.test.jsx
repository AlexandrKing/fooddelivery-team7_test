import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LoginPage from '../LoginPage.jsx';

const mockUseAuth = vi.fn();
const mockLoginRequest = vi.fn();
const mockFormatAuthErrorMessage = vi.fn();
const mockLoginWithCredentials = vi.fn();
const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('../../context/AuthContext.jsx', () => ({
  useAuth: () => mockUseAuth(),
  getRoleHomeRoute: (role) => (role === 'ADMIN' ? '/admin' : '/'),
}));

vi.mock('../../services/authApi.js', () => ({
  loginRequest: (...args) => mockLoginRequest(...args),
  formatAuthErrorMessage: (...args) => mockFormatAuthErrorMessage(...args),
}));

function renderLogin(initialPath = '/login', locationState = undefined) {
  mockUseAuth.mockReturnValue({
    loginWithCredentials: mockLoginWithCredentials,
    isAuthenticated: false,
    homeRoute: '/',
  });
  return render(
    <MemoryRouter initialEntries={[{ pathname: initialPath, state: locationState }]}>
      <LoginPage />
    </MemoryRouter>
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockFormatAuthErrorMessage.mockReturnValue('Не удалось войти');
  });

  it('logs in successfully and redirects by role', async () => {
    mockLoginRequest.mockResolvedValueOnce({
      ok: true,
      data: { id: 1, role: 'ADMIN' },
    });
    renderLogin();

    await userEvent.type(screen.getByLabelText('Email'), 'admin@test.local');
    await userEvent.type(screen.getByLabelText('Пароль'), 'secret');
    await userEvent.click(screen.getByRole('button', { name: 'Войти' }));

    await waitFor(() => expect(mockLoginWithCredentials).toHaveBeenCalledTimes(1));
    expect(mockLoginWithCredentials).toHaveBeenCalledWith(
      { id: 1, role: 'ADMIN' },
      'admin@test.local',
      'secret'
    );
    expect(mockNavigate).toHaveBeenCalledWith('/admin', { replace: true });
  });

  it('shows 401/400 login error message', async () => {
    mockLoginRequest.mockResolvedValueOnce({
      ok: false,
      status: 401,
      body: { message: 'Неверный email или пароль' },
    });
    mockFormatAuthErrorMessage.mockReturnValueOnce('Неверный email или пароль');
    renderLogin();

    await userEvent.type(screen.getByLabelText('Email'), 'user@test.local');
    await userEvent.type(screen.getByLabelText('Пароль'), 'wrong');
    await userEvent.click(screen.getByRole('button', { name: 'Войти' }));

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Неверный email или пароль')).toBeInTheDocument();
  });

  it('shows forbidden-like backend error message when provided', async () => {
    mockLoginRequest.mockResolvedValueOnce({
      ok: false,
      status: 403,
      body: { message: 'Аккаунт деактивирован' },
    });
    mockFormatAuthErrorMessage.mockReturnValueOnce('Аккаунт деактивирован');
    renderLogin();

    await userEvent.type(screen.getByLabelText('Email'), 'user@test.local');
    await userEvent.type(screen.getByLabelText('Пароль'), 'secret');
    await userEvent.click(screen.getByRole('button', { name: 'Войти' }));

    await waitFor(() => expect(screen.getByText('Аккаунт деактивирован')).toBeInTheDocument());
  });

  it('shows parse/network errors from login flow', async () => {
    mockLoginRequest.mockResolvedValueOnce({
      ok: false,
      status: 500,
      body: null,
      parseError: 'Некорректный JSON от сервера',
    });
    renderLogin();
    await userEvent.type(screen.getByLabelText('Email'), 'user@test.local');
    await userEvent.type(screen.getByLabelText('Пароль'), 'secret');
    await userEvent.click(screen.getByRole('button', { name: 'Войти' }));
    await waitFor(() => expect(screen.getByText('Некорректный JSON от сервера')).toBeInTheDocument());
  });

  it('shows session expired banner when redirected from 401 flow', () => {
    renderLogin('/login', { sessionExpired: true });
    expect(screen.getByText('Сессия истекла или доступ запрещён. Войдите снова.')).toBeInTheDocument();
  });
});
