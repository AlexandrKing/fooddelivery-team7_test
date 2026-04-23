package com.team7.service.restaurant;

import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.DishEntity;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RestaurantManagementServiceTest {

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
    OrderEntity order = new OrderEntity();
    order.setId(1L);
    order.setRestaurantId(3L);
    given(orderJpaRepository.findByRestaurantIdOrderByCreatedAtDesc(3L)).willReturn(List.of(order));

    List<OrderEntity> result = service.getRestaurantOrders(3L);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId());
  }

  @Test
  void updateRestaurantOrderStatusCoversHappyPathAndRestrictions() {
    OrderEntity order = new OrderEntity();
    order.setId(10L);
    order.setRestaurantId(3L);
    order.setStatus("PENDING");
    given(orderJpaRepository.findById(10L)).willReturn(Optional.of(order));
    given(orderJpaRepository.save(any(OrderEntity.class))).willAnswer(a -> a.getArgument(0));

    OrderEntity updated = service.updateRestaurantOrderStatus(3L, 10L, "preparing");
    assertEquals("PREPARING", updated.getStatus());

    IllegalArgumentException invalidTransition = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateRestaurantOrderStatus(3L, 10L, "delivered")
    );
    assertTrue(invalidTransition.getMessage().contains("Недопустимый переход статуса заказа"));
    assertTrue(invalidTransition.getMessage().contains("PREPARING -> DELIVERED"));

    given(orderJpaRepository.findById(99L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.updateRestaurantOrderStatus(3L, 99L, "ready"));

    OrderEntity foreignOrder = new OrderEntity();
    foreignOrder.setId(11L);
    foreignOrder.setRestaurantId(5L);
    given(orderJpaRepository.findById(11L)).willReturn(Optional.of(foreignOrder));
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateRestaurantOrderStatus(3L, 11L, "ready")
    );
    assertEquals("Order does not belong to restaurant", ex.getMessage());
  }

  @Test
  void menuCrudCoversHappyPathNotFoundAndForeignRestaurant() {
    DishEntity dish = dish(50L, 3L, "Pizza");
    given(dishJpaRepository.findByRestaurantIdAndIsAvailableTrue(3L)).willReturn(List.of(dish));
    assertEquals(1, service.getMenu(3L).size());

    given(dishJpaRepository.save(any(DishEntity.class))).willAnswer(a -> a.getArgument(0));
    DishEntity created = service.createDish(3L, dish(null, null, "Pasta"));
    assertEquals(3L, created.getRestaurantId());
    assertEquals(true, created.getIsAvailable());

    given(dishJpaRepository.findById(50L)).willReturn(Optional.of(dish));
    DishEntity patch = new DishEntity();
    patch.setName("Pizza 2");
    DishEntity updated = service.updateDish(3L, 50L, patch);
    assertEquals("Pizza 2", updated.getName());

    given(dishJpaRepository.findById(404L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.updateDish(3L, 404L, patch));

    DishEntity foreign = dish(60L, 9L, "Other");
    given(dishJpaRepository.findById(60L)).willReturn(Optional.of(foreign));
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.deleteDish(3L, 60L));
    assertEquals("Dish does not belong to restaurant", ex.getMessage());

    given(dishJpaRepository.findById(50L)).willReturn(Optional.of(dish));
    service.deleteDish(3L, 50L);
    verify(dishJpaRepository).delete(dish);
  }

  private static DishEntity dish(Long id, Long restaurantId, String name) {
    DishEntity d = new DishEntity();
    d.setId(id);
    d.setRestaurantId(restaurantId);
    d.setName(name);
    d.setIsAvailable(Boolean.TRUE);
    d.setPrice(500.0);
    return d;
  }
}
