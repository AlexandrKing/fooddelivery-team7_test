package com.team7.service.courieadmin;
import java.time.Instant;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long orderId;
    private Long shopId;
    private Instant createdAt;
    private Long restaurantId;
    private String clientLogin;
    private List<Item> items;
    private String status;
    private Long price;
    private Long profit;
    private String restaurantAddress;
    private String clientAddress;
    private Long courierId;

    public Order() {
        this.items = new ArrayList<>();
        this.createdAt = Instant.now();
        this.status = "NEW";
    }

    public Order(Long orderId, Long shopId, Long restaurantId, String clientLogin,
                 String restaurantAddress, String clientAddress) {
        this();
        this.orderId = orderId;
        this.shopId = shopId;
        this.restaurantId = restaurantId;
        this.clientLogin = clientLogin;
        this.restaurantAddress = restaurantAddress;
        this.clientAddress = clientAddress;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getClientLogin() { return clientLogin; }
    public void setClientLogin(String clientLogin) { this.clientLogin = clientLogin; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public Long getProfit() { return profit; }
    public void setProfit(Long profit) { this.profit = profit; }

    public String getRestaurantAddress() { return restaurantAddress; }
    public void setRestaurantAddress(String restaurantAddress) { this.restaurantAddress = restaurantAddress; }

    public String getClientAddress() { return clientAddress; }
    public void setClientAddress(String clientAddress) { this.clientAddress = clientAddress; }

    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }

    public void addItem(Item item) {
        this.items.add(item);
    }

    public void calculateTotalPrice() {
        this.price = 0L;
        for (Item item : items) {
            this.price += item.getPrice() * item.getQuantity();
        }
        this.profit = (long) (this.price * 0.2);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", restaurant='" + restaurantId + "'" +
                ", client='" + clientLogin + "'" +
                ", status='" + status + "'" +
                ", price=" + price +
                ", items=" + items.size() +
                '}';
    }
}
