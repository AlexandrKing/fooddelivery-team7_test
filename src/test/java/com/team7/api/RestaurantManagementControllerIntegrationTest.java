package com.team7.api;

import com.team7.persistence.AdminUserJpaRepository;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.AppRole;
import com.team7.persistence.entity.DishEntity;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestaurantManagementControllerIntegrationTest {

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
  void unauthenticatedAccessToRestaurantEndpointIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/restaurant/menu"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void foreignRolesCannotAccessRestaurantOnlyEndpoints() throws Exception {
    mockMvc.perform(get("/api/restaurant/menu").with(user("u@test").roles("USER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/restaurant/menu").with(user("c@test").roles("COURIER")))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/restaurant/menu").with(user("a@test").roles("ADMIN")))
        .andExpect(status().isForbidden());
  }

  @Test
  void getMenuReturnsEmptyAndNonEmptyLists() throws Exception {
    mockRestaurantPrincipal("restaurant@test.local", 3L);
    given(restaurantManagementService.getMenu(3L)).willReturn(List.of());
    mockMvc.perform(get("/api/restaurant/menu").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));

    DishEntity dish = dish(101L, 3L, "Pizza", 699.0);
    given(restaurantManagementService.getMenu(3L)).willReturn(List.of(dish));
    mockMvc.perform(get("/api/restaurant/menu").with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(101))
        .andExpect(jsonPath("$.data[0].name").value("Pizza"));
  }

  @Test
  void createUpdateDeleteDishCoverHappyPathAndErrors() throws Exception {
    mockRestaurantPrincipal("restaurant@test.local", 3L);
    DishEntity created = dish(201L, 3L, "Pasta", 540.0);
    given(restaurantManagementService.createDish(eq(3L), any(DishEntity.class))).willReturn(created);
    mockMvc.perform(post("/api/restaurant/menu")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Pasta\",\"description\":\"Cream\",\"price\":540.0,\"available\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(201));

    DishEntity updated = dish(201L, 3L, "Pasta 2", 560.0);
    given(restaurantManagementService.updateDish(eq(3L), eq(201L), any(DishEntity.class))).willReturn(updated);
    mockMvc.perform(put("/api/restaurant/menu/201")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Pasta 2\",\"price\":560.0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("Pasta 2"));

    mockMvc.perform(delete("/api/restaurant/menu/201")
            .with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    given(restaurantManagementService.updateDish(eq(3L), eq(999L), any(DishEntity.class)))
        .willThrow(new IllegalArgumentException("Dish not found"));
    mockMvc.perform(put("/api/restaurant/menu/999")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"ghost\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Dish not found"));

    willThrow(new IllegalArgumentException("Dish does not belong to restaurant"))
        .given(restaurantManagementService)
        .deleteDish(3L, 777L);
    mockMvc.perform(delete("/api/restaurant/menu/777")
            .with(user("restaurant@test.local").roles("RESTAURANT")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Dish does not belong to restaurant"));
  }

  @Test
  void updateOrderStatusValidationErrorWhenStatusMissing() throws Exception {
    mockRestaurantPrincipal("restaurant@test.local", 3L);
    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/restaurant/orders/10/status")
            .with(user("restaurant@test.local").roles("RESTAURANT"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Status is required"));
  }

  @Test
  void restaurantRoleWithoutLinkedProfileGetsDomainError() throws Exception {
    AppAccountEntity account = new AppAccountEntity();
    account.setEmail("restaurant@test.local");
    account.setRole(AppRole.RESTAURANT);
    account.setLinkedRestaurantId(null);
    given(appAccountJpaRepository.findByEmail("restaurant@test.local")).willReturn(Optional.of(account));

    mockMvc.perform(get("/api/restaurant/menu").with(user("restaurant@test.local").roles("RESTAURANT")))
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

  private static DishEntity dish(Long id, Long restaurantId, String name, Double price) {
    DishEntity d = new DishEntity();
    d.setId(id);
    d.setRestaurantId(restaurantId);
    d.setName(name);
    d.setPrice(price);
    d.setIsAvailable(Boolean.TRUE);
    return d;
  }
}
