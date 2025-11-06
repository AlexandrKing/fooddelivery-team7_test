package com.team7.client.model;

import java.util.List;

public class Cart {
    private Long id;
    private Long userId;
    private List<CartItem> items;
    private Double totalAmount;
}
