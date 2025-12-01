package com.team7.service.client;

import com.team7.model.client.Cart;
import com.team7.model.client.CartItem;

import java.util.*;

public class CartServiceImpl implements CartService {
    private static final Map<Long, Cart> USER_CARTS = new HashMap<>();
    private static Long cartIdCounter = 1L;
    private static Long cartItemIdCounter = 1L;

    @Override
    public Cart getCart(Long userId) {
        return USER_CARTS.getOrDefault(userId, createNewCart(userId));
    }

    @Override
    public Cart addItem(Long userId, Long restaurantId, Long menuItemId,
                        Integer quantity) {
        Cart cart = USER_CARTS.getOrDefault(userId, createNewCart(userId));

        CartItem newItem = new CartItem();
        newItem.setId(cartItemIdCounter++);
        newItem.setMenuItemId(menuItemId);
        newItem.setRestaurantId(restaurantId);
        newItem.setQuantity(quantity);

        cart.getItems().add(newItem);
        updateTotalAmount(cart);
        USER_CARTS.put(userId, cart);

        return cart;
    }

    @Override
    public Cart updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        Cart cart = USER_CARTS.get(userId);
        if (cart == null) throw new IllegalArgumentException("Корзина не найдена");

        cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    updateTotalAmount(cart);
                });

        return cart;
    }

    @Override
    public Cart removeItem(Long userId, Long itemId) {
        Cart cart = USER_CARTS.get(userId);
        if (cart == null) throw new IllegalArgumentException("Корзина не найдена");

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        updateTotalAmount(cart);
        return cart;
    }

    @Override
    public Cart clearCart(Long userId) {
        Cart cart = createNewCart(userId);
        USER_CARTS.put(userId, cart);
        return cart;
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setId(cartIdCounter++);
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotalAmount(0.0);
        return cart;
    }

    private void updateTotalAmount(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> {
                    double basePrice = 100.0;
                    return basePrice * item.getQuantity();
                })
                .sum();
        cart.setTotalAmount(total);
    }
}
