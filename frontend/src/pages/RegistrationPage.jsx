import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { getRoleHomeRoute, useAuth } from '../context/AuthContext.jsx';
import {
  formatAuthErrorMessage,
  registerRequest,
} from '../services/authApi.js';

export default function RegistrationPage() {
  const navigate = useNavigate();
  const { loginWithCredentials, isAuthenticated, homeRoute } = useAuth();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  if (isAuthenticated) {
    return <Navigate to={homeRoute} replace />;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Пароли не совпадают');
      return;
    }

    setBusy(true);
    try {
      const result = await registerRequest({
        name: name.trim(),
        email: email.trim(),
        phone: phone.trim(),
        password,
        confirmPassword,
      });

      if (result.ok) {
        // Сразу авторизуем для Basic на защищённых /api/** (без /api/auth/me).
        loginWithCredentials(result.data, email.trim(), password);
        navigate(getRoleHomeRoute(result.data?.role), { replace: true });
        return;
      }

      if ('parseError' in result && result.parseError) {
        setError(result.parseError);
        return;
      }

      setError(
        formatAuthErrorMessage(result.body, 'Регистрация не выполнена')
      );
    } catch (err) {
      setError(err?.message || 'Сеть недоступна');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="page auth-page">
      <h2>Регистрация</h2>

      <p className="auth-hint">
        Телефон в формате <code>+79XXXXXXXXX</code>
      </p>

      <form className="auth-form" onSubmit={handleSubmit}>
        <label className="auth-form__field">
          Имя
          <input
            type="text"
            name="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={busy}
          />
        </label>
        <label className="auth-form__field">
          Email
          <input
            type="email"
            name="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={busy}
          />
        </label>
        <label className="auth-form__field">
          Телефон
          <input
            type="tel"
            name="phone"
            autoComplete="tel"
            placeholder="+79991234567"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            required
            disabled={busy}
          />
        </label>
        <label className="auth-form__field">
          Пароль
          <input
            type="password"
            name="password"
            autoComplete="new-password"
            minLength={6}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={busy}
          />
        </label>
        <label className="auth-form__field">
          Подтверждение пароля
          <input
            type="password"
            name="confirmPassword"
            autoComplete="new-password"
            minLength={6}
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
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
          {busy ? 'Отправка…' : 'Зарегистрироваться'}
        </button>
      </form>

      <p className="auth-alt">
        Уже есть аккаунт? <Link to="/login">Вход</Link>
      </p>
    </section>
  );
}
