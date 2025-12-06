package com.team7.service.courieradmin;

import com.team7.model.courier.AssignedOrder;
import com.team7.service.config.DatabaseConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourierOrderService {

    public boolean assignOrderToCourier(Long orderId, Long courierId) {
        // Сначала обновляем статус заказа в таблице orders
        String updateOrderSql = "UPDATE orders SET status = 'ACCEPTED' WHERE id = ? AND status = 'PENDING'";

        // Затем добавляем запись в courier_assigned_orders
        String insertAssignmentSql = "INSERT INTO courier_assigned_orders (courier_id, order_id, status, assigned_at) VALUES (?, ?, 'assigned', CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Начинаем транзакцию
            conn.setAutoCommit(false);

            try {
                // 1. Обновляем статус заказа
                try (PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderSql)) {
                    updateOrderStmt.setLong(1, orderId);
                    int rowsUpdated = updateOrderStmt.executeUpdate();

                    if (rowsUpdated == 0) {
                        // Заказ не найден или уже не в статусе PENDING
                        System.out.println("Заказ #" + orderId + " не найден или уже принят");
                        conn.rollback();
                        return false;
                    }
                }

                // 2. Назначаем заказ курьеру
                try (PreparedStatement insertStmt = conn.prepareStatement(insertAssignmentSql)) {
                    insertStmt.setLong(1, courierId);
                    insertStmt.setLong(2, orderId);
                    int affectedRows = insertStmt.executeUpdate();

                    if (affectedRows > 0) {
                        // 3. Обновляем статус курьера
                        CourierService courierService = new CourierService();
                        courierService.updateCourierStatus(courierId, "busy");

                        // Фиксируем транзакцию
                        conn.commit();
                        System.out.println("✅ Заказ #" + orderId + " назначен курьеру #" + courierId);
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Ошибка при назначении заказа: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean markOrderAsPickedUp(Long orderId) {
        String sql = "UPDATE courier_assigned_orders SET status = 'picked_up', picked_up_at = CURRENT_TIMESTAMP WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            boolean result = stmt.executeUpdate() > 0;

            if (result) {
                System.out.println("✅ Заказ #" + orderId + " отмечен как забран");
            }

            return result;

        } catch (SQLException e) {
            System.err.println("Ошибка отметки заказа как полученного: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean markOrderAsDelivered(Long orderId, String deliveryNotes) {
        String sql = "UPDATE courier_assigned_orders SET status = 'delivered', actual_delivery_time = CURRENT_TIMESTAMP, delivery_notes = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Отмечаем как доставленный в courier_assigned_orders
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, deliveryNotes);
                    stmt.setLong(2, orderId);
                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                // 2. Обновляем статус заказа в orders
                String updateOrderSql = "UPDATE orders SET status = 'DELIVERED', actual_delivery_time = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateOrderSql)) {
                    updateStmt.setLong(1, orderId);
                    updateStmt.executeUpdate();
                }

                // 3. Получаем ID курьера и начисляем комиссию
                Long courierId = getCourierIdByOrderId(orderId);
                if (courierId != null) {
                    CourierService courierService = new CourierService();

                    // Обновляем статус курьера
                    courierService.updateCourierStatus(courierId, "available");

                    // Увеличиваем счетчик выполненных заказов
                    courierService.incrementCompletedOrders(courierId);

                    // Начисляем комиссию
                    BigDecimal orderAmount = getOrderAmount(orderId);
                    if (orderAmount != null) {
                        BigDecimal commission = orderAmount.multiply(new BigDecimal("0.5"));
                        courierService.addMoneyToCourier(courierId, commission);
                        System.out.println("💰 Начислено " + commission + " руб. курьеру #" + courierId);
                    }
                }

                // Фиксируем транзакцию
                conn.commit();
                System.out.println("✅ Заказ #" + orderId + " отмечен как доставленный");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Ошибка при отметке доставки: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private Long getCourierIdByOrderId(Long orderId) {
        String sql = "SELECT courier_id FROM courier_assigned_orders WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("courier_id");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения ID курьера: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private BigDecimal getOrderAmount(Long orderId) {
        String sql = "SELECT total_amount FROM orders WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total_amount");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения суммы заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<AssignedOrder> getOrdersByCourierId(Long courierId) {
        List<AssignedOrder> orders = new ArrayList<>();
        String sql = "SELECT cao.*, o.total_amount, o.delivery_address, r.name as restaurant_name " +
                "FROM courier_assigned_orders cao " +
                "JOIN orders o ON cao.order_id = o.id " +
                "JOIN restaurants r ON o.restaurant_id = r.id " +
                "WHERE cao.courier_id = ? " +
                "ORDER BY cao.assigned_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, courierId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AssignedOrder order = new AssignedOrder();
                order.setId(rs.getLong("id"));
                order.setCourierId(rs.getLong("courier_id"));
                order.setOrderId(rs.getLong("order_id"));
                order.setAssignedAt(rs.getTimestamp("assigned_at").toLocalDateTime());

                Timestamp pickedUpAt = rs.getTimestamp("picked_up_at");
                if (pickedUpAt != null) {
                    order.setPickedUpAt(pickedUpAt.toLocalDateTime());
                }

                Timestamp deliveredAt = rs.getTimestamp("delivery_time");
                if (deliveredAt != null) {
                    order.setDeliveredAt(deliveredAt.toLocalDateTime());
                }

                order.setStatus(rs.getString("status"));
                order.setDeliveryNotes(rs.getString("delivery_notes"));
                order.setOrderAmount(rs.getBigDecimal("total_amount"));
                order.setDeliveryAddress(rs.getString("delivery_address"));
                order.setRestaurantName(rs.getString("restaurant_name"));

                orders.add(order);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения заказов курьера: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    // Новый метод для получения статуса назначения заказа
    public String getOrderAssignmentStatus(Long orderId) {
        String sql = "SELECT status FROM courier_assigned_orders WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения статуса назначения: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Метод для проверки, назначен ли заказ
    public boolean isOrderAssigned(Long orderId) {
        return getOrderAssignmentStatus(orderId) != null;
    }
}