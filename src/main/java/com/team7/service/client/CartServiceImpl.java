package com.team7.service.client;

import com.team7.model.client.Cart;
import com.team7.model.client.CartItem;

import com.team7.model.client.*;
import java.sql.*;
import java.util.*;

public class CartServiceImpl implements CartService {
    private final DatabaseService dbService = new DatabaseService();

    @Override
    public Cart getCart(Long userId) {
        String cartSql = "SELECT * FROM carts WHERE user_id = ?";
        String itemsSql = "SELECT ci.*, m.name, m.price FROM cart_items ci " +
                "JOIN menu m ON ci.menu_item_id = m.id " +
                "WHERE ci.cart_id = ?";

        try (Connection conn = dbService.connect();
             PreparedStatement cartStmt = conn.prepareStatement(cartSql)) {

            cartStmt.setLong(1, userId);
            ResultSet cartRs = cartStmt.executeQuery();

            if (cartRs.next()) {
                Cart cart = createCartFromResultSet(cartRs);

                try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                    itemsStmt.setLong(1, cart.getId());
                    ResultSet itemsRs = itemsStmt.executeQuery();

                    List<CartItem> items = new ArrayList<>();
                    while (itemsRs.next()) {
                        CartItem item = createCartItemFromResultSet(itemsRs);
                        items.add(item);
                    }
                    cart.setItems(items);
                }

                return cart;
            } else {
                return createNewCart(userId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении корзины: " + e.getMessage(), e);
        }
    }

    @Override
    public Cart addItem(Long userId, Long restaurantId, Long menuItemId, Integer quantity) {
        Connection conn = null;
        try {
            conn = dbService.connect();
            conn.setAutoCommit(false);

            Cart cart = getCart(userId);
            if (cart.getId() == null) {
                cart = createNewCartInDb(userId, restaurantId, conn);
            }

            String checkSql = "SELECT * FROM cart_items WHERE cart_id = ? AND menu_item_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setLong(1, cart.getId());
                checkStmt.setLong(2, menuItemId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    updateItemQuantityInDb(cart.getId(), rs.getLong("id"),
                            rs.getInt("quantity") + quantity, conn);
                } else {
                    addNewItemToCart(cart.getId(), menuItemId, quantity, conn);
                }
            }

            updateCartTotalAmount(cart.getId(), conn);
            conn.commit();

            return getCart(userId);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Ошибка при добавлении товара в корзину: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }

    @Override
    public Cart updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        if (quantity <= 0) {
            return removeItem(userId, itemId);
        }

        Connection conn = null;
        try {
            conn = dbService.connect();
            conn.setAutoCommit(false);

            Cart cart = getCart(userId);
            if (cart.getId() == null) {
                throw new IllegalArgumentException("Корзина не найдена");
            }

            updateItemQuantityInDb(cart.getId(), itemId, quantity, conn);
            updateCartTotalAmount(cart.getId(), conn);
            conn.commit();

            return getCart(userId);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Ошибка при обновлении количества: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }

    @Override
    public Cart removeItem(Long userId, Long itemId) {
        Connection conn = null;
        try {
            conn = dbService.connect();
            conn.setAutoCommit(false);

            Cart cart = getCart(userId);
            if (cart.getId() == null) {
                throw new IllegalArgumentException("Корзина не найдена");
            }

            String sql = "DELETE FROM cart_items WHERE id = ? AND cart_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, itemId);
                pstmt.setLong(2, cart.getId());
                pstmt.executeUpdate();
            }

            updateCartTotalAmount(cart.getId(), conn);
            conn.commit();

            return getCart(userId);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Ошибка при удалении товара: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }

    @Override
    public Cart clearCart(Long userId) {
        Connection conn = null;
        try {
            conn = dbService.connect();
            conn.setAutoCommit(false);

            Cart cart = getCart(userId);
            if (cart.getId() == null) {
                return createNewCart(userId);
            }

            String sql = "DELETE FROM cart_items WHERE cart_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, cart.getId());
                pstmt.executeUpdate();
            }

            String updateSql = "UPDATE carts SET total_amount = 0 WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setLong(1, cart.getId());
                pstmt.executeUpdate();
            }

            conn.commit();
            return getCart(userId);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Ошибка при очистке корзины: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotalAmount(0.0);
        return cart;
    }

    private Cart createNewCartInDb(Long userId, Long restaurantId, Connection conn) throws SQLException {
        String sql = "INSERT INTO carts (user_id, restaurant_id, total_amount) VALUES (?, ?, 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, restaurantId);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Cart cart = new Cart();
                    cart.setId(rs.getLong(1));
                    cart.setUserId(userId);
                    cart.setRestaurantId(restaurantId);
                    cart.setItems(new ArrayList<>());
                    cart.setTotalAmount(0.0);
                    return cart;
                }
            }
        }
        throw new SQLException("Не удалось создать корзину");
    }

    private void addNewItemToCart(Long cartId, Long menuItemId, Integer quantity, Connection conn) throws SQLException {
        String sql = "INSERT INTO cart_items (cart_id, menu_item_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, cartId);
            pstmt.setLong(2, menuItemId);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
        }
    }

    private void updateItemQuantityInDb(Long cartId, Long itemId, Integer quantity, Connection conn) throws SQLException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ? AND cart_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setLong(2, itemId);
            pstmt.setLong(3, cartId);
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new IllegalArgumentException("Элемент корзины не найден");
            }
        }
    }

    private void updateCartTotalAmount(Long cartId, Connection conn) throws SQLException {
        String sql = "UPDATE carts SET total_amount = (" +
                "SELECT COALESCE(SUM(ci.quantity * m.price), 0) " +
                "FROM cart_items ci " +
                "JOIN menu m ON ci.menu_item_id = m.id " +
                "WHERE ci.cart_id = ?" +
                ") WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, cartId);
            pstmt.setLong(2, cartId);
            pstmt.executeUpdate();
        }
    }

    private Cart createCartFromResultSet(ResultSet rs) throws SQLException {
        Cart cart = new Cart();
        cart.setId(rs.getLong("id"));
        cart.setUserId(rs.getLong("user_id"));
        cart.setRestaurantId(rs.getLong("restaurant_id"));
        cart.setTotalAmount(rs.getDouble("total_amount"));
        return cart;
    }

    private CartItem createCartItemFromResultSet(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setMenuItemId(rs.getLong("menu_item_id"));
        item.setRestaurantId(rs.getLong("restaurant_id"));
        item.setQuantity(rs.getInt("quantity"));
        return item;
    }
}