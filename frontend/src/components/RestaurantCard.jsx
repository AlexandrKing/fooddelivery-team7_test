export default function RestaurantCard({ restaurant }) {
  const {
    id,
    name,
    address,
    cuisineType,
    rating,
    deliveryTime,
    minOrderAmount,
    isActive,
  } = restaurant;

  return (
    <article className="restaurant-card">
      <h3 className="restaurant-card__title">{name || '—'}</h3>
      <p className="restaurant-card__meta">
        <span>#{id}</span>
        {isActive === false && <span className="badge badge--muted">неактивен</span>}
      </p>
      {address && <p className="restaurant-card__line">{address}</p>}
      {(cuisineType || rating != null || deliveryTime != null) && (
        <dl className="restaurant-card__dl">
          {cuisineType != null && cuisineType !== '' && (
            <>
              <dt>Кухня</dt>
              <dd>{cuisineType}</dd>
            </>
          )}
          {rating != null && (
            <>
              <dt>Рейтинг</dt>
              <dd>{rating}</dd>
            </>
          )}
          {deliveryTime != null && (
            <>
              <dt>Доставка</dt>
              <dd>до {deliveryTime} мин</dd>
            </>
          )}
          {minOrderAmount != null && (
            <>
              <dt>Мин. заказ</dt>
              <dd>{minOrderAmount}</dd>
            </>
          )}
        </dl>
      )}
    </article>
  );
}
