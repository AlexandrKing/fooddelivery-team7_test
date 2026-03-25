package com.team7.api;

import tools.jackson.databind.ObjectMapper;
import com.team7.model.client.*;
import com.team7.service.client.AuthService;
import com.team7.service.client.CartService;
import com.team7.service.client.OrderService;
import com.team7.service.client.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private RestaurantService restaurantService;

  @MockitoBean
  private CartService cartService;

  @MockitoBean
  private OrderService orderService;

  @Test
  void authRegisterReturnsUnifiedSuccessResponse() throws Exception {
    User user = new User();
    user.setId(1L);
    user.setName("Alex");
    user.setEmail("alex@mail.com");
    user.setPhone("+79991234567");
    user.setAddresses(List.of());

    given(authService.register(anyString(), anyString(), anyString(), anyString(), anyString()))
        .willReturn(user);

    Map<String, Object> req = Map.of(
        "name", "Alex",
        "email", "alex@mail.com",
        "phone", "+79991234567",
        "password", "secret123",
        "confirmPassword", "secret123"
    );

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.email").value("alex@mail.com"));
  }

  @Test
  void authRegisterValidationErrorReturnsUnifiedErrorResponse() throws Exception {
    Map<String, Object> req = Map.of(
        "name", "Alex",
        "email", "",
        "phone", "+79991234567",
        "password", "123",
        "confirmPassword", "123"
    );

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("Validation Error"));
  }

  @Test
  void restaurantsGetListReturnsUnifiedSuccessResponse() throws Exception {
    Restaurant r = new Restaurant();
    r.setId(10L);
    r.setName("Pizza Place");
    r.setAddress("Main st");
    r.setCuisineType("Italian");
    r.setRating(4.7);
    r.setDeliveryTime(35);
    r.setMinOrderAmount(500.0);
    r.setIsActive(true);

    given(restaurantService.getRestaurants()).willReturn(List.of(r));

    mockMvc.perform(get("/api/restaurants"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void restaurantsGetListWithAuthReturnsUnifiedSuccessResponse() throws Exception {
    Restaurant r = new Restaurant();
    r.setId(10L);
    r.setName("Pizza Place");
    r.setAddress("Main st");
    r.setCuisineType("Italian");
    r.setRating(4.7);
    r.setDeliveryTime(35);
    r.setMinOrderAmount(500.0);
    r.setIsActive(true);

    given(restaurantService.getRestaurants()).willReturn(List.of(r));

    mockMvc.perform(get("/api/restaurants").with(user("api-user").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(10))
        .andExpect(jsonPath("$.data[0].name").value("Pizza Place"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void cartAddItemReturnsUnifiedSuccessResponse() throws Exception {
    Cart cart = new Cart();
    cart.setId(5L);
    cart.setUserId(1L);
    cart.setRestaurantId(2L);
    cart.setTotalAmount(750.0);
    cart.setItems(List.of(new CartItem(11L, 101L, 2L, 2, "Burger", 375.0)));

    given(cartService.addItem(eq(1L), eq(2L), eq(101L), eq(2))).willReturn(cart);

    Map<String, Object> req = Map.of(
        "restaurantId", 2,
        "dishId", 101,
        "quantity", 2
    );

    mockMvc.perform(post("/api/carts/1/items")
            .with(user("api-user").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value(1))
        .andExpect(jsonPath("$.data.items[0].name").value("Burger"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void ordersCreateReturnsUnifiedSuccessResponse() throws Exception {
    Order order = new Order();
    order.setId(99L);
    order.setUserId(1L);
    order.setRestaurantId(2L);
    order.setStatus(OrderStatus.PENDING);
    order.setDeliveryAddress("Lenina 1");
    order.setDeliveryType(DeliveryType.DELIVERY);
    order.setPaymentMethod(PaymentMethod.CARD);
    order.setPreferredDeliveryTime(LocalDateTime.now().plusHours(1));
    order.setTotalAmount(1500.0);
    order.setCreatedAt(LocalDateTime.now());
    order.setItems(List.of(new OrderItem(1L, 101L, "Burger", 500.0, 3)));

    given(orderService.createOrder(anyLong(), anyLong(), anyString(), any(), any(), any()))
        .willReturn(order);

    Map<String, Object> req = Map.of(
        "userId", 1,
        "restaurantId", 2,
        "deliveryAddress", "Lenina 1",
        "deliveryType", "DELIVERY",
        "deliveryTime", LocalDateTime.now().plusHours(2).toString(),
        "paymentMethod", "CARD"
    );

    mockMvc.perform(post("/api/orders")
            .with(user("api-user").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(99))
        .andExpect(jsonPath("$.data.status").value("PENDING"));
  }
}

