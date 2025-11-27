package com.team7.courieradmin.service;

public class Item {
    private Long itemId;
    private String name;
    private String description;
    private Long price;
    private Integer quantity;

    public Item() {}

    public Item(Long itemId, String name, String description, Long price, Integer quantity) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Long getTotalPrice() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return name + " x" + quantity + " - " + getTotalPrice() + " руб.";
    }
}