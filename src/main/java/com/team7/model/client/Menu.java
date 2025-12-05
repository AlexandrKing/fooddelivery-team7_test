package com.team7.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Boolean isAvailable;

    public void setRestaurantId(long restaurantId) {
    }

    public void setAvailable(boolean isAvailable) {
    }

    public void setCategory(String category) {
    }

    public void setCalories(int calories) {
    }

    public void setWeight(double weight) {
    }

    public void setImageUrl(String imageUrl) {
    }

    public void setCookingTime(int cookingTime) {
    }

    public void setName(int cookingTime) {

    }

    public void setName(String name) {
    }
}
