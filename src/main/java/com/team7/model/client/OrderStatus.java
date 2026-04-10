package com.team7.model.client;

public enum OrderStatus {
    PENDING,           // Ожидание подтверждения
    ASSIGNED,          // Назначен курьеру (courier_assigned_orders / кабинет курьера)
    ACCEPTED,          // Подтвержден
    PREPARING,         // Готовится
    COOKING,           // В процессе приготовления (синоним для PREPARING)
    READY,             // Готов к выдаче
    PICKED_UP,         // Курьер забрал заказ
    IN_DELIVERY,       // В доставке
    DELIVERING,        // В процессе доставки (синоним для IN_DELIVERY)
    DELIVERED,         // Доставлен
    CANCELLED;         // Отменен

    // Можно добавить методы для удобства
    public boolean isActive() {
        return this != DELIVERED && this != CANCELLED;
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == ACCEPTED || this == PREPARING;
    }

    public static OrderStatus fromString(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}