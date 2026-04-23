package com.team7.api;

import com.team7.model.client.DeliveryType;
import com.team7.model.client.Order;
import com.team7.model.client.OrderStatus;
import com.team7.model.client.PaymentMethod;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

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
  void unauthenticatedRequestToOrdersEndpointIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/orders/1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void userRoleCanGetOrdersListForUser() throws Exception {
    mockUserPrincipal("user@test.local", 1L);
    Order order = order(11L, 1L, 2L, OrderStatus.PENDING);
    given(orderService.getUserOrders(1L)).willReturn(List.of(order));

    mockMvc.perform(get("/api/orders/user/1").with(user("user@test.local").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(11))
        .andExpect(jsonPath("$.data[0].userId").value(1));
  }

  @Test
  void courierRoleAlsoCanAccessGenericOrdersEndpoint() throws Exception {
    Order order = order(12L, 1L, 3L, OrderStatus.PENDING);
    given(orderService.getOrder(12L)).willReturn(order);

    mockMvc.perform(get("/api/orders/12").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(12));
  }

  @Test
  void getOrderReturnsDomainErrorWhenOrderNotFound() throws Exception {
    given(orderService.getOrder(99L)).willThrow(new IllegalArgumentException("Заказ не найден"));

    mockMvc.perform(get("/api/orders/99").with(user("user@test.local").roles("USER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Заказ не найден"));
  }

  @Test
  void getOrderReturnsDomainErrorWhenAccessToForeignOrderIsDenied() throws Exception {
    mockUserPrincipal("user@test.local", 1L);
    Order foreign = order(77L, 2L, 2L, OrderStatus.PENDING);
    given(orderService.getOrder(77L)).willReturn(foreign);

    mockMvc.perform(get("/api/orders/77").with(user("user@test.local").roles("USER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Доступ к заказу запрещён"));
  }

  @Test
  void createOrderFromCartReturnsCreatedOrder() throws Exception {
    mockUserPrincipal("user@test.local", 1L);
    LocalDateTime deliveryTime = LocalDateTime.now().plusHours(2);
    Order created = order(15L, 1L, 2L, OrderStatus.PENDING);
    created.setPreferredDeliveryTime(deliveryTime);
    given(orderService.createOrder(
        eq(1L), eq(2L), eq("Lenina 10"), eq(DeliveryType.DELIVERY), any(LocalDateTime.class), eq(PaymentMethod.CARD)
    )).willReturn(created);

    mockMvc.perform(post("/api/orders")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":1,\"restaurantId\":2,\"deliveryAddress\":\"Lenina 10\",\"deliveryType\":\"DELIVERY\",\"deliveryTime\":\""
                + deliveryTime + "\",\"paymentMethod\":\"CARD\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(15))
        .andExpect(jsonPath("$.data.status").value("PENDING"));
  }

  @Test
  void createOrderReturnsValidationErrorForInvalidPayload() throws Exception {
    mockUserPrincipal("user@test.local", 1L);
    mockMvc.perform(post("/api/orders")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":0,\"restaurantId\":0,\"deliveryAddress\":\"\",\"deliveryType\":null,\"deliveryTime\":null,\"paymentMethod\":null}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("Validation Error"));
  }

  @Test
  void createOrderReturnsDomainErrorWhenCartIsEmpty() throws Exception {
    mockUserPrincipal("user@test.local", 1L);
    LocalDateTime deliveryTime = LocalDateTime.now().plusHours(2);
    given(orderService.createOrder(
        eq(1L), eq(2L), eq("Lenina 10"), eq(DeliveryType.DELIVERY), any(LocalDateTime.class), eq(PaymentMethod.CARD)
    )).willThrow(new IllegalArgumentException("Корзина пуста"));

    mockMvc.perform(post("/api/orders")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":1,\"restaurantId\":2,\"deliveryAddress\":\"Lenina 10\",\"deliveryType\":\"DELIVERY\",\"deliveryTime\":\""
                + deliveryTime + "\",\"paymentMethod\":\"CARD\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Корзина пуста"));
  }

  @Test
  void cancelOrderReturnsConflictLikeDomainErrorWhenStatusTransitionForbidden() throws Exception {
    mockUserPrincipal("user@test.local", 1L);
    given(orderService.getOrder(55L)).willReturn(order(55L, 1L, 2L, OrderStatus.PENDING));
    given(orderService.cancelOrder(55L)).willThrow(new IllegalArgumentException("Нельзя отменить заказ в текущем статусе"));

    mockMvc.perform(post("/api/orders/55/cancel").with(user("user@test.local").roles("USER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Нельзя отменить заказ в текущем статусе"));
  }

  @Test
  void repeatOrderReturnsDomainErrorWhenOriginalOrderMissing() throws Exception {
    mockUserPrincipal("user@test.local", 1L);
    given(orderService.getOrder(404L)).willThrow(new IllegalArgumentException("Заказ не найден"));

    mockMvc.perform(post("/api/orders/404/repeat").with(user("user@test.local").roles("USER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Заказ не найден"));
  }

  @Test
  void userCannotRequestOrdersOfAnotherUser() throws Exception {
    mockUserPrincipal("user@test.local", 1L);

    mockMvc.perform(get("/api/orders/user/2").with(user("user@test.local").roles("USER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Доступ к заказу запрещён"));
  }

  @Test
  void userCannotCreateOrderForAnotherUserId() throws Exception {
    mockUserPrincipal("user@test.local", 1L);
    LocalDateTime deliveryTime = LocalDateTime.now().plusHours(2);

    mockMvc.perform(post("/api/orders")
            .with(user("user@test.local").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":2,\"restaurantId\":2,\"deliveryAddress\":\"Lenina 10\",\"deliveryType\":\"DELIVERY\",\"deliveryTime\":\""
                + deliveryTime + "\",\"paymentMethod\":\"CARD\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Доступ к заказу запрещён"));
  }

  private static Order order(Long id, Long userId, Long restaurantId, OrderStatus status) {
    Order order = new Order();
    order.setId(id);
    order.setUserId(userId);
    order.setRestaurantId(restaurantId);
    order.setStatus(status);
    order.setDeliveryAddress("Address");
    order.setDeliveryType(DeliveryType.DELIVERY);
    order.setPaymentMethod(PaymentMethod.CARD);
    order.setPreferredDeliveryTime(LocalDateTime.now().plusHours(1));
    order.setTotalAmount(1000.0);
    order.setCreatedAt(LocalDateTime.now());
    order.setItems(List.of());
    return order;
  }

  private void mockUserPrincipal(String email, Long linkedUserId) {
    UserSecurityRepository.SecurityUserRecord rec =
        new UserSecurityRepository.SecurityUserRecord(
            1L,
            email,
            "hash",
            "USER",
            linkedUserId,
            null,
            null,
            null,
            true
        );
    given(userSecurityRepository.findByEmail(email)).willReturn(rec);
  }
}
