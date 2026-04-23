import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function AppHeader() {
  const navigate = useNavigate();
  const { user, isAuthenticated, role, homeRoute, logout } = useAuth();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  const displayName =
    (typeof user?.name === 'string' && user.name.trim()) ||
    (typeof user?.email === 'string' && user.email) ||
    null;

  const navLinkClass = ({ isActive }) =>
    `app-header__link${isActive ? ' app-header__link--active' : ''}`;

  const roleLabel = role === 'ADMIN'
    ? 'Admin'
    : role === 'COURIER'
      ? 'Courier'
      : role === 'RESTAURANT'
        ? 'Restaurant'
        : 'User';

  return (
    <header className="app-header">
      <div className="app-header__row">
        <div className="app-header__left">
          <div className="app-header__brand">
            <span className="app-header__brand-mark" aria-hidden="true" />
            <div className="app-header__brand-text">
              <h1>
                <Link to={isAuthenticated ? homeRoute : '/'} className="app-header__title-link">
                  Food Delivery
                </Link>
              </h1>
              <p className="app-tagline">Каталог ресторанов</p>
            </div>
          </div>
        </div>
        <div className="app-header__right">
          <nav className="app-header__nav" aria-label="Аккаунт">
            {isAuthenticated && displayName ? (
              <>
                {role === 'USER' && (
                  <>
                    <NavLink to="/orders" className={navLinkClass}>
                      Заказы
                    </NavLink>
                    <NavLink to="/cart" className={navLinkClass}>
                      Корзина
                    </NavLink>
                  </>
                )}
                {role === 'ADMIN' && (
                  <NavLink to="/admin" className={navLinkClass}>
                    Админ-панель
                  </NavLink>
                )}
                {role === 'COURIER' && (
                  <NavLink to="/courier" className={navLinkClass}>
                    Кабинет курьера
                  </NavLink>
                )}
                {role === 'RESTAURANT' && (
                  <NavLink to="/restaurant" className={navLinkClass}>
                    Кабинет ресторана
                  </NavLink>
                )}
              </>
            ) : (
              <>
                <NavLink to="/login" className={navLinkClass}>
                  Вход
                </NavLink>
                <Link to="/register" className="app-header__link app-header__link--primary">
                  Регистрация
                </Link>
              </>
            )}
          </nav>
          {isAuthenticated && displayName && (
            <div className="app-header__account">
              <span className="app-header__role">{roleLabel}</span>
              <span className="app-header__user" title={user?.email ?? ''}>
                {displayName}
              </span>
              <button
                type="button"
                className="app-header__btn app-header__btn--ghost"
                onClick={handleLogout}
              >
                Выйти
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
