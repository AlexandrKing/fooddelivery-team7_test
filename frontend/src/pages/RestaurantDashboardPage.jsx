import { useEffect, useMemo, useState } from 'react';
import {
  createRestaurantDish,
  deleteRestaurantDish,
  fetchRestaurantMenu,
  fetchRestaurantOrders,
  updateRestaurantDish,
  updateRestaurantOrderStatus,
} from '../services/restaurantApi.js';

const ORDER_STATUSES = [
  'PENDING',
  'PREPARING',
  'READY',
  'DELIVERING',
  'DELIVERED',
  'CANCELLED',
];

function emptyDishForm() {
  return {
    name: '',
    description: '',
    price: '',
    available: true,
    category: '',
    calories: '',
    imageUrl: '',
    preparationTimeMin: '',
  };
}

function toPayload(form) {
  const price = Number(form.price);
  const calories = Number(form.calories);
  const prep = Number(form.preparationTimeMin);
  return {
    name: form.name.trim(),
    description: form.description.trim() || null,
    price: Number.isFinite(price) ? price : null,
    available: Boolean(form.available),
    category: form.category.trim() || null,
    calories: Number.isFinite(calories) ? calories : null,
    imageUrl: form.imageUrl.trim() || null,
    preparationTimeMin: Number.isFinite(prep) ? Math.trunc(prep) : null,
  };
}

function asCurrency(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  return `${n.toLocaleString('ru-RU')} ₽`;
}

function asDate(value) {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return String(value);
  return d.toLocaleString('ru-RU');
}

