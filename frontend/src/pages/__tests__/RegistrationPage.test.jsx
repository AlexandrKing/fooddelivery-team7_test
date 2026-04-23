import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RegistrationPage from '../RegistrationPage.jsx';

const mockUseAuth = vi.fn();
const mockRegisterRequest = vi.fn();
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
  getRoleHomeRoute: (role) => (role === 'USER' ? '/' : '/admin'),
}));

vi.mock('../../services/authApi.js', () => ({
  registerRequest: (...args) => mockRegisterRequest(...args),
  formatAuthErrorMessage: (...args) => mockFormatAuthErrorMessage(...args),
}));

function renderRegistration() {
  mockUseAuth.mockReturnValue({
    loginWithCredentials: mockLoginWithCredentials,
    isAuthenticated: false,
    homeRoute: '/',
  });
  return render(
    <MemoryRouter initialEntries={['/register']}>
      <RegistrationPage />
    </MemoryRouter>
  );
}

async function fillValidForm() {
  await userEvent.type(screen.getByLabelText('Имя'), 'Alex');
  await userEvent.type(screen.getByLabelText('Email'), 'alex@test.local');
  await userEvent.type(screen.getByLabelText('Телефон'), '+79991234567');
  await userEvent.type(screen.getByLabelText('Пароль'), 'secret123');
  await userEvent.type(screen.getByLabelText('Подтверждение пароля'), 'secret123');
}

describe('RegistrationPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockFormatAuthErrorMessage.mockReturnValue('Регистрация не выполнена');
  });

  it('registers successfully and redirects', async () => {
    mockRegisterRequest.mockResolvedValueOnce({
      ok: true,
      data: { id: 1, role: 'USER' },
    });
    renderRegistration();

    await fillValidForm();
    await userEvent.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));

    await waitFor(() => expect(mockLoginWithCredentials).toHaveBeenCalledTimes(1));
    expect(mockLoginWithCredentials).toHaveBeenCalledWith(
      { id: 1, role: 'USER' },
      'alex@test.local',
      'secret123'
    );
    expect(mockNavigate).toHaveBeenCalledWith('/', { replace: true });
  });

  it('prevents submit on password mismatch', async () => {
    renderRegistration();
    await userEvent.type(screen.getByLabelText('Имя'), 'Alex');
    await userEvent.type(screen.getByLabelText('Email'), 'alex@test.local');
    await userEvent.type(screen.getByLabelText('Телефон'), '+79991234567');
    await userEvent.type(screen.getByLabelText('Пароль'), 'secret123');
    await userEvent.type(screen.getByLabelText('Подтверждение пароля'), 'different');
    await userEvent.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));

    expect(screen.getByText('Пароли не совпадают')).toBeInTheDocument();
    expect(mockRegisterRequest).not.toHaveBeenCalled();
  });

  it('shows backend validation/domain errors', async () => {
    mockRegisterRequest.mockResolvedValueOnce({
      ok: false,
      status: 400,
      body: { message: 'Email уже используется' },
    });
    mockFormatAuthErrorMessage.mockReturnValueOnce('Email уже используется');
    renderRegistration();

    await fillValidForm();
    await userEvent.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));

    await waitFor(() => expect(screen.getByText('Email уже используется')).toBeInTheDocument());
  });

  it('shows parse/network error from registration flow', async () => {
    mockRegisterRequest.mockResolvedValueOnce({
      ok: false,
      status: 500,
      body: null,
      parseError: 'Некорректный JSON от сервера',
    });
    renderRegistration();
    await fillValidForm();
    await userEvent.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));
    await waitFor(() => expect(screen.getByText('Некорректный JSON от сервера')).toBeInTheDocument());
  });
});
