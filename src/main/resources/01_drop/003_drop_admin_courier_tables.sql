-- Удаление таблиц админа и курьера в правильном порядке
DROP TABLE IF EXISTS courier_assigned_orders CASCADE;
DROP TABLE IF EXISTS courier_users CASCADE;
DROP TABLE IF EXISTS admin_users CASCADE;