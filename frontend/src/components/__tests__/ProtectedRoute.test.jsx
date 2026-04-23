import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import ProtectedRoute from '../ProtectedRoute.jsx';

const mockUseAuth = vi.fn();

vi.mock('../../context/AuthContext.jsx', () => ({
  useAuth: () => mockUseAuth(),
}));

function renderWithRoutes(allowedRoles = null) {
  return render(
    <MemoryRouter initialEntries={['/protected']}>
      <Routes>
        <Route
          path="/protected"
          element={
            <ProtectedRoute allowedRoles={allowedRoles}>
              <div>Protected content</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div>Login page</div>} />
        <Route path="/admin" element={<div>Admin home</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('ProtectedRoute', () => {
  it('redirects unauthenticated users to login', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      role: 'USER',
      homeRoute: '/',
    });
    renderWithRoutes(['USER']);
    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('renders children for authenticated users with allowed role', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      role: 'ADMIN',
      homeRoute: '/admin',
    });
    renderWithRoutes(['ADMIN']);
    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });

  it('redirects authenticated users with wrong role to their home', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      role: 'ADMIN',
      homeRoute: '/admin',
    });
    renderWithRoutes(['USER']);
    expect(screen.getByText('Admin home')).toBeInTheDocument();
  });

  it('supports multiple role cases without loading state', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      role: 'COURIER',
      homeRoute: '/courier',
    });
    renderWithRoutes(['COURIER', 'ADMIN']);
    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });
});
