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

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

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
    mockMvc.perform(get("/api/admin/orders"))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/api/admin/orders").with(user("u@test").roles("USER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/admin/orders").with(user("c@test").roles("COURIER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/admin/orders").with(user("r@test").roles("RESTAURANT")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminOrdersListSupportsEmptyAndNonEmpty() throws Exception {
    given(adminService.getOrders()).willReturn(List.of());
    mockMvc.perform(get("/api/admin/orders").with(user("admin@test").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));

    OrderEntity order = new OrderEntity();
    order.setId(901L);
    order.setUserId(1L);
    order.setRestaurantId(4L);
    order.setStatus("DELIVERED");
    order.setTotalAmount(1500.0);
    order.setCreatedAt(LocalDateTime.now());
    given(adminService.getOrders()).willReturn(List.of(order));
    mockMvc.perform(get("/api/admin/orders").with(user("admin@test").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(901));
  }

  @Test
  void adminAccountEndpointsCoverListToggleAndNotFound() throws Exception {
    AppAccountEntity acc = account(11L, "u@test.local", AppRole.USER, true);
    given(adminService.getAccounts()).willReturn(List.of(acc));
    mockMvc.perform(get("/api/admin/accounts").with(user("admin@test").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(11));

    AppAccountEntity toggled = account(11L, "u@test.local", AppRole.USER, false);
    given(adminService.setAccountActive(11L, false)).willReturn(toggled);
    mockMvc.perform(patch("/api/admin/accounts/11/active")
            .with(user("admin@test").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"active\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.active").value(false));

    given(adminService.setAccountActive(404L, true)).willThrow(new IllegalArgumentException("Account not found"));
    mockMvc.perform(patch("/api/admin/accounts/404/active")
            .with(user("admin@test").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"active\":true}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Account not found"));
  }

  private static AppAccountEntity account(Long id, String email, AppRole role, boolean active) {
    AppAccountEntity acc = new AppAccountEntity();
    acc.setId(id);
    acc.setEmail(email);
    acc.setRole(role);
    acc.setIsActive(active);
    acc.setPasswordHash("hash");
    acc.setCreatedAt(LocalDateTime.now());
    acc.setUpdatedAt(LocalDateTime.now());
    return acc;
  }
}
