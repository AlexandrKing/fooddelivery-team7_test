package com.team7.model.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

  @Test
  void fromStringIsCaseInsensitiveAndReturnsNullForUnknown() {
    assertEquals(OrderStatus.PENDING, OrderStatus.fromString("pending"));
    assertEquals(OrderStatus.ASSIGNED, OrderStatus.fromString("assigned"));
    assertEquals(OrderStatus.CANCELLED, OrderStatus.fromString("CANCELLED"));

    assertNull(OrderStatus.fromString("unknown_status"));
    assertThrows(NullPointerException.class, () -> OrderStatus.fromString(null));
  }

  @Test
  void isActiveTrueForActiveStatesAndFalseForDeliveredCancelled() {
    assertTrue(OrderStatus.PENDING.isActive());
    assertTrue(OrderStatus.ASSIGNED.isActive());
    assertTrue(OrderStatus.READY.isActive());
    assertFalse(OrderStatus.DELIVERED.isActive());
    assertFalse(OrderStatus.CANCELLED.isActive());
  }

  @Test
  void canBeCancelledOnlyForAllowedStates() {
    assertTrue(OrderStatus.PENDING.canBeCancelled());
    assertTrue(OrderStatus.ACCEPTED.canBeCancelled());
    assertTrue(OrderStatus.PREPARING.canBeCancelled());

    assertFalse(OrderStatus.ASSIGNED.canBeCancelled());
    assertFalse(OrderStatus.READY.canBeCancelled());
    assertFalse(OrderStatus.CANCELLED.canBeCancelled());
  }
}

