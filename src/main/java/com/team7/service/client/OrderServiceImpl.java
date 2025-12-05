package com.team7.service.client;

import com.team7.model.client.*;
import com.team7.service.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderServiceImpl implements OrderService {
    private final CartService cartService;

    public OrderServiceImpl(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public Order createOrder(Long userId, Long restaurantId, String deliveryAddress,
                             DeliveryType deliveryType, LocalDateTime deliveryTime,
                             PaymentMethod paymentMethod) {

        // Получаем корзину пользователя
        Cart cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Создаем заказ
            String orderSql = "INSERT INTO orders (user_id, restaurant_id, delivery_address, " +
                "delivery_type, delivery_time, payment_method, status, total_amount) " +
                "VALUES (?, ?, ?, ?::delivery_type, ?, ?::payment_method, ?::order_status, ?) " +
                "RETURNING id, created_at";

            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
                orderStmt.setLong(1, userId);
                orderStmt.setLong(2, restaurantId);
                orderStmt.setString(3, deliveryAddress);
                orderStmt.setString(4, deliveryType.toString());
                orderStmt.setTimestamp(5, Timestamp.valueOf(deliveryTime));
                orderStmt.setString(6, paymentMethod.toString());
                orderStmt.setString(7, OrderStatus.PENDING.toString());
                orderStmt.setDouble(8, cart.getTotalAmount());

                ResultSet rs = orderStmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Не удалось создать заказ");
                }

                Long orderId = rs.getLong("id");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                // Добавляем товары в заказ
                String itemSql = "INSERT INTO order_items (order_id, menu_item_id, name, price, quantity) " +
                    "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                    for (CartItem cartItem : cart.getItems()) {
                        itemStmt.setLong(1, orderId);
                        itemStmt.setLong(2, cartItem.getMenuItemId());
                        itemStmt.setString(3, cartItem.getName());
                        itemStmt.setDouble(4, cartItem.getPrice());
                        itemStmt.setInt(5, cartItem.getQuantity());
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }

                // Добавляем запись в историю статусов
                String historySql = "INSERT INTO order_status_history (order_id, status) VALUES (?, ?::order_status)";
                try (PreparedStatement historyStmt = conn.prepareStatement(historySql)) {
                    historyStmt.setLong(1, orderId);
                    historyStmt.setString(2, OrderStatus.PENDING.toString());
                    historyStmt.executeUpdate();
                }

                // Очищаем корзину
                cartService.clearCart(userId);

                conn.commit();

                // Создаем объект заказа для возврата
                Order order = new Order();
                order.setId(orderId);
                order.setUserId(userId);
                order.setRestaurantId(restaurantId);
                order.setDeliveryAddress(deliveryAddress);
                order.setDeliveryType(deliveryType);
                order.setPreferredDeliveryTime(deliveryTime);
                order.setPaymentMethod(paymentMethod);
                order.setStatus(OrderStatus.PENDING);
                order.setCreatedAt(createdAt);
                order.setTotalAmount(cart.getTotalAmount());

                return order;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Ошибка при создании заказа: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }

    @Override
    public Order getOrder(Long orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getLong("id"));
                order.setUserId(rs.getLong("user_id"));
                order.setRestaurantId(rs.getLong("restaurant_id"));
                order.setDeliveryAddress(rs.getString("delivery_address"));
                order.setDeliveryType(DeliveryType.valueOf(rs.getString("delivery_type")));
                order.setPreferredDeliveryTime(rs.getTimestamp("delivery_time").toLocalDateTime());
                order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
                order.setStatus(OrderStatus.valueOf(rs.getString("status")));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                order.setTotalAmount(rs.getDouble("total_amount"));

                // Загружаем товары заказа
                order.setItems(getOrderItems(orderId, conn));

                return order;
            } else {
                throw new IllegalArgumentException("Заказ не найден");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении заказа: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getLong("id"));
                order.setUserId(rs.getLong("user_id"));
                order.setRestaurantId(rs.getLong("restaurant_id"));
                order.setDeliveryAddress(rs.getString("delivery_address"));
                order.setDeliveryType(DeliveryType.valueOf(rs.getString("delivery_type")));
                order.setPreferredDeliveryTime(rs.getTimestamp("delivery_time").toLocalDateTime());
                order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
                order.setStatus(OrderStatus.valueOf(rs.getString("status")));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                order.setTotalAmount(rs.getDouble("total_amount"));
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении заказов: " + e.getMessage(), e);
        }

        return orders;
    }

    @Override
    public Order cancelOrder(Long orderId) {
        String sql = "UPDATE orders SET status = ?::order_status WHERE id = ? AND status = ?::order_status";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, OrderStatus.CANCELLED.toString());
            pstmt.setLong(2, orderId);
            pstmt.setString(3, OrderStatus.PENDING.toString());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Добавляем запись в историю статусов
                String historySql = "INSERT INTO order_status_history (order_id, status) VALUES (?, ?::order_status)";
                try (PreparedStatement historyStmt = conn.prepareStatement(historySql)) {
                    historyStmt.setLong(1, orderId);
                    historyStmt.setString(2, OrderStatus.CANCELLED.toString());
                    historyStmt.executeUpdate();
                }

                return getOrder(orderId);
            } else {
                throw new IllegalArgumentException("Нельзя отменить заказ в текущем статусе");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при отмене заказа: " + e.getMessage(), e);
        }
    }

    @Override
    public Order repeatOrder(Long orderId) {
        // Получаем оригинальный заказ
        Order originalOrder = getOrder(orderId);

        // Создаем новый заказ на основе старого
        return createOrder(
            originalOrder.getUserId(),
            originalOrder.getRestaurantId(),
            originalOrder.getDeliveryAddress(),
            originalOrder.getDeliveryType(),
            LocalDateTime.now().plusHours(1),
            originalOrder.getPaymentMethod()
        );
    }

    private List<OrderItem> getOrderItems(Long orderId, Connection conn) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getLong("id"));
                item.setMenuItemId(rs.getLong("menu_item_id"));
                item.setName(rs.getString("name"));
                item.setPrice(rs.getDouble("price"));
                item.setQuantity(rs.getInt("quantity"));
                items.add(item);
            }
        }
        return items;
    }
}