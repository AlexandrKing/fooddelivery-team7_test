import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { getRoleHomeRoute, useAuth } from '../context/AuthContext.jsx';
import {
  formatAuthErrorMessage,
  loginRequest,
} from '../services/authApi.js';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { loginWithCredentials, isAuthenticated, homeRoute } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const sessionExpired = Boolean(location.state?.sessionExpired);

  if (isAuthenticated) {
    return <Navigate to={homeRoute} replace />;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const result = await loginRequest({
        email: email.trim(),
        password,
      });

      if (result.ok) {
        loginWithCredentials(result.data, email.trim(), password);
        navigate(getRoleHomeRoute(result.data?.role), { replace: true });
        return;
      }

      if ('parseError' in result && result.parseError) {
        setError(result.parseError);
        return;
      }

      setError(
        formatAuthErrorMessage(
          result.body,
          result.status === 401
            ? 'Неверный email или пароль'
            : 'Не удалось войти'
        )
      );
    } catch (err) {
      setError(err?.message || 'Сеть недоступна');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="page auth-page">
      <h2>Вход</h2>

      {sessionExpired && (
        <p className="auth-banner" role="status">
          Сессия истекла или доступ запрещён. Войдите снова.
        </p>
      )}

      <form className="auth-form" onSubmit={handleSubmit}>
        <label className="auth-form__field">
          Email
          <input
            type="email"
            name="email"
            autoComplete="username"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={busy}
          />
        </label>
        <label className="auth-form__field">
          Пароль
          <input
            type="password"
            name="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={busy}
          />
        </label>

        {error ? (
          <div className="state state--error auth-form__error" role="alert">
            {error}
          </div>
        ) : null}

        <button type="submit" className="auth-form__submit" disabled={busy}>
          {busy ? 'Вход…' : 'Войти'}
        </button>
      </form>

      <p className="auth-alt">
        Нет аккаунта? <Link to="/register">Регистрация</Link>
      </p>
    </section>
  );
}