export default function RestaurantDashboardPage() {
  const [ordersStatus, setOrdersStatus] = useState('loading');
  const [menuStatus, setMenuStatus] = useState('loading');
  const [orders, setOrders] = useState([]);
  const [menu, setMenu] = useState([]);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [formError, setFormError] = useState('');
  const [updatingOrderId, setUpdatingOrderId] = useState(null);
  const [creatingDish, setCreatingDish] = useState(false);
  const [updatingDishId, setUpdatingDishId] = useState(null);
  const [deletingDishId, setDeletingDishId] = useState(null);
  const [editingDishId, setEditingDishId] = useState(null);
  const [newDishForm, setNewDishForm] = useState(emptyDishForm());
  const [editDishForm, setEditDishForm] = useState(emptyDishForm());

  const menuSorted = useMemo(
    () => [...menu].sort((a, b) => Number(a.id) - Number(b.id)),
    [menu]
  );

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setOrdersStatus('loading');
      setMenuStatus('loading');
      setError('');
      setSuccessMessage('');
      try {
        const [ordersData, menuData] = await Promise.all([
          fetchRestaurantOrders(),
          fetchRestaurantMenu(),
        ]);
        if (cancelled) return;
        setOrders(ordersData);
        setMenu(menuData);
        setOrdersStatus('success');
        setMenuStatus('success');
      } catch (e) {
        if (cancelled) return;
        setOrdersStatus('error');
        setMenuStatus('error');
        setError(e?.message || 'Не удалось загрузить кабинет ресторана');
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  async function handleOrderStatusChange(orderId, status) {
    setUpdatingOrderId(orderId);
    setError('');
    setSuccessMessage('');
    try {
      const updated = await updateRestaurantOrderStatus(orderId, status);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      setSuccessMessage(`Статус заказа #${orderId} обновлен`);
    } catch (e) {
      setError(e?.message || 'Не удалось обновить статус заказа');
    } finally {
      setUpdatingOrderId(null);
    }
  }

  function validateDishForm(form) {
    const name = form.name.trim();
    const price = Number(form.price);
    const calories = form.calories === '' ? null : Number(form.calories);
    const prep = form.preparationTimeMin === '' ? null : Number(form.preparationTimeMin);

    if (!name) return 'Укажите название блюда.';
    if (!Number.isFinite(price)) return 'Укажите корректную цену блюда.';
    if (price < 0) return 'Цена не может быть отрицательной.';
    if (calories != null && (!Number.isFinite(calories) || calories < 0)) {
      return 'Калории должны быть неотрицательным числом.';
    }
    if (prep != null && (!Number.isFinite(prep) || prep < 0)) {
      return 'Время приготовления должно быть неотрицательным числом.';
    }
    return null;
  }

  async function handleCreateDish(e) {
    e.preventDefault();
    const validationMessage = validateDishForm(newDishForm);
    if (validationMessage) {
      setFormError(validationMessage);
      return;
    }
    setCreatingDish(true);
    setError('');
    setFormError('');
    setSuccessMessage('');
    try {
      const created = await createRestaurantDish(toPayload(newDishForm));
      setMenu((prev) => [created, ...prev]);
      setNewDishForm(emptyDishForm());
      setSuccessMessage(`Блюдо "${created.name || created.id}" добавлено`);
    } catch (e2) {
      setError(e2?.message || 'Не удалось добавить блюдо. Проверьте введенные значения.');
    } finally {
      setCreatingDish(false);
    }
  }

  function startEditDish(dish) {
    setEditingDishId(dish.id);
    setEditDishForm({
      name: dish.name ?? '',
      description: dish.description ?? '',
      price: dish.price ?? '',
      available: dish.available !== false,
      category: dish.category ?? '',
      calories: dish.calories ?? '',
      imageUrl: dish.imageUrl ?? '',
      preparationTimeMin: dish.preparationTimeMin ?? '',
    });
  }

  async function handleSaveDishEdit(dishId) {
    const validationMessage = validateDishForm(editDishForm);
    if (validationMessage) {
      setFormError(validationMessage);
      return;
    }
    setUpdatingDishId(dishId);
    setError('');
    setFormError('');
    setSuccessMessage('');
    try {
      const updated = await updateRestaurantDish(dishId, toPayload(editDishForm));
      setMenu((prev) => prev.map((d) => (d.id === dishId ? updated : d)));
      setEditingDishId(null);
      setSuccessMessage(`Блюдо "${updated.name || updated.id}" обновлено`);
    } catch (e) {
      setError(e?.message || 'Не удалось обновить блюдо. Проверьте введенные значения.');
    } finally {
      setUpdatingDishId(null);
    }
  }

  async function handleDeleteDish(dishId) {
    setDeletingDishId(dishId);
    setError('');
    setSuccessMessage('');
    try {
      await deleteRestaurantDish(dishId);
      setMenu((prev) => prev.filter((d) => d.id !== dishId));
      if (editingDishId === dishId) {
        setEditingDishId(null);
      }
      setSuccessMessage(`Блюдо #${dishId} удалено`);
    } catch (e) {
      setError(e?.message || 'Не удалось удалить блюдо');
    } finally {
      setDeletingDishId(null);
    }
  }

  return (
    <section className="page dashboard-page">
      <h2>Кабинет ресторана</h2>

      {error && (
        <div className="state state--error" role="alert">
          <p>{error}</p>
        </div>
      )}
      {successMessage && (
        <div className="state state--success" role="status">
          <p>{successMessage}</p>
        </div>
      )}
      {formError && (
        <div className="state state--error" role="alert">
          <p>{formError}</p>
        </div>
      )}

      <section className="dashboard-block">
        <h3>Заказы ресторана</h3>
        {ordersStatus === 'loading' && (
          <p className="state state--loading" aria-busy="true">
            Загрузка заказов…
          </p>
        )}
        {ordersStatus === 'error' && (
          <p className="state state--error">Не удалось загрузить заказы.</p>
        )}
        {ordersStatus === 'success' && orders.length === 0 && (
          <p className="state state--empty">У ресторана пока нет заказов.</p>
        )}
        {ordersStatus === 'success' && orders.length > 0 && (
          <ul className="order-list">
            {orders.map((order) => (
              <li key={order.id} className="order-card">
                <div className="order-card__head">
                  <h3>Заказ #{order.id}</h3>
                  <span className="badge">{order.status || '—'}</span>
                </div>
                <p className="order-card__line">
                  Клиент: {order.userId} · Сумма: <strong>{asCurrency(order.totalAmount)}</strong>
                </p>
                <p className="order-card__line">Создан: {asDate(order.createdAt)}</p>
                <div className="order-card__actions">
                  <select
                    className="restaurant-status-select"
                    value={order.status || 'PENDING'}
                    onChange={(e) => handleOrderStatusChange(order.id, e.target.value)}
                    disabled={updatingOrderId === order.id}
                  >
                    {ORDER_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="dashboard-block">
        <h3>Меню ресторана</h3>

        <form className="dashboard-form" onSubmit={handleCreateDish}>
          <div className="dashboard-form__grid">
            <label className="checkout-form__field">
              Название
              <input
                value={newDishForm.name}
                onChange={(e) => setNewDishForm((prev) => ({ ...prev, name: e.target.value }))}
                required
                disabled={creatingDish}
              />
            </label>
            <label className="checkout-form__field">
              Цена
              <input
                type="number"
                min="0"
                step="0.01"
                value={newDishForm.price}
                onChange={(e) => setNewDishForm((prev) => ({ ...prev, price: e.target.value }))}
                required
                disabled={creatingDish}
              />
            </label>
            <label className="checkout-form__field">
              Категория
              <input
                value={newDishForm.category}
                onChange={(e) => setNewDishForm((prev) => ({ ...prev, category: e.target.value }))}
                disabled={creatingDish}
              />
            </label>
            <label className="checkout-form__field">
              Время приготовления (мин)
              <input
                type="number"
                min="0"
                value={newDishForm.preparationTimeMin}
                onChange={(e) =>
                  setNewDishForm((prev) => ({ ...prev, preparationTimeMin: e.target.value }))
                }
                disabled={creatingDish}
              />
            </label>
          </div>
          <label className="checkout-form__field">
            Описание
            <input
              value={newDishForm.description}
              onChange={(e) => setNewDishForm((prev) => ({ ...prev, description: e.target.value }))}
              disabled={creatingDish}
            />
          </label>
          <label className="checkout-form__field">
            URL изображения
            <input
              value={newDishForm.imageUrl}
              onChange={(e) => setNewDishForm((prev) => ({ ...prev, imageUrl: e.target.value }))}
              disabled={creatingDish}
            />
          </label>
          <label className="checkout-form__field dashboard-form__toggle">
            <input
              type="checkbox"
              checked={newDishForm.available}
              onChange={(e) => setNewDishForm((prev) => ({ ...prev, available: e.target.checked }))}
              disabled={creatingDish}
            />
            Доступно для заказа
          </label>
          <button className="filters-form__btn" type="submit" disabled={creatingDish}>
            {creatingDish ? 'Добавление…' : 'Добавить блюдо'}
          </button>
        </form>

        {menuStatus === 'loading' && (
          <p className="state state--loading" aria-busy="true">
            Загрузка меню…
          </p>
        )}
        {menuStatus === 'error' && (
          <p className="state state--error">Не удалось загрузить меню.</p>
        )}
        {menuStatus === 'success' && menuSorted.length === 0 && (
          <p className="state state--empty">В меню пока нет блюд.</p>
        )}
        {menuStatus === 'success' && menuSorted.length > 0 && (
          <ul className="menu-item-list">
            {menuSorted.map((dish) => {
              const isEditing = editingDishId === dish.id;
              const isUpdating = updatingDishId === dish.id;
              const isDeleting = deletingDishId === dish.id;
              return (
                <li key={dish.id} className="menu-item-card">
                  {!isEditing ? (
                    <>
                      <div className="menu-item-card__head">
                        <h4 className="menu-item-card__title">{dish.name || `Блюдо #${dish.id}`}</h4>
                        <span className="menu-item-card__price">{asCurrency(dish.price)}</span>
                      </div>
                      <p className="menu-item-card__meta">
                        #{dish.id} · {dish.category || 'Без категории'} ·{' '}
                        {dish.available === false ? 'Недоступно' : 'Доступно'}
                      </p>
                      {dish.description && <p className="menu-item-card__desc">{dish.description}</p>}
                      <div className="order-card__actions">
                        <button
                          type="button"
                          className="filters-form__btn filters-form__btn--secondary"
                          onClick={() => startEditDish(dish)}
                          disabled={isDeleting}
                        >
                          Редактировать
                        </button>
                        <button
                          type="button"
                          className="filters-form__btn filters-form__btn--secondary"
                          onClick={() => handleDeleteDish(dish.id)}
                          disabled={isDeleting}
                        >
                          {isDeleting ? 'Удаление…' : 'Удалить'}
                        </button>
                      </div>
                    </>
                  ) : (
                    <div className="dashboard-form">
                      <div className="dashboard-form__grid">
                        <label className="checkout-form__field">
                          Название
                          <input
                            value={editDishForm.name}
                            onChange={(e) => setEditDishForm((prev) => ({ ...prev, name: e.target.value }))}
                            disabled={isUpdating}
                          />
                        </label>
                        <label className="checkout-form__field">
                          Цена
                          <input
                            type="number"
                            min="0"
                            step="0.01"
                            value={editDishForm.price}
                            onChange={(e) => setEditDishForm((prev) => ({ ...prev, price: e.target.value }))}
                            disabled={isUpdating}
                          />
                        </label>
                        <label className="checkout-form__field">
                          Категория
                          <input
                            value={editDishForm.category}
                            onChange={(e) => setEditDishForm((prev) => ({ ...prev, category: e.target.value }))}
                            disabled={isUpdating}
                          />
                        </label>
                        <label className="checkout-form__field">
                          Время приготовления (мин)
                          <input
                            type="number"
                            min="0"
                            value={editDishForm.preparationTimeMin}
                            onChange={(e) =>
                              setEditDishForm((prev) => ({
                                ...prev,
                                preparationTimeMin: e.target.value,
                              }))
                            }
                            disabled={isUpdating}
                          />
                        </label>
                      </div>
                      <label className="checkout-form__field">
                        Описание
                        <input
                          value={editDishForm.description}
                          onChange={(e) =>
                            setEditDishForm((prev) => ({ ...prev, description: e.target.value }))
                          }
                          disabled={isUpdating}
                        />
                      </label>
                      <label className="checkout-form__field">
                        URL изображения
                        <input
                          value={editDishForm.imageUrl}
                          onChange={(e) =>
                            setEditDishForm((prev) => ({ ...prev, imageUrl: e.target.value }))
                          }
                          disabled={isUpdating}
                        />
                      </label>
                      <label className="checkout-form__field dashboard-form__toggle">
                        <input
                          type="checkbox"
                          checked={editDishForm.available}
                          onChange={(e) =>
                            setEditDishForm((prev) => ({ ...prev, available: e.target.checked }))
                          }
                          disabled={isUpdating}
                        />
                        Доступно для заказа
                      </label>
                      <div className="order-card__actions">
                        <button
                          type="button"
                          className="filters-form__btn"
                          onClick={() => handleSaveDishEdit(dish.id)}
                          disabled={isUpdating}
                        >
                          {isUpdating ? 'Сохранение…' : 'Сохранить'}
                        </button>
                        <button
                          type="button"
                          className="filters-form__btn filters-form__btn--secondary"
                          onClick={() => setEditingDishId(null)}
                          disabled={isUpdating}
                        >
                          Отмена
                        </button>
                      </div>
                    </div>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </section>
    </section>
  );
}

