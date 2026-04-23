package com.team7.service.order;

import java.util.Locale;
import java.util.Set;

public final class OrderStatusTransitionPolicy {
  private static final Set<String> CLAIMABLE_ORDER_STATUSES = Set.of(
      "PENDING",
      "ACCEPTED",
      "PREPARING",
      "COOKING",
      "READY"
  );

  private OrderStatusTransitionPolicy() {
  }

  public static boolean canClaimFromOrderStatus(String orderStatus) {
    return CLAIMABLE_ORDER_STATUSES.contains(normalize(orderStatus));
  }

  public static String validateCourierTransition(String currentStatus, String requestedStatus) {
    String current = normalize(currentStatus);
    String next = normalize(requestedStatus);

    if (current.isEmpty()) {
      throw new IllegalArgumentException("Текущий статус назначения курьера не указан");
    }
    if (next.isEmpty()) {
      throw new IllegalArgumentException("Новый статус назначения курьера не указан");
    }

    if ("ASSIGNED".equals(current) && "PICKED_UP".equals(next)) {
      return next;
    }
    if ("PICKED_UP".equals(current) && ("IN_DELIVERY".equals(next) || "DELIVERING".equals(next))) {
      return next;
    }
    if (("IN_DELIVERY".equals(current) || "DELIVERING".equals(current)) && "DELIVERED".equals(next)) {
      return next;
    }

    throw new IllegalArgumentException("Недопустимый переход статуса курьера: " + current + " -> " + next);
  }

  public static String validateRestaurantTransition(String currentStatus, String requestedStatus) {
    String current = normalize(currentStatus);
    String next = normalize(requestedStatus);

    if (current.isEmpty()) {
      throw new IllegalArgumentException("Текущий статус заказа не указан");
    }
    if (next.isEmpty()) {
      throw new IllegalArgumentException("Новый статус заказа не указан");
    }

    if ("PENDING".equals(current) && matches(next, "ACCEPTED", "PREPARING", "COOKING", "READY", "CANCELLED")) {
      return next;
    }
    if ("ACCEPTED".equals(current) && matches(next, "PREPARING", "COOKING", "READY", "CANCELLED")) {
      return next;
    }
    if (("PREPARING".equals(current) || "COOKING".equals(current)) && matches(next, "READY", "CANCELLED")) {
      return next;
    }
    if ("READY".equals(current) && "CANCELLED".equals(next)) {
      return next;
    }

    throw new IllegalArgumentException("Недопустимый переход статуса заказа: " + current + " -> " + next);
  }

  private static boolean matches(String status, String... allowed) {
    for (String value : allowed) {
      if (value.equals(status)) {
        return true;
      }
    }
    return false;
  }

  private static String normalize(String status) {
    return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
  }
}
