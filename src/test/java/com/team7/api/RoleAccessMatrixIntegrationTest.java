package com.team7.api;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleAccessMatrixIntegrationTest {

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
  void unauthenticatedAccessToProtectedEndpointIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/admin/accounts"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void userCannotAccessAdminCourierOrRestaurantEndpoints() throws Exception {
    mockMvc.perform(get("/api/admin/accounts").with(user("user@test.local").roles("USER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/courier/orders/assigned").with(user("user@test.local").roles("USER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/restaurant/orders").with(user("user@test.local").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void courierCannotAccessClientAdminOrRestaurantEndpoints() throws Exception {
    mockMvc.perform(get("/api/client/courier-reviews/mine").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/admin/accounts").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/restaurant/orders").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void restaurantCannotAccessClientAdminOrCourierEndpoints() throws Exception {
    mockMvc.perform(get("/api/client/courier-reviews/mine").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/admin/accounts").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/courier/orders/assigned").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminHasAccessOnlyToAdminEndpoints() throws Exception {
    given(adminService.getAccounts()).willReturn(List.of());

    mockMvc.perform(get("/api/admin/accounts").with(user("admin@test.local").roles("ADMIN")))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/client/courier-reviews/mine").with(user("admin@test.local").roles("ADMIN")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/courier/orders/assigned").with(user("admin@test.local").roles("ADMIN")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/restaurant/orders").with(user("admin@test.local").roles("ADMIN")))
        .andExpect(status().isForbidden());
  }
}
