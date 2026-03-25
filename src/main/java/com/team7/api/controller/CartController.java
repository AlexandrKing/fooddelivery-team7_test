package com.team7.api.controller;

import com.team7.api.dto.cart.CartDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.model.client.Cart;
import com.team7.model.client.CartItem;
import jakarta.validation.Valid;
import com.team7.service.client.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("/{userId}")
  public ApiSuccessResponse<CartDtos.CartResponse> getCart(@PathVariable Long userId) {
    return ApiSuccessResponse.of(toCartResponse(cartService.getCart(userId)));
  }

  @PostMapping("/{userId}/items")
  public ApiSuccessResponse<CartDtos.CartResponse> addItem(
      @PathVariable Long userId,
      @Valid @RequestBody CartDtos.AddItemRequest request
  ) {
    Cart cart = cartService.addItem(userId, request.restaurantId(), request.dishId(), request.quantity());
    return ApiSuccessResponse.of(toCartResponse(cart));
  }

  @PatchMapping("/{userId}/items/{itemId}")
  public ApiSuccessResponse<CartDtos.CartResponse> updateItemQuantity(
      @PathVariable Long userId,
      @PathVariable Long itemId,
      @Valid @RequestBody CartDtos.UpdateItemQuantityRequest request
  ) {
    Cart cart = cartService.updateItemQuantity(userId, itemId, request.quantity());
    return ApiSuccessResponse.of(toCartResponse(cart));
  }

  @DeleteMapping("/{userId}/items/{itemId}")
  public ApiSuccessResponse<CartDtos.CartResponse> removeItem(
      @PathVariable Long userId,
      @PathVariable Long itemId
  ) {
    return ApiSuccessResponse.of(toCartResponse(cartService.removeItem(userId, itemId)));
  }

  @DeleteMapping("/{userId}/items")
  public ApiSuccessResponse<CartDtos.CartResponse> clearCart(@PathVariable Long userId) {
    return ApiSuccessResponse.of(toCartResponse(cartService.clearCart(userId)));
  }

  private CartDtos.CartResponse toCartResponse(Cart cart) {
    List<CartDtos.CartItemResponse> items = cart.getItems() == null
        ? List.of()
        : cart.getItems().stream().map(this::toCartItemResponse).toList();

    return new CartDtos.CartResponse(
        cart.getId(),
        cart.getUserId(),
        items,
        cart.getTotalAmount(),
        cart.getRestaurantId()
    );
  }

  private CartDtos.CartItemResponse toCartItemResponse(CartItem item) {
    return new CartDtos.CartItemResponse(
        item.getId(),
        item.getMenuItemId(),
        item.getRestaurantId(),
        item.getQuantity(),
        item.getName(),
        item.getPrice()
    );
  }
}

