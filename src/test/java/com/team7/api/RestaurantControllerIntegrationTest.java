package com.team7.api;

import com.team7.model.client.Menu;
import com.team7.model.client.Restaurant;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestaurantControllerIntegrationTest {

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
  void unauthenticatedRequestToRestaurantsIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/restaurants"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getRestaurantsWithoutFiltersDelegatesToGetRestaurantsAndMapsFields() throws Exception {
    Restaurant r = restaurant(10L);
    given(restaurantService.getRestaurants()).willReturn(List.of(r));

    mockMvc.perform(get("/api/restaurants").with(user("user@test.local").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(10))
        .andExpect(jsonPath("$.data[0].name").value("R-10"))
        .andExpect(jsonPath("$.data[0].address").value("Addr-10"))
        .andExpect(jsonPath("$.data[0].latitude").value(55.7))
        .andExpect(jsonPath("$.data[0].longitude").value(37.6))
        .andExpect(jsonPath("$.data[0].cuisineType").value("Any"))
        .andExpect(jsonPath("$.data[0].rating").value(4.5))
        .andExpect(jsonPath("$.data[0].deliveryTime").value(40))
        .andExpect(jsonPath("$.data[0].minOrderAmount").value(500.0))
        .andExpect(jsonPath("$.data[0].isActive").value(true));
  }

  @Test
  void getRestaurantsWithAnyFilterDelegatesToFilterRestaurants() throws Exception {
    Restaurant r = restaurant(11L);
    given(restaurantService.filterRestaurants(anyDouble(), anyInt())).willReturn(List.of(r));

    mockMvc.perform(get("/api/restaurants?rating=4.0&deliveryTime=60")
            .with(user("user@test.local").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(11));
  }

  @Test
  void getRestaurantByIdReturnsMappedRestaurant() throws Exception {
    Restaurant r = restaurant(12L);
    given(restaurantService.getRestaurantById(12L)).willReturn(r);

    mockMvc.perform(get("/api/restaurants/12").with(user("user@test.local").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(12))
        .andExpect(jsonPath("$.data.name").value("R-12"));
  }

  @Test
  void getMenuReturnsMappedMenuItems() throws Exception {
    Menu m = new Menu();
    m.setId(5L);
    m.setRestaurantId(10L);
    m.setName("Burger");
    m.setDescription("Tasty");
    m.setPrice(300.0);
    m.setAvailable(true);
    m.setCategory("Fast");
    m.setCalories(700);
    m.setWeight(250.0);
    m.setImageUrl("img");
    m.setCookingTime(15);
    given(restaurantService.getMenu(eq(10L))).willReturn(List.of(m));

    mockMvc.perform(get("/api/restaurants/10/menu").with(user("user@test.local").roles("USER"))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(5))
        .andExpect(jsonPath("$.data[0].name").value("Burger"))
        .andExpect(jsonPath("$.data[0].restaurantId").value(10))
        .andExpect(jsonPath("$.data[0].price").value(300.0))
        .andExpect(jsonPath("$.data[0].available").value(true));
  }

  @Test
  void restaurantControllerReturnsDomainErrorWhenServiceThrowsIllegalArgument() throws Exception {
    given(restaurantService.getRestaurantById(999L)).willThrow(new IllegalArgumentException("Ресторан не найден"));

    mockMvc.perform(get("/api/restaurants/999").with(user("user@test.local").roles("USER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Ресторан не найден"));
  }

  private static Restaurant restaurant(Long id) {
    Restaurant r = new Restaurant();
    r.setId(id);
    r.setName("R-" + id);
    r.setAddress("Addr-" + id);
    r.setLatitude(55.7);
    r.setLongitude(37.6);
    r.setCuisineType("Any");
    r.setRating(4.5);
    r.setDeliveryTime(40);
    r.setMinOrderAmount(500.0);
    r.setIsActive(true);
    return r;
  }
}

