CREATE INDEX IF NOT EXISTS idx_courier_assignments_courier ON courier_assigned_orders(courier_id);
CREATE INDEX IF NOT EXISTS idx_courier_assignments_order ON courier_assigned_orders(order_id);
CREATE INDEX IF NOT EXISTS idx_courier_assignments_status ON courier_assigned_orders(status);