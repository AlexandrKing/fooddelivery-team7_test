package com.team7.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long id;
    private Long menuItemId;
    private Long restaurantId;
    private Integer quantity;
}
