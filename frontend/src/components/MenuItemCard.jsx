export default function MenuItemCard({ item }) {
  const {
    id,
    name,
    description,
    price,
    available,
    category,
    calories,
    cookingTime,
  } = item;

  return (
    <article className="menu-item-card">
      <div className="menu-item-card__head">
        <h3 className="menu-item-card__title">{name || '—'}</h3>
        {price != null && (
          <span className="menu-item-card__price">{price} ₽</span>
        )}
      </div>
      <p className="menu-item-card__meta">#{id}</p>
      {available === false && (
        <p className="menu-item-card__unavailable">Сейчас недоступно</p>
      )}
      {description && (
        <p className="menu-item-card__desc">{description}</p>
      )}
      {(category || calories != null || cookingTime != null) && (
        <dl className="menu-item-card__dl">
          {category != null && category !== '' && (
            <>
              <dt>Категория</dt>
              <dd>{category}</dd>
            </>
          )}
          {calories != null && (
            <>
              <dt>Ккал</dt>
              <dd>{calories}</dd>
            </>
          )}
          {cookingTime != null && (
            <>
              <dt>Время</dt>
              <dd>{cookingTime} мин</dd>
            </>
          )}
        </dl>
      )}
    </article>
  );
}
