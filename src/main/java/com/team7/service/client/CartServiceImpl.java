package com.team7.service.client;

import com.team7.model.client.Cart;
import com.team7.repository.client.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;

    // TODO(legacy-cleanup): remove this fallback constructor in Wave 2.
    @Deprecated(forRemoval = false, since = "1.1")
    public CartServiceImpl() {
        this.cartRepository = new CartRepository();
    }

    @Autowired
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