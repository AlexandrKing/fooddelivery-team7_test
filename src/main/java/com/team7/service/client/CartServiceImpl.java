package com.team7.service.client;

import com.team7.model.client.Cart;
import com.team7.repository.client.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;

    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart getCart(Long userId) {
        return cartRepository.getCart(userId);
    }

    @Override
    public Cart addItem(Long userId, Long restaurantId, Long dishId, Integer quantity) {
        return cartRepository.addItem(userId, restaurantId, dishId, quantity);
    }

    @Override
    public Cart updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        if (quantity <= 0) {
            return removeItem(userId, itemId);
        }
        return cartRepository.updateItemQuantity(userId, itemId, quantity);
    }

    @Override
    public Cart removeItem(Long userId, Long itemId) {
        return cartRepository.removeItem(userId, itemId);
    }

    @Override
    public Cart clearCart(Long userId) {
        return cartRepository.clearCart(userId);
    }
}