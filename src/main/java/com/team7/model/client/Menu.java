package com.team7.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private Double price;
    private Boolean available;
    private String category;
    private Integer calories;
    private Double weight;
    private String imageUrl;
    private Integer cookingTime;

    // Дополнительные геттеры для совместимости с различными стилями
    public Boolean getIsAvailable() {
        return available;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.available = isAvailable;
    }

    // Конструктор с основными полями (для удобства)
    public Menu(Long id, Long restaurantId, String name, String description, Double price, Boolean available) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
    }

    // Конструктор для тестов
    public Menu(Long id, String name, String description, Double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = true;
    }

    // Билдер-методы для удобного создания объектов
    public Menu withRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
        return this;
    }

    public Menu withCategory(String category) {
        this.category = category;
        return this;
    }

    public Menu withImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    // Метод для проверки доступности
    public boolean isAvailable() {
        return available != null && available;
    }

    // Метод для форматированного вывода
    @Override
    public String toString() {
        return String.format("Menu{id=%d, name='%s', price=%.2f, available=%s}",
            id, name, price, available);
    }
}