import { Navigate, Route, Routes } from 'react-router-dom';
import AppHeader from './components/AppHeader.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { useAuth } from './context/AuthContext.jsx';
import AdminDashboardPage from './pages/AdminDashboardPage.jsx';
import CartPage from './pages/CartPage.jsx';
import CourierDashboardPage from './pages/CourierDashboardPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import OrderHistoryPage from './pages/OrderHistoryPage.jsx';
import RegistrationPage from './pages/RegistrationPage.jsx';
import RestaurantDashboardPage from './pages/RestaurantDashboardPage.jsx';
import RestaurantsPage from './pages/RestaurantsPage.jsx';
import RestaurantMenuPage from './pages/RestaurantMenuPage.jsx';

function RoleHomeFallback() {
  const { isAuthenticated, homeRoute } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <Navigate to={homeRoute} replace />;
}

export default function App() {
  return (
    <div className="app">
      <AppHeader />
      <main className="app-main">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegistrationPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute allowedRoles={['USER']}>
                <RestaurantsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/cart"
            element={
              <ProtectedRoute allowedRoles={['USER']}>
                <CartPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/orders"
            element={
              <ProtectedRoute allowedRoles={['USER']}>
                <OrderHistoryPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/restaurants/:restaurantId/menu"
            element={
              <ProtectedRoute allowedRoles={['USER']}>
                <RestaurantMenuPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/courier"
            element={
              <ProtectedRoute allowedRoles={['COURIER']}>
                <CourierDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/restaurant"
            element={
              <ProtectedRoute allowedRoles={['RESTAURANT']}>
                <RestaurantDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<RoleHomeFallback />} />
        </Routes>
      </main>
    </div>
  );
}
