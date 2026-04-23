import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, getRoleHomeRoute, useAuth } from '../AuthContext.jsx';
import { apiFetch } from '../../services/apiClient.js';

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{location.pathname}</div>;
}

function AuthProbe() {
  const auth = useAuth();
  return (
    <div>
      <div data-testid="auth">{String(auth.isAuthenticated)}</div>
      <div data-testid="role">{auth.role}</div>
      <div data-testid="email">{auth.email ?? ''}</div>
      <div data-testid="home">{auth.homeRoute}</div>
      <button
        type="button"
        onClick={() =>
          auth.loginWithCredentials(
            { id: 1, role: 'ADMIN', linkedAdminId: 5 },
            'admin@test.local',
            'secret'
          )
        }
      >
        login-admin
      </button>
      <button
        type="button"
        onClick={() =>
          auth.loginWithCredentials({ id: 2, role: 'USER' }, 'user@test.local', 'secret')
        }
      >
        login-user
      </button>
      <button type="button" onClick={() => auth.logout()}>
        logout
      </button>
    </div>
  );
}

function renderAuth() {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <AuthProvider>
        <LocationProbe />
        <AuthProbe />
      </AuthProvider>
    </MemoryRouter>
  );
}

describe('AuthContext', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('initializes unauthenticated state', () => {
    renderAuth();
    expect(screen.getByTestId('auth')).toHaveTextContent('false');
    expect(screen.getByTestId('role')).toHaveTextContent('USER');
    expect(screen.getByTestId('home')).toHaveTextContent('/');
  });

  it('handles successful login and role update', async () => {
    renderAuth();
    await userEvent.click(screen.getByRole('button', { name: 'login-admin' }));
    expect(screen.getByTestId('auth')).toHaveTextContent('true');
    expect(screen.getByTestId('role')).toHaveTextContent('ADMIN');
    expect(screen.getByTestId('email')).toHaveTextContent('admin@test.local');
    expect(screen.getByTestId('home')).toHaveTextContent('/admin');
  });

  it('handles logout and clears session state', async () => {
    renderAuth();
    await userEvent.click(screen.getByRole('button', { name: 'login-user' }));
    expect(screen.getByTestId('auth')).toHaveTextContent('true');
    await userEvent.click(screen.getByRole('button', { name: 'logout' }));
    expect(screen.getByTestId('auth')).toHaveTextContent('false');
    expect(screen.getByTestId('email')).toHaveTextContent('');
    expect(screen.getByTestId('role')).toHaveTextContent('USER');
  });

  it('resets auth and redirects on expired session (401)', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue({ status: 401 });
    renderAuth();
    await userEvent.click(screen.getByRole('button', { name: 'login-user' }));
    expect(screen.getByTestId('auth')).toHaveTextContent('true');

    await apiFetch('/api/orders');

    await waitFor(() => {
      expect(screen.getByTestId('auth')).toHaveTextContent('false');
    });
    expect(screen.getByTestId('location')).toHaveTextContent('/login');
  });

  it('maps role home routes', () => {
    expect(getRoleHomeRoute('USER')).toBe('/');
    expect(getRoleHomeRoute('ADMIN')).toBe('/admin');
    expect(getRoleHomeRoute('COURIER')).toBe('/courier');
    expect(getRoleHomeRoute('RESTAURANT')).toBe('/restaurant');
    expect(getRoleHomeRoute('UNKNOWN')).toBe('/');
  });
});
