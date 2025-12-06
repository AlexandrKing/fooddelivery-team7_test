package com.team7.service.courieradmin;

import com.team7.service.config.DatabaseConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public static class Order {
        private Long id;
        private Long userId;
        private Long restaurantId;
        private String status;
        private BigDecimal totalAmount;
        private String deliveryAddress;
        private String deliveryType;
        private String paymentMethod;
        private LocalDateTime createdAt;
        private LocalDateTime preferredDeliveryTime;
        private String clientName;
        private String restaurantName;

        public Order() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
        public String getDeliveryType() { return deliveryType; }
        public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getPreferredDeliveryTime() { return preferredDeliveryTime; }
        public void setPreferredDeliveryTime(LocalDateTime preferredDeliveryTime) { this.preferredDeliveryTime = preferredDeliveryTime; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getRestaurantName() { return restaurantName; }
        public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as client_name, r.name as restaurant_name " +
                "FROM orders o " +
                "LEFT JOIN users u ON o.user_id = u.id " +
                "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения заказов: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> getAvailableOrders() {
        List<Order> orders = new ArrayList<>();
        // Упрощенный запрос: только заказы со статусом PENDING
        String sql = "SELECT o.*, u.full_name as client_name, r.name as restaurant_name " +
                "FROM orders o " +
                "LEFT JOIN users u ON o.user_id = u.id " +
                "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                "WHERE o.status = 'PENDING' " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                orders.add(mapResultSetToOrder(rs));
            }

            System.out.println("DEBUG OrderService: Найдено " + count + " заказов со статусом PENDING");

        } catch (SQLException e) {
            System.err.println("Ошибка получения доступных заказов: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public Order getOrderById(Long orderId) {
        String sql = "SELECT o.*, u.full_name as client_name, r.name as restaurant_name " +
                "FROM orders o " +
                "LEFT JOIN users u ON o.user_id = u.id " +
                "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                "WHERE o.id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToOrder(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean cancelOrder(Long orderId) {
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка отмены заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean takeOrder(Long orderId, Long courierId) {
        Order order = getOrderById(orderId);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            return false;
        }

        // Проверяем, не назначен ли уже заказ другому курьеру
        if (isOrderAssigned(orderId)) {
            System.out.println("Заказ уже назначен другому курьеру");
            return false;
        }

        // Используем CourierOrderService для назначения
        CourierOrderService courierOrderService = new CourierOrderService();
        return courierOrderService.assignOrderToCourier(orderId, courierId);
    }

    public boolean completeOrder(Long orderId) {
        String sql = "UPDATE orders SET status = 'DELIVERED' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка завершения заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public BigDecimal getOrderProfit(Long orderId) {
        Order order = getOrderById(orderId);
        if (order != null && order.getTotalAmount() != null) {
            return order.getTotalAmount().multiply(new BigDecimal("0.20"));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getOrderAmount(Long orderId) {
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

    private boolean isOrderAssigned(Long orderId) {
        String sql = "SELECT COUNT(*) FROM courier_assigned_orders WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка проверки назначения заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public Long getCourierIdForOrder(Long orderId) {
        String sql = "SELECT courier_id FROM courier_assigned_orders WHERE order_id = ? LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("courier_id");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения ID курьера для заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setUserId(rs.getLong("user_id"));
        order.setRestaurantId(rs.getLong("restaurant_id"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setDeliveryType(rs.getString("delivery_type"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp preferredTime = rs.getTimestamp("preferred_delivery_time");
        if (preferredTime != null) {
            order.setPreferredDeliveryTime(preferredTime.toLocalDateTime());
        }



        order.setClientName(rs.getString("client_name"));
        order.setRestaurantName(rs.getString("restaurant_name"));
        return order;
    }

    public List<Order> getOrdersByCourierId(Long courierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as client_name, r.name as restaurant_name " +
                "FROM orders o " +
                "LEFT JOIN users u ON o.user_id = u.id " +
                "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                "WHERE EXISTS ( " +
                "    SELECT 1 FROM courier_assigned_orders cao " +
                "    WHERE cao.order_id = o.id AND cao.courier_id = ?" +
                ") " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, courierId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения заказов курьера: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    // Метод для отладки - показывает все заказы
    public void debugAllOrders() {
        String sql = "SELECT o.id, o.status, o.total_amount, u.full_name as client, " +
                "r.name as restaurant, " +
                "(SELECT COUNT(*) FROM courier_assigned_orders cao WHERE cao.order_id = o.id) as assignment_count " +
                "FROM orders o " +
                "LEFT JOIN users u ON o.user_id = u.id " +
                "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                "ORDER BY o.id";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== DEBUG: ВСЕ ЗАКАЗЫ ===");
            System.out.printf("%-5s %-15s %-10s %-20s %-25s %-10s%n",
                    "ID", "Статус", "Сумма", "Клиент", "Ресторан", "Назначений");
            System.out.println("━".repeat(90));

            int count = 0;
            int pendingCount = 0;
            while (rs.next()) {
                count++;
                String status = rs.getString("status");
                if ("PENDING".equals(status)) {
                    pendingCount++;
                }

                System.out.printf("%-5d %-15s %-10.2f %-20s %-25s %-10d%n",
                        rs.getLong("id"),
                        status,
                        rs.getBigDecimal("total_amount").doubleValue(),
                        rs.getString("client"),
                        rs.getString("restaurant"),
                        rs.getInt("assignment_count"));
            }

            System.out.println("━".repeat(90));
            System.out.println("Всего заказов: " + count + ", из них PENDING: " + pendingCount);

        } catch (SQLException e) {
            System.err.println("Ошибка отладки заказов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для создания тестового заказа
    public boolean createTestOrder() {
        String sql = "INSERT INTO orders (user_id, restaurant_id, status, delivery_address, delivery_type, payment_method, total_amount) " +
                "VALUES (1, 1, 'PENDING', 'ул. Тестовая, 1', 'DELIVERY', 'CARD', 500.00)";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            int result = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long newId = generatedKeys.getLong(1);
                        System.out.println("✅ Создан тестовый заказ #" + newId);

                        // Добавляем позиции в заказ
                        String itemsSql = "INSERT INTO order_items (order_id, dish_id, name, price, quantity) VALUES " +
                                "(" + newId + ", 1, 'Маргарита', 450.00, 1)";
                        stmt.executeUpdate(itemsSql);

                        return true;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка создания тестового заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}