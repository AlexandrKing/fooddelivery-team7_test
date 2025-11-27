package com.team7.client.service;

import com.team7.client.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryServiceImpl implements HistoryService {
    private final DatabaseService dbService = new DatabaseService();

    @Override
    public List<Order> getOrderHistory(Long userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении истории заказов: " + e.getMessage(), e);
        }
        return orders;
    }

    @Override
    public Order getOrderById(Long orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createOrderFromResultSet(rs);
            } else {
                throw new IllegalArgumentException("Заказ не найден");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении заказа: " + e.getMessage(), e);
        }
    }

    @Override
    public Order repeatOrder(Long orderId) {
        Connection conn = null;
        try {
            conn = dbService.connect();
            conn.setAutoCommit(false);

            // Получаем оригинальный заказ
            Order originalOrder = getOrderById(orderId);

            // Создаем новый заказ
            String insertOrderSql = "INSERT INTO orders (user_id, restaurant_id, status, delivery_address, " +
                    "delivery_type, payment_method, preferred_delivery_time, total_amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            Long newOrderId;
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, originalOrder.getUserId());
                pstmt.setLong(2, originalOrder.getRestaurantId());
                pstmt.setString(3, OrderStatus.PENDING.name());
                pstmt.setString(4, originalOrder.getDeliveryAddress());
                pstmt.setString(5, originalOrder.getDeliveryType().name());
                pstmt.setString(6, originalOrder.getPaymentMethod().name());
                pstmt.setTimestamp(7, Timestamp.valueOf(originalOrder.getPreferredDeliveryTime()));
                pstmt.setDouble(8, originalOrder.getTotalAmount());

                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newOrderId = rs.getLong(1);
                    } else {
                        throw new SQLException("Не удалось создать заказ");
                    }
                }
            }

            conn.commit();
            return getOrderById(newOrderId);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Ошибка при повторении заказа: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }

    private Order createOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        // Lombok автоматически создаст геттеры и сеттеры через аннотации @Data или @Getter @Setter
        order.setId(rs.getLong("id"));
        order.setUserId(rs.getLong("user_id"));
        order.setRestaurantId(rs.getLong("restaurant_id"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setDeliveryType(DeliveryType.valueOf(rs.getString("delivery_type")));
        order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        order.setPreferredDeliveryTime(rs.getTimestamp("preferred_delivery_time").toLocalDateTime());
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return order;
    }
}