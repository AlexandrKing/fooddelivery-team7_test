-- 016_insert_courier_assigned_orders.sql
-- Вставка тестовых данных для назначенных заказов курьерам

INSERT INTO courier_assigned_orders (order_id, courier_id, assigned_at, status, pickup_time, delivery_time, notes)
VALUES
  -- Курьер 1 (Иван Иванов) назначен на 2 заказа
  (1, 1, '2024-01-10 10:30:00', 'delivered', '2024-01-10 10:35:00', '2024-01-10 11:15:00', 'Доставлено вовремя'),
  (2, 1, '2024-01-10 12:00:00', 'in_progress', '2024-01-10 12:05:00', NULL, 'Клиент попросил позвонить перед доставкой'),

  -- Курьер 2 (Петр Петров) назначен на 3 заказа
  (3, 2, '2024-01-10 11:00:00', 'delivered', '2024-01-10 11:05:00', '2024-01-10 11:45:00', 'Клиент доволен'),
  (4, 2, '2024-01-10 13:30:00', 'picked_up', '2024-01-10 13:35:00', NULL, 'В пути'),
  (5, 2, '2024-01-10 14:00:00', 'assigned', NULL, NULL, 'Ожидает готовности заказа'),

  -- Курьер 3 (Анна Сидорова) назначен на 2 заказа
  (6, 3, '2024-01-10 09:00:00', 'delivered', '2024-01-10 09:05:00', '2024-01-10 09:40:00', 'Ранняя доставка'),
  (7, 3, '2024-01-10 15:00:00', 'assigned', NULL, NULL, 'Вечерний заказ'),

  -- Курьер 4 (Мария Кузнецова) назначен на 1 заказ
  (8, 4, '2024-01-10 16:00:00', 'picked_up', '2024-01-10 16:05:00', NULL, 'Срочный заказ'),

  -- Курьер 5 (Алексей Смирнов) пока без назначений
  -- (NULL, 5, NULL, 'available', NULL, NULL, NULL)

  -- Курьер 6 (Ольга Васильева) назначен на 1 заказ
  (9, 6, '2024-01-10 17:00:00', 'assigned', NULL, NULL, 'Заказ на завтра'),

  -- Курьер 7 (Дмитрий Попов) назначен на 2 заказа
  (10, 7, '2024-01-10 18:00:00', 'delivered', '2024-01-10 18:05:00', '2024-01-10 18:50:00', 'Доставлено с опозданием из-за пробок'),
  (11, 7, '2024-01-10 19:00:00', 'in_progress', '2024-01-10 19:05:00', NULL, 'Последний заказ на сегодня');

-- Обновляем статусы заказов в соответствии с назначением курьеров
UPDATE client_orders SET status = 'delivered' WHERE id IN (1, 3, 6, 10);
UPDATE client_orders SET status = 'in_delivery' WHERE id IN (2, 7);
UPDATE client_orders SET status = 'ready_for_pickup' WHERE id IN (4, 8);
UPDATE client_orders SET status = 'assigned_to_courier' WHERE id IN (5, 9, 11);

-- Обновляем статистику курьеров
UPDATE courier_users
SET completed_orders = CASE id
  WHEN 1 THEN 1
  WHEN 2 THEN 1
  WHEN 3 THEN 1
  WHEN 7 THEN 1
  ELSE completed_orders
END,
status = CASE id
  WHEN 1 THEN 'busy'
  WHEN 2 THEN 'busy'
  WHEN 3 THEN 'available'
  WHEN 4 THEN 'busy'
  WHEN 6 THEN 'available'
  WHEN 7 THEN 'busy'
  ELSE status
END
WHERE id IN (1, 2, 3, 4, 6, 7);

-- Проверяем вставленные данные
SELECT
  ca.id,
  o.id as order_id,
  c.full_name as courier_name,
  ca.status,
  ca.assigned_at,
  ca.pickup_time,
  ca.delivery_time
FROM courier_assigned_orders ca
JOIN client_orders o ON ca.order_id = o.id
JOIN courier_users c ON ca.courier_id = c.id
ORDER BY ca.assigned_at DESC;