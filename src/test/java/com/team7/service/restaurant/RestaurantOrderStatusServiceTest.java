package com.team7.service.restaurant;

import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.OrderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RestaurantOrderStatusServiceTest {

  @Mock
  private OrderJpaRepository orderJpaRepository;
  @Mock
  private DishJpaRepository dishJpaRepository;

  private RestaurantManagementService service;

  @BeforeEach
  void setUp() {
    service = new RestaurantManagementService(orderJpaRepository, dishJpaRepository);
  }

  @Test
  void getRestaurantOrdersReturnsList() {
    OrderEntity o = new OrderEntity();
    o.setId(1L);
    given(orderJpaRepository.findByRestaurantIdOrderByCreatedAtDesc(4L)).willReturn(List.of(o));
    assertEquals(1, service.getRestaurantOrders(4L).size());
  }

  @Test
  void updateStatusAllowsOnlyConfiguredRestaurantTransitions() {
    OrderEntity order = new OrderEntity();
    order.setId(10L);
    order.setRestaurantId(4L);
    order.setStatus("PENDING");
    given(orderJpaRepository.findById(10L)).willReturn(Optional.of(order));
    given(orderJpaRepository.save(any(OrderEntity.class))).willAnswer(a -> a.getArgument(0));

    OrderEntity updated = service.updateRestaurantOrderStatus(4L, 10L, "PREPARING");
    assertEquals("PREPARING", updated.getStatus());

    IllegalArgumentException invalidTransitionToDelivered = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateRestaurantOrderStatus(4L, 10L, "DELIVERED")
    );
    assertTrue(invalidTransitionToDelivered.getMessage().contains("Недопустимый переход статуса заказа"));
    assertTrue(invalidTransitionToDelivered.getMessage().contains("PREPARING -> DELIVERED"));

    order.setStatus("READY");

    IllegalArgumentException invalidTransition = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateRestaurantOrderStatus(4L, 10L, "PREPARING")
    );
    assertTrue(invalidTransition.getMessage().contains("Недопустимый переход статуса заказа"));
    assertTrue(invalidTransition.getMessage().contains("READY -> PREPARING"));
  }

  @Test
  void updateStatusThrowsOnNotFoundOrForeignRestaurant() {
    given(orderJpaRepository.findById(404L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.updateRestaurantOrderStatus(4L, 404L, "READY"));

    OrderEntity foreign = new OrderEntity();
    foreign.setId(11L);
    foreign.setRestaurantId(9L);
    given(orderJpaRepository.findById(11L)).willReturn(Optional.of(foreign));
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateRestaurantOrderStatus(4L, 11L, "READY")
    );
    assertEquals("Order does not belong to restaurant", ex.getMessage());
  }
}
