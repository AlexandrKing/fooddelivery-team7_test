package com.team7.client.service;

import com.team7.client.model.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> getMenu(String restaurantId);
    Menu getMenuItem(String restaurantId, String itemId);
}
