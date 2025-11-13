package com.team7.client.model;

import java.util.List;

public class OrderItem {
    private Long id;
    private Long menuItemId;
    private String name;
    private Double price;
    private Integer quantity;
    private List<SelectedOption> selectedOptions;
}
