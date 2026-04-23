import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import {
  addCartItem,
  clearCart,
  fetchCart,
  removeCartItem,
  updateCartItemQuantity,
} from '../services/cartApi.js';
import { createOrder } from '../services/orderApi.js';

function asCurrency(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  return `${n.toLocaleString('ru-RU')} ₽`;
}

export default function CartPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const userId = useMemo(() => {
    const id = Number(user?.id);
    return Number.isFinite(id) && id > 0 ? id : null;
  }, [user]);

  const [status, setStatus] = useState('loading');
  const [cart, setCart] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [busyItemId, setBusyItemId] = useState(null);
  const [busyClear, setBusyClear] = useState(false);
  const [busyCheckout, setBusyCheckout] = useState(false);
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [deliveryType, setDeliveryType] = useState('DELIVERY');
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [deliveryTimeLocal, setDeliveryTimeLocal] = useState('');

  const loadCart = useCallback(async () => {
    if (userId == null) {
      setStatus('error');
      setErrorMessage('Не удалось определить пользователя для корзины.');
      setCart(null);
      return;
    }

    setStatus('loading');
    setErrorMessage('');
    try {
      const data = await fetchCart(userId);
      setCart(data);
      setStatus('success');
    } catch (e) {
      setCart(null);
      setStatus('error');
      setErrorMessage(e?.message || 'Не удалось загрузить корзину');
    }
  }, [userId]);

  useEffect(() => {
    loadCart();
  }, [loadCart]);

  const items = Array.isArray(cart?.items) ? cart.items : [];

  async function handleChangeQuantity(itemId, nextQuantity) {
    if (userId == null) return;
    setBusyItemId(itemId);
    setErrorMessage('');
    try {
      const updated = await updateCartItemQuantity(userId, itemId, nextQuantity);
      setCart(updated);
    } catch (e) {
      setErrorMessage(e?.message || 'Не удалось изменить количество');
    } finally {
      setBusyItemId(null);
    }
  }

  async function handleRemoveItem(itemId) {
    if (userId == null) return;
    setBusyItemId(itemId);
    setErrorMessage('');
    try {
      const updated = await removeCartItem(userId, itemId);
      setCart(updated);
    } catch (e) {
      setErrorMessage(e?.message || 'Не удалось удалить позицию');
    } finally {
      setBusyItemId(null);
    }
  }

  async function handleClearCart() {
    if (userId == null) return;
    setBusyClear(true);
    setErrorMessage('');
    try {
      const updated = await clearCart(userId);
      setCart(updated);
    } catch (e) {
      setErrorMessage(e?.message || 'Не удалось очистить корзину');
    } finally {
      setBusyClear(false);
    }
  }

  async function handleAddOneMore(item) {
    if (userId == null) return;
    setBusyItemId(item.id);
    setErrorMessage('');
    try {
      const updated = await addCartItem(userId, {
        restaurantId: cart?.restaurantId ?? item.restaurantId,
        dishId: item.menuItemId,
        quantity: 1,
      });
      setCart(updated);
    } catch (e) {
      setErrorMessage(e?.message || 'Не удалось добавить количество');
    } finally {
      setBusyItemId(null);
    }
  }

  async function handleCheckout(e) {
    e.preventDefault();
    if (userId == null) return;
    if (!deliveryAddress.trim()) {
      setErrorMessage('Укажите адрес доставки');
      return;
    }
    if (!deliveryTimeLocal) {
      setErrorMessage('Укажите время доставки');
      return;
    }
    if (!cart?.restaurantId) {
      setErrorMessage('Не удалось определить ресторан корзины');
      return;
    }

    setBusyCheckout(true);
    setErrorMessage('');
    try {
      const deliveryTime =
        deliveryTimeLocal.length === 16
          ? `${deliveryTimeLocal}:00`
          : deliveryTimeLocal;
      await createOrder({
        userId,
        restaurantId: cart.restaurantId,
        deliveryAddress: deliveryAddress.trim(),
        deliveryType,
        deliveryTime,
        paymentMethod,
      });
      navigate('/orders');
    } catch (e2) {
      setErrorMessage(e2?.message || 'Не удалось оформить заказ');
    } finally {
      setBusyCheckout(false);
    }
  }

  return (
    <section className="page cart-page">
      <nav className="page-nav">
        <Link to="/">← К ресторанам</Link>
      </nav>

      <h2>Корзина</h2>

      {status === 'loading' && (
        <p className="state state--loading" aria-busy="true">
          Загрузка корзины…
        </p>
      )}

      {status === 'error' && (
        <div className="state state--error" role="alert">
          <strong>Ошибка</strong>
          <p>{errorMessage}</p>
        </div>
      )}

      {status === 'success' && items.length === 0 && (
        <p className="state state--empty">Корзина пуста.</p>
      )}

      {status === 'success' && items.length > 0 && (
        <>
          <ul className="cart-item-list">
            {items.map((item) => {
              const quantity = Number(item.quantity) || 0;
              const price = Number(item.price) || 0;
              const lineTotal = quantity * price;
              const rowBusy = busyItemId === item.id || busyClear;

              return (
                <li key={item.id} className="cart-item-card">
                  <div className="cart-item-card__head">
                    <h3>{item.name || `Блюдо #${item.menuItemId}`}</h3>
                    <span>{asCurrency(price)}</span>
                  </div>
                  <p className="cart-item-card__meta">
                    item #{item.id} · menu #{item.menuItemId}
                  </p>
                  <div className="cart-item-card__controls">
                    <button
                      type="button"
                      onClick={() => handleChangeQuantity(item.id, quantity - 1)}
                      disabled={rowBusy || quantity <= 1}
                    >
                      −
                    </button>
                    <span>{quantity}</span>
                    <button
                      type="button"
                      onClick={() => handleAddOneMore(item)}
                      disabled={rowBusy}
                    >
                      +
                    </button>
                    <button
                      type="button"
                      className="cart-item-card__remove"
                      onClick={() => handleRemoveItem(item.id)}
                      disabled={rowBusy}
                    >
                      Удалить
                    </button>
                  </div>
                  <p className="cart-item-card__sum">Сумма: {asCurrency(lineTotal)}</p>
                </li>
              );
            })}
          </ul>

          <div className="cart-summary">
            <p>
              Итого: <strong>{asCurrency(cart?.totalAmount)}</strong>
            </p>
            <button
              type="button"
              className="filters-form__btn filters-form__btn--secondary"
              onClick={handleClearCart}
              disabled={busyClear || busyItemId != null}
            >
              Очистить корзину
            </button>
          </div>

          <form className="checkout-form" onSubmit={handleCheckout}>
            <h3>Оформление заказа</h3>
            <label className="checkout-form__field">
              Адрес доставки
              <input
                type="text"
                value={deliveryAddress}
                onChange={(e) => setDeliveryAddress(e.target.value)}
                placeholder="ул. Пример, д. 1"
                disabled={busyCheckout}
                required
              />
            </label>
            <div className="checkout-form__grid">
              <label className="checkout-form__field">
                Тип
                <select
                  value={deliveryType}
                  onChange={(e) => setDeliveryType(e.target.value)}
                  disabled={busyCheckout}
                >
                  <option value="DELIVERY">DELIVERY</option>
                  <option value="PICKUP">PICKUP</option>
                </select>
              </label>
              <label className="checkout-form__field">
                Оплата
                <select
                  value={paymentMethod}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                  disabled={busyCheckout}
                >
                  <option value="CARD">CARD</option>
                  <option value="CASH">CASH</option>
                </select>
              </label>
              <label className="checkout-form__field">
                Время
                <input
                  type="datetime-local"
                  value={deliveryTimeLocal}
                  onChange={(e) => setDeliveryTimeLocal(e.target.value)}
                  disabled={busyCheckout}
                  required
                />
              </label>
            </div>
            <button
              type="submit"
              className="filters-form__btn"
              disabled={busyCheckout || busyItemId != null || busyClear}
            >
              {busyCheckout ? 'Оформление…' : 'Оформить заказ'}
            </button>
          </form>
        </>
      )}

      {errorMessage && status === 'success' && (
        <div className="state state--error" role="alert">
          <p>{errorMessage}</p>
        </div>
      )}
    </section>
  );
}
