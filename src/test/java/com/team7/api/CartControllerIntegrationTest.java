package com.team7.api;

import com.team7.model.client.Cart;
import com.team7.model.client.CartItem;
import com.team7.persistence.AdminUserJpaRepository;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.repository.client.UserSecurityRepository;
import com.team7.service.admin.AdminService;
import com.team7.service.client.AuthService;
import com.team7.service.client.CartService;
import com.team7.service.client.CourierReviewService;
import com.team7.service.client.OrderService;
import com.team7.service.client.RestaurantService;
import com.team7.service.courier.CourierService;
import com.team7.service.restaurant.RestaurantManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;
  @MockitoBean
  private RestaurantService restaurantService;
  @MockitoBean
  private CartService cartService;
  @MockitoBean
  private OrderService orderService;
  @MockitoBean
  private AuthenticationManager authenticationManager;
  @MockitoBean
  private AppAccountJpaRepository appAccountJpaRepository;
  @MockitoBean
  private UserJpaRepository userJpaRepository;
  @MockitoBean
  private AdminUserJpaRepository adminUserJpaRepository;
  @MockitoBean
  private CourierUserJpaRepository courierUserJpaRepository;
  @MockitoBean
  private AdminService adminService;
  @MockitoBean
  private CourierService courierService;
  @MockitoBean
  private RestaurantManagementService restaurantManagementService;
  @MockitoBean
  private UserSecurityRepository userSecurityRepository;
  @MockitoBean
  private CourierReviewService courierReviewService;

  @Test
  void unauthenticatedRequestToCartEndpointIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/carts/1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getCartReturnsCurrentUserCart() throws Exception {
    Cart cart = cartWithItems(21L, 1L, List.of(new CartItem(101L, 301L, 2L, 2, "Burger", 350.0)), 700.0);
    given(cartService.getCart(1L)).willReturn(cart);

    mockMvc.perform(get("/api/carts/1").with(user("user@test.local").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value(1))
        .andExpect(jsonPath("$.data.items[0].menuItemId").value(301));
  }

  @Test
  void getCartReturnsEmptyCart() throws Exception {
    Cart cart = cartWithItems(null, 1L, List.of(), 0.0);
    given(cartService.getCart(1L)).willReturn(cart);

    mockMvc.perform(get("/api/carts/1").with(user("user@test.local").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(0))
        .andExpect(jsonPath("$.data.totalAmount").value(0.0));
  }

  @Test
  void addItemReturnsUpdatedCart() throws Exception {
    Cart cart = cartWithItems(21L, 1L, List.of(new CartItem(101L, 301L, 2L, 1, "Burger", 350.0)), 350.0);
    given(cartService.addItem(1L, 2L, 301L, 1)).willReturn(cart);

    mockMvc.perform(post("/api/carts/1/items")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"restaurantId\":2,\"dishId\":301,\"quantity\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[0].quantity").value(1))
        .andExpect(jsonPath("$.data.totalAmount").value(350.0));
  }

  @Test
  void addSameItemAgainReturnsCartWithIncreasedQuantity() throws Exception {
    Cart cart = cartWithItems(21L, 1L, List.of(new CartItem(101L, 301L, 2L, 3, "Burger", 350.0)), 1050.0);
    given(cartService.addItem(1L, 2L, 301L, 2)).willReturn(cart);

    mockMvc.perform(post("/api/carts/1/items")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"restaurantId\":2,\"dishId\":301,\"quantity\":2}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[0].quantity").value(3));
  }

  @Test
  void updateQuantityReturnsUpdatedCart() throws Exception {
    Cart cart = cartWithItems(21L, 1L, List.of(new CartItem(101L, 301L, 2L, 4, "Burger", 350.0)), 1400.0);
    given(cartService.updateItemQuantity(1L, 101L, 4)).willReturn(cart);

    mockMvc.perform(patch("/api/carts/1/items/101")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"quantity\":4}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[0].quantity").value(4));
  }

  @Test
  void removeItemReturnsUpdatedCart() throws Exception {
    Cart cart = cartWithItems(21L, 1L, List.of(), 0.0);
    given(cartService.removeItem(1L, 101L)).willReturn(cart);

    mockMvc.perform(delete("/api/carts/1/items/101").with(user("user@test.local").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(0));
  }

  @Test
  void clearCartReturnsEmptyCart() throws Exception {
    Cart cart = cartWithItems(21L, 1L, List.of(), 0.0);
    given(cartService.clearCart(1L)).willReturn(cart);

    mockMvc.perform(delete("/api/carts/1/items").with(user("user@test.local").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalAmount").value(0.0));
  }

  @Test
  void addItemReturnsValidationErrorForInvalidQuantity() throws Exception {
    mockMvc.perform(post("/api/carts/1/items")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"restaurantId\":2,\"dishId\":301,\"quantity\":0}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation Error"));
  }

  @Test
  void addItemReturnsDomainErrorForMissingDish() throws Exception {
    given(cartService.addItem(1L, 2L, 999L, 1)).willThrow(new IllegalArgumentException("Блюдо не найдено: id=999"));

    mockMvc.perform(post("/api/carts/1/items")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"restaurantId\":2,\"dishId\":999,\"quantity\":1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Блюдо не найдено: id=999"));
  }

  @Test
  void updateQuantityReturnsDomainErrorForMissingCartItem() throws Exception {
    given(cartService.updateItemQuantity(1L, 404L, 2)).willThrow(new IllegalArgumentException("Элемент корзины не найден"));

    mockMvc.perform(patch("/api/carts/1/items/404")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"quantity\":2}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Элемент корзины не найден"));
  }

  @Test
  void updateQuantityWithNullPayloadFieldReturnsValidationError() throws Exception {
    mockMvc.perform(patch("/api/carts/1/items/101")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"quantity\":null}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation Error"));
  }

  private static Cart cartWithItems(Long cartId, Long userId, List<CartItem> items, Double totalAmount) {
    Cart cart = new Cart();
    cart.setId(cartId);
    cart.setUserId(userId);
    cart.setItems(items);
    cart.setTotalAmount(totalAmount);
    cart.setRestaurantId(2L);
    return cart;
  }
}
