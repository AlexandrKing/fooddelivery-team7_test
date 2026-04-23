# Food Delivery — frontend (React + Vite)

Минимальный клиент поверх Spring Boot REST API.

## Запуск

1. Поднимите backend (рекомендуется Maven Wrapper):
   - Windows: `.\mvnw.cmd spring-boot:run`
   - Linux/macOS: `./mvnw spring-boot:run`
   По умолчанию backend слушает порт **8080**.
2. В этой папке:

```bash
npm install
npm run dev
```

Откройте http://localhost:5173 — запросы к `/api/*` проксируются на backend (см. `vite.config.js`).

## Endpoint каталога

- **GET** `/api/restaurants` — список ресторанов (`RestaurantController`).
- Опциональные query: `rating`, `deliveryTime` (фильтр на стороне API).

Ответ обёрнут в `ApiSuccessResponse`: поле `data` — массив ресторанов.

## Меню ресторана

- **GET** `/api/restaurants/{id}` — карточка ресторана (`RestaurantResponse`).
- **GET** `/api/restaurants/{id}/menu` — список блюд (`MenuItemResponse[]`).

В UI: маршрут `/restaurants/:restaurantId/menu` (react-router-dom). Список ресторанов ведёт на меню с `state.restaurantName` для заголовка; при прямом заходе по URL имя подгружается тем же **GET** `/{id}`.

## Авторизация

Сейчас в `SecurityConfig` маршруты `/api/**` (кроме login/register) требуют роль **USER** / **ADMIN** (HTTP Basic). Пока в UI авторизации нет: при **401** страница покажет подсказку. Временная проверка API: Swagger или curl с `-u email:password`.

Другой origin без proxy — настройте CORS на backend.

## Сборка

```bash
npm run build
npm run preview
```
