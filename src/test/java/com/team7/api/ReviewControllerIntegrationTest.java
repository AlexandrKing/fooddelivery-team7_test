package com.team7.api;

import com.team7.api.dto.review.CourierReviewDtos;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerIntegrationTest {

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
  void unauthenticatedRequestsToReviewEndpointsAreUnauthorized() throws Exception {
    mockMvc.perform(get("/api/client/courier-reviews/mine"))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/api/admin/courier-reviews"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void roleRestrictionsForReviewEndpoints() throws Exception {
    mockMvc.perform(post("/api/client/courier-reviews")
            .with(user("courier@test").roles("COURIER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"orderId\":1,\"rating\":5}"))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/admin/courier-reviews").with(user("user@test").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void getMineReturnsEmptyAndNonEmptyReviews() throws Exception {
    UserSecurityRepository.SecurityUserRecord rec =
        new UserSecurityRepository.SecurityUserRecord(1L, "user@test", "h", "USER", 1L, null, null, null, true);
    given(userSecurityRepository.findByEmail("user@test")).willReturn(rec);
    given(courierReviewService.listMyCourierReviews(1L)).willReturn(List.of());
    mockMvc.perform(get("/api/client/courier-reviews/mine").with(user("user@test").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));

    CourierReviewDtos.CourierReviewResponse review = new CourierReviewDtos.CourierReviewResponse(
        10L, 100L, 1L, 7L, 5, "Fast", LocalDateTime.now()
    );
    given(courierReviewService.listMyCourierReviews(1L)).willReturn(List.of(review));
    mockMvc.perform(get("/api/client/courier-reviews/mine").with(user("user@test").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(10))
        .andExpect(jsonPath("$.data[0].rating").value(5));
  }

  @Test
  void createReviewCoversHappyPathValidationAndDomainError() throws Exception {
    UserSecurityRepository.SecurityUserRecord rec =
        new UserSecurityRepository.SecurityUserRecord(1L, "user@test", "h", "USER", 1L, null, null, null, true);
    given(userSecurityRepository.findByEmail("user@test")).willReturn(rec);
    CourierReviewDtos.CourierReviewResponse created = new CourierReviewDtos.CourierReviewResponse(
        11L, 101L, 1L, 8L, 4, "ok", LocalDateTime.now()
    );
    given(courierReviewService.createCourierReview(1L, 101L, 4, "ok")).willReturn(created);
    mockMvc.perform(post("/api/client/courier-reviews")
            .with(user("user@test").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"orderId\":101,\"rating\":4,\"comment\":\"ok\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(11));

    mockMvc.perform(post("/api/client/courier-reviews")
            .with(user("user@test").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"orderId\":0,\"rating\":0}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation Error"));

    given(courierReviewService.createCourierReview(eq(1L), eq(999L), eq(5), eq("dup")))
        .willThrow(new IllegalArgumentException("Отзыв по этому заказу уже оставлен"));
    mockMvc.perform(post("/api/client/courier-reviews")
            .with(user("user@test").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"orderId\":999,\"rating\":5,\"comment\":\"dup\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Отзыв по этому заказу уже оставлен"));
  }

  @Test
  void adminListAndDeleteReviewCoverHappyAndNotFound() throws Exception {
    CourierReviewDtos.AdminCourierReviewResponse adminReview = new CourierReviewDtos.AdminCourierReviewResponse(
        21L, 201L, 1L, "user@test", 7L, "Courier", 5, "Great", LocalDateTime.now()
    );
    given(courierReviewService.listAllCourierReviewsForAdmin()).willReturn(List.of(adminReview));

    mockMvc.perform(get("/api/admin/courier-reviews").with(user("admin@test").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(21));

    mockMvc.perform(delete("/api/admin/courier-reviews/21").with(user("admin@test").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.deleted").value(true));

    org.mockito.Mockito.doThrow(new IllegalArgumentException("Отзыв не найден"))
        .when(courierReviewService).deleteReviewByAdmin(404L);
    mockMvc.perform(delete("/api/admin/courier-reviews/404").with(user("admin@test").roles("ADMIN")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Отзыв не найден"));
  }
}
