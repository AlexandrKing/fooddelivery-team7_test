package com.team7.api;

import com.team7.persistence.AdminUserJpaRepository;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.AppRole;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourierControllerIntegrationTest {

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
    mockMvc.perform(get("/api/courier/orders/assigned"))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/api/courier/orders/assigned").with(user("u@test").roles("USER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/courier/orders/assigned").with(user("a@test").roles("ADMIN")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/courier/orders/assigned").with(user("r@test").roles("RESTAURANT")))
        .andExpect(status().isForbidden());
  }

  @Test
  void assignedAndAvailableListsSupportEmptyAndNonEmpty() throws Exception {
    mockCourierPrincipal("courier@test.local", 9L);
    given(courierService.getAssignedOrders(9L)).willReturn(List.of());
    mockMvc.perform(get("/api/courier/orders/assigned").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));

    CourierAssignedOrderEntity assigned = new CourierAssignedOrderEntity();
    assigned.setId(1L);
    assigned.setCourierId(9L);
    assigned.setOrderId(100L);
    assigned.setStatus("ASSIGNED");
    assigned.setAssignedAt(LocalDateTime.now());
    given(courierService.getAssignedOrders(9L)).willReturn(List.of(assigned));
    mockMvc.perform(get("/api/courier/orders/assigned").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].orderId").value(100));

    given(courierService.getAvailableDeliveryOrders()).willReturn(List.of());
    mockMvc.perform(get("/api/courier/orders/available").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));

    OrderEntity available = new OrderEntity();
    available.setId(200L);
    available.setUserId(1L);
    available.setRestaurantId(3L);
    available.setStatus("READY");
    available.setTotalAmount(999.0);
    available.setDeliveryAddress("Lenina 1");
    available.setCreatedAt(LocalDateTime.now());
    given(courierService.getAvailableDeliveryOrders()).willReturn(List.of(available));
    mockMvc.perform(get("/api/courier/orders/available").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(200));
  }

  @Test
  void claimAndStatusUpdateCoverHappyPathAndDomainErrors() throws Exception {
    mockCourierPrincipal("courier@test.local", 9L);
    CourierAssignedOrderEntity claimed = new CourierAssignedOrderEntity();
    claimed.setId(2L);
    claimed.setCourierId(9L);
    claimed.setOrderId(101L);
    claimed.setStatus("ASSIGNED");
    claimed.setAssignedAt(LocalDateTime.now());
    given(courierService.claimOrder(9L, 101L)).willReturn(claimed);

    mockMvc.perform(post("/api/courier/orders/101/claim").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

    CourierAssignedOrderEntity delivered = new CourierAssignedOrderEntity();
    delivered.setId(2L);
    delivered.setCourierId(9L);
    delivered.setOrderId(101L);
    delivered.setStatus("DELIVERED");
    delivered.setDeliveryTime(LocalDateTime.now());
    given(courierService.updateAssignedOrderStatus(9L, 101L, "DELIVERED")).willReturn(delivered);
    mockMvc.perform(patch("/api/courier/orders/101/status")
            .with(user("courier@test.local").roles("COURIER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"DELIVERED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("DELIVERED"));

    mockMvc.perform(patch("/api/courier/orders/101/status")
            .with(user("courier@test.local").roles("COURIER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Status is required"));

    given(courierService.claimOrder(9L, 404L)).willThrow(new IllegalArgumentException("Заказ не найден"));
    mockMvc.perform(post("/api/courier/orders/404/claim").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Заказ не найден"));

    given(courierService.updateAssignedOrderStatus(9L, 202L, "DELIVERED"))
        .willThrow(new IllegalArgumentException("Assigned order not found"));
    mockMvc.perform(patch("/api/courier/orders/202/status")
            .with(user("courier@test.local").roles("COURIER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"DELIVERED\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Assigned order not found"));

    given(courierService.updateAssignedOrderStatus(9L, 101L, "READY"))
        .willThrow(new IllegalArgumentException("Invalid status value"));
    mockMvc.perform(patch("/api/courier/orders/101/status")
            .with(user("courier@test.local").roles("COURIER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"READY\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid status value"));

    given(courierService.updateAssignedOrderStatus(9L, 101L, "DELIVERED"))
        .willThrow(new IllegalArgumentException("Invalid status transition"));
    mockMvc.perform(patch("/api/courier/orders/101/status")
            .with(user("courier@test.local").roles("COURIER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"DELIVERED\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid status transition"));
  }

  @Test
  void missingLinkedCourierProfileReturnsDomainError() throws Exception {
    AppAccountEntity account = new AppAccountEntity();
    account.setEmail("courier@test.local");
    account.setRole(AppRole.COURIER);
    account.setLinkedCourierId(null);
    given(appAccountJpaRepository.findByEmail("courier@test.local")).willReturn(Optional.of(account));

    mockMvc.perform(get("/api/courier/orders/assigned").with(user("courier@test.local").roles("COURIER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Courier profile is not linked"));
  }

  private void mockCourierPrincipal(String email, Long courierId) {
    AppAccountEntity account = new AppAccountEntity();
    account.setEmail(email);
    account.setRole(AppRole.COURIER);
    account.setLinkedCourierId(courierId);
    given(appAccountJpaRepository.findByEmail(email)).willReturn(Optional.of(account));
  }
}
