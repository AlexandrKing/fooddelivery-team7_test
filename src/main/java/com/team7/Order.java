package com.team7;
import java.util.List;
import java.time.Instant;

public class Order {
    private Long orderId;
    private Long shopId;
    private Instant createdAt;
    private Long restaurantId;
    private Long clientLogin;
    private List<Item> items;
    private String status;
    private Long price;
    private Long profit;
    private String restaurantAdress;
    private String clientAdress;
}
