package com.team7.client.service;

import com.team7.client.model.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> getMenu(Long restaurantId);
    Menu getMenuItem(Long restaurantId, Long itemId);
}
