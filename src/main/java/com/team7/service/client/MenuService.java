package com.team7.service.client;

import com.team7.model.client.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> getMenu(Long restaurantId);
    Menu getMenuItem(Long restaurantId, Long itemId);
}
