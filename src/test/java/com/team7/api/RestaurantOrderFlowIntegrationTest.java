package com.team7.api;

import com.team7.persistence.AdminUserJpaRepository;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.AppRole;
import com.team7.persistence.entity.OrderEntity;
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
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestaurantOrderFlowIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean private AuthService authService;
  @MockitoBean private RestaurantService restaurantService;
  @MockitoBean private CartService cartService;
  @MockitoBean private OrderService orderService;
  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private AppAccountJpaRepository appAccountJpaRepository;
  @MockitoBean private UserJpaRepository userJpaRepository;
  @MockitoBean private AdminUserJpaRepository adminUserJpaRepository;
  @MockitoBean private CourierUserJpaRepository courierUserJpaRepository;
  @MockitoBean private AdminService adminService;
  @MockitoBean private CourierService courierService;
  @MockitoBean private RestaurantManagementService restaurantManagementService;
  @MockitoBean private UserSecurityRepository userSecurityRepository;
  @MockitoBean private CourierReviewService courierReviewService;

  @Test
  void unauthorizedAndWrongRoleAccessAreBlocked() throws Exception {
    mockMvc.perform(get("/api/restaurant/orders"))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/api/restaurant/orders").with(user("u@test").roles("USER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/restaurant/orders").with(user("c@test").roles("COURIER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/restaurant/orders").with(user("a@test").roles("ADMIN")))
        .andExpect(status().isForbidden());
  }

  @Test
  void orderListSupportsEmptyAndNonEmpty() throws Exception {
    mockRestaurantPrincipal("restaurant@test.local", 4L);
    given(restaurantManagementService.getRestaurantOrders(4L)).willReturn(List.of());
    mockMvc.perform(get("/api/restaurant/orders").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));

    OrderEntity order = new OrderEntity();
    order.setId(301L);
    order.setUserId(1L);
    order.setRestaurantId(4L);
    order.setStatus("PREPARING");
    order.setTotalAmount(700.0);
    order.setCreatedAt(LocalDateTime.now());
    given(restaurantManagementService.getRestaurantOrders(4L)).willReturn(List.of(order));
    mockMvc.perform(get("/api/restaurant/orders").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(301));
  }

  @Test
  void updateStatusCoversHappyPathAndErrors() throws Exception {
    mockRestaurantPrincipal("restaurant@test.local", 4L);
    OrderEntity ready = new OrderEntity();
    ready.setId(302L);
    ready.setUserId(1L);
    ready.setRestaurantId(4L);
    ready.setStatus("READY");
    ready.setTotalAmount(800.0);
    ready.setCreatedAt(LocalDateTime.now());
    given(restaurantManagementService.updateRestaurantOrderStatus(4L, 302L, "READY")).willReturn(ready);

    mockMvc.perform(patch("/api/restaurant/orders/302/status")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"READY\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("READY"));

    mockMvc.perform(patch("/api/restaurant/orders/302/status")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Status is required"));

    given(restaurantManagementService.updateRestaurantOrderStatus(4L, 999L, "READY"))
        .willThrow(new IllegalArgumentException("Order not found"));
    mockMvc.perform(patch("/api/restaurant/orders/999/status")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"READY\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Order not found"));

    given(restaurantManagementService.updateRestaurantOrderStatus(4L, 333L, "READY"))
        .willThrow(new IllegalArgumentException("Order does not belong to restaurant"));
    mockMvc.perform(patch("/api/restaurant/orders/333/status")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"READY\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Order does not belong to restaurant"));

    given(restaurantManagementService.updateRestaurantOrderStatus(4L, 302L, "DELIVERED"))
        .willThrow(new IllegalArgumentException("Invalid status value"));
    mockMvc.perform(patch("/api/restaurant/orders/302/status")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"DELIVERED\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid status value"));

    given(restaurantManagementService.updateRestaurantOrderStatus(4L, 302L, "PREPARING"))
        .willThrow(new IllegalArgumentException("Invalid status transition"));
    mockMvc.perform(patch("/api/restaurant/orders/302/status")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"PREPARING\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid status transition"));
  }

  @Test
  void missingLinkedRestaurantProfileReturnsDomainError() throws Exception {
    AppAccountEntity account = new AppAccountEntity();
    account.setEmail("restaurant@test.local");
    account.setRole(AppRole.RESTAURANT);
    account.setLinkedRestaurantId(null);
    given(appAccountJpaRepository.findByEmail("restaurant@test.local")).willReturn(Optional.of(account));

    mockMvc.perform(get("/api/restaurant/orders").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Restaurant profile is not linked"));
  }

  private void mockRestaurantPrincipal(String email, Long restaurantId) {
    AppAccountEntity account = new AppAccountEntity();
    account.setEmail(email);
    account.setRole(AppRole.RESTAURANT);
    account.setLinkedRestaurantId(restaurantId);
    given(appAccountJpaRepository.findByEmail(email)).willReturn(Optional.of(account));
  }
}
