package com.team7.service.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTransitionPolicyTest {

  @Test
  void canClaimFromConfiguredStatuses() {
    assertTrue(OrderStatusTransitionPolicy.canClaimFromOrderStatus("PENDING"));
    assertTrue(OrderStatusTransitionPolicy.canClaimFromOrderStatus(" accepted "));
    assertTrue(OrderStatusTransitionPolicy.canClaimFromOrderStatus("preparing"));
    assertFalse(OrderStatusTransitionPolicy.canClaimFromOrderStatus("CANCELLED"));
    assertFalse(OrderStatusTransitionPolicy.canClaimFromOrderStatus(null));
  }

  @Test
  void courierTransitionsAllowOnlyConfiguredPath() {
    assertEquals("PICKED_UP", OrderStatusTransitionPolicy.validateCourierTransition("ASSIGNED", "picked_up"));
    assertEquals("DELIVERING", OrderStatusTransitionPolicy.validateCourierTransition("PICKED_UP", "delivering"));
    assertEquals("IN_DELIVERY", OrderStatusTransitionPolicy.validateCourierTransition("PICKED_UP", "in_delivery"));
    assertEquals("DELIVERED", OrderStatusTransitionPolicy.validateCourierTransition("DELIVERING", "delivered"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> OrderStatusTransitionPolicy.validateCourierTransition("ASSIGNED", "DELIVERED")
    );
    assertTrue(ex.getMessage().contains("ASSIGNED -> DELIVERED"));
  }

  @Test
  void restaurantTransitionsAllowOnlyConfiguredPath() {
    assertEquals("ACCEPTED", OrderStatusTransitionPolicy.validateRestaurantTransition("PENDING", "accepted"));
    assertEquals("PREPARING", OrderStatusTransitionPolicy.validateRestaurantTransition("ACCEPTED", "preparing"));
    assertEquals("READY", OrderStatusTransitionPolicy.validateRestaurantTransition("COOKING", "ready"));
    assertEquals("CANCELLED", OrderStatusTransitionPolicy.validateRestaurantTransition("READY", "cancelled"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> OrderStatusTransitionPolicy.validateRestaurantTransition("PREPARING", "DELIVERED")
    );
    assertTrue(ex.getMessage().contains("PREPARING -> DELIVERED"));
  }

  @Test
  void transitionsRejectBlankInputsWithMeaningfulMessages() {
    assertThrows(IllegalArgumentException.class, () -> OrderStatusTransitionPolicy.validateCourierTransition(null, "x"));
    assertThrows(IllegalArgumentException.class, () -> OrderStatusTransitionPolicy.validateCourierTransition("x", null));
    assertThrows(IllegalArgumentException.class, () -> OrderStatusTransitionPolicy.validateRestaurantTransition(null, "x"));
    assertThrows(IllegalArgumentException.class, () -> OrderStatusTransitionPolicy.validateRestaurantTransition("x", null));
  }
}

