import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import MenuItemCard from '../components/MenuItemCard.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { addCartItem } from '../services/cartApi.js';
import {
  fetchRestaurantById,
  fetchRestaurantMenu,
} from '../services/restaurantsApi.js';

function authHint(status) {
  if (status !== 401) return null;
  return ' Доступ запрещён (401). Выполните вход снова.';
}

export default function RestaurantMenuPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { restaurantId } = useParams();
  const location = useLocation();
  const stateName = location.state?.restaurantName;

  const parsedId = useMemo(() => {
    const n = Number(restaurantId);
    return Number.isFinite(n) && n > 0 ? n : null;
  }, [restaurantId]);

  const [status, setStatus] = useState('loading');
  const [items, setItems] = useState([]);
  const [title, setTitle] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [busyAddId, setBusyAddId] = useState(null);

  const userId = useMemo(() => {
    const id = Number(user?.id);
    return Number.isFinite(id) && id > 0 ? id : null;
  }, [user]);

  useEffect(() => {
    if (parsedId == null) {
      setStatus('error');
      setErrorMessage('Некорректный идентификатор ресторана.');
      setItems([]);
      setTitle('');
      return;
    }

    let cancelled = false;

    async function load() {
      setStatus('loading');
      setErrorMessage('');
      try {
        const [menu, restaurant] = await Promise.all([
          fetchRestaurantMenu(parsedId),
          fetchRestaurantById(parsedId),
        ]);
        if (cancelled) return;
        setItems(menu);
        setTitle(
          (typeof stateName === 'string' && stateName.trim() !== ''
            ? stateName
            : restaurant?.name) || `Ресторан #${parsedId}`
        );
        setStatus('success');
      } catch (e) {
        if (cancelled) return;
        const hint = authHint(e?.status);
        setErrorMessage(
          (e?.message || 'Не удалось загрузить меню') + (hint || '')
        );
        setItems([]);
        setTitle(
          typeof stateName === 'string' && stateName.trim() !== ''
            ? stateName
            : `Ресторан #${parsedId}`
        );
        setStatus('error');
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [parsedId, stateName]);

  if (parsedId == null) {
    return (
      <section className="page menu-page">
        <nav className="page-nav">
          <Link to="/">← К ресторанам</Link>
        </nav>
        <div className="state state--error" role="alert">
          <strong>Ошибка</strong>
          <p>{errorMessage}</p>
        </div>
      </section>
    );
  }

  async function handleAddToCart(item) {
    if (userId == null) {
      setErrorMessage('Не удалось определить пользователя для корзины.');
      return;
    }
    setBusyAddId(item.id);
    setErrorMessage('');
    try {
      await addCartItem(userId, {
        restaurantId: parsedId,
        dishId: item.id,
        quantity: 1,
      });
      navigate('/cart');
    } catch (e) {
      setErrorMessage(e?.message || 'Не удалось добавить блюдо в корзину');
    } finally {
      setBusyAddId(null);
    }
  }

  return (
    <section className="page menu-page">
      <nav className="page-nav">
        <Link to="/">← К ресторанам</Link>
      </nav>

      <h2 className="menu-page__title">{title}</h2>
      <p className="menu-page__subtitle">Меню · ресторан #{parsedId}</p>

      {status === 'loading' && (
        <p className="state state--loading" aria-busy="true">
          Загрузка меню…
        </p>
      )}

      {status === 'error' && (
        <div className="state state--error" role="alert">
          <strong>Ошибка</strong>
          <p>{errorMessage}</p>
        </div>
      )}

      {status === 'success' && items.length === 0 && (
        <p className="state state--empty">В меню пока нет доступных блюд.</p>
      )}

      {status === 'success' && items.length > 0 && (
        <ul className="menu-item-list">
          {items.map((item) => (
            <li key={item.id}>
              <MenuItemCard item={item} />
              <div className="menu-item-actions">
                <button
                  type="button"
                  className="filters-form__btn"
                  disabled={busyAddId === item.id || item.available === false}
                  onClick={() => handleAddToCart(item)}
                >
                  {busyAddId === item.id ? 'Добавление…' : 'Добавить в корзину'}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {errorMessage && status === 'success' && (
        <div className="state state--error" role="alert">
          <p>{errorMessage}</p>
        </div>
      )}
    </section>
  );
}
