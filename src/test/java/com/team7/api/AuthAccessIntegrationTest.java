package com.team7.api;

import com.team7.persistence.AdminUserJpaRepository;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.AppRole;
import com.team7.service.admin.AdminService;
import com.team7.service.client.AuthService;
import com.team7.service.client.CartService;
import com.team7.service.client.CourierReviewService;
import com.team7.service.client.OrderService;
import com.team7.service.client.RestaurantService;
import com.team7.service.courier.CourierService;
import com.team7.service.restaurant.RestaurantManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.team7.repository.client.UserSecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAccessIntegrationTest {

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
  void authMeWithoutAuthenticatedPrincipalReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "ghost@test.local", roles = "USER")
  void authMeWithUnknownAccountReturnsUnifiedError() throws Exception {
    given(appAccountJpaRepository.findByEmail("ghost@test.local")).willReturn(Optional.empty());

    mockMvc.perform(get("/api/auth/me").with(user("ghost@test.local").roles("USER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Учетная запись не найдена"));
  }

  @Test
  @WithMockUser(username = "courier@test.local", roles = "COURIER")
  void courierRoleWithoutLinkedCourierProfileReturnsUnifiedError() throws Exception {
    AppAccountEntity account = account("courier@test.local", AppRole.COURIER, null, null, null, null);
    given(appAccountJpaRepository.findByEmail("courier@test.local")).willReturn(Optional.of(account));

    mockMvc.perform(get("/api/courier/orders/assigned").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Courier profile is not linked"));
  }

  @Test
  @WithMockUser(username = "restaurant@test.local", roles = "RESTAURANT")
  void restaurantRoleWithoutLinkedRestaurantProfileReturnsUnifiedError() throws Exception {
    AppAccountEntity account = account("restaurant@test.local", AppRole.RESTAURANT, null, null, null, null);
    given(appAccountJpaRepository.findByEmail("restaurant@test.local")).willReturn(Optional.of(account));

    mockMvc.perform(get("/api/restaurant/orders").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Restaurant profile is not linked"));
  }

  @Test
  void authLoginValidationErrorReturnsUnifiedValidationError() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("Validation Error"));
  }

  private static AppAccountEntity account(
      String email,
      AppRole role,
      Long linkedUserId,
      Long linkedRestaurantId,
      Long linkedCourierId,
      Long linkedAdminId
  ) {
    AppAccountEntity e = new AppAccountEntity();
    e.setEmail(email);
    e.setPasswordHash("hash");
    e.setRole(role);
    e.setLinkedUserId(linkedUserId);
    e.setLinkedRestaurantId(linkedRestaurantId);
    e.setLinkedCourierId(linkedCourierId);
    e.setLinkedAdminId(linkedAdminId);
    e.setIsActive(Boolean.TRUE);
    return e;
  }
}
