package com.team7.client.model;

import java.util.List;

public class CartItem {
    private Long id;
    private Long menuItemId;
    private Long restaurantId;
    private Integer quantity;
    private List<SelectedOption> selectedOptions;
}
