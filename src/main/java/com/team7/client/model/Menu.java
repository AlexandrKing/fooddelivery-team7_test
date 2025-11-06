package com.team7.client.model;

import java.util.List;

public class Menu {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private List<MenuItemOption> options;
    private Boolean isAvailable;
}
