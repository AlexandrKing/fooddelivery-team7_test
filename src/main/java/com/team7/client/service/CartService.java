package com.team7.client.service;

import com.team7.client.model.Cart;

public interface CartService {
    Cart getCart(Long userId);
    Cart removeItem(Long userId, Long itemId);
    Cart addItem(Long userId, Long restaurantId, Long menuItemId, Integer quantity);
    Cart updateItemQuantity(Long userId, Long itemId, Integer quantity);
    Cart clearCart(Long userId);
}
