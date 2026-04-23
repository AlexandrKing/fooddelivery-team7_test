import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import App from '../App.jsx';

const mockUseAuth = vi.fn();

vi.mock('../context/AuthContext.jsx', () => ({
  useAuth: () => mockUseAuth(),
}));

vi.mock('../components/AppHeader.jsx', () => ({
  default: () => <div>Header</div>,
}));
vi.mock('../pages/LoginPage.jsx', () => ({ default: () => <div>Login page</div> }));
vi.mock('../pages/RegistrationPage.jsx', () => ({ default: () => <div>Register page</div> }));
vi.mock('../pages/RestaurantsPage.jsx', () => ({ default: () => <div>Restaurants page</div> }));
vi.mock('../pages/CartPage.jsx', () => ({ default: () => <div>Cart page</div> }));
vi.mock('../pages/OrderHistoryPage.jsx', () => ({ default: () => <div>Orders page</div> }));
vi.mock('../pages/RestaurantMenuPage.jsx', () => ({ default: () => <div>Restaurant menu page</div> }));
vi.mock('../pages/AdminDashboardPage.jsx', () => ({ default: () => <div>Admin page</div> }));
vi.mock('../pages/CourierDashboardPage.jsx', () => ({ default: () => <div>Courier page</div> }));
vi.mock('../pages/RestaurantDashboardPage.jsx', () => ({ default: () => <div>Restaurant page</div> }));

function renderApp(path, authState) {
  mockUseAuth.mockReturnValue(authState);
  return render(
    <MemoryRouter initialEntries={[path]}>
      <App />
    </MemoryRouter>
  );
}

describe('App routes', () => {
  it('allows public routes without auth', () => {
    renderApp('/login', { isAuthenticated: false, role: 'USER', homeRoute: '/' });
    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('blocks protected user routes when unauthenticated', () => {
    renderApp('/cart', { isAuthenticated: false, role: 'USER', homeRoute: '/' });
    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('renders protected route for allowed USER role', () => {
    renderApp('/orders', { isAuthenticated: true, role: 'USER', homeRoute: '/' });
    expect(screen.getByText('Orders page')).toBeInTheDocument();
  });

  it('enforces role-specific route guard', () => {
    renderApp('/admin', { isAuthenticated: true, role: 'USER', homeRoute: '/' });
    expect(screen.getByText('Restaurants page')).toBeInTheDocument();
  });

  it('renders admin page for ADMIN and fallback redirects by role', () => {
    renderApp('/unknown-path', { isAuthenticated: true, role: 'ADMIN', homeRoute: '/admin' });
    expect(screen.getByText('Admin page')).toBeInTheDocument();
  });
});
