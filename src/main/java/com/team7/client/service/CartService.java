package com.team7.client.service;

import com.team7.client.model.Cart;
import com.team7.client.model.SelectedOption;

import java.util.List;

public interface CartService {
    Cart getCart(String userId);
    Cart removeItem(String userId, String itemId);
    Cart addItem(String userId, String restaurantId, String menuItemId, Integer quantity, List<SelectedOption> options);
    Cart updateItemQuantity(String userId, String itemId, Integer quantity);
    Cart clearCart(String userId);
}
