package com.team7.model.client;

public enum OrderStatus {
    PENDING,           // Ожидание подтверждения
    ACCEPTED,          // Подтвержден
    PREPARING,         // Готовится
    COOKING,           // В процессе приготовления (синоним для PREPARING)
    READY,             // Готов к выдаче
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