package com.team7.client.service;

import com.team7.client.model.*;
import java.util.*;

public class MenuServiceImpl implements MenuService {
    private static final Map<Long, List<Menu>> MENUS = new HashMap<>();

    @Override
    public List<Menu> getMenu(Long restaurantId) {
        return MENUS.getOrDefault(restaurantId, new ArrayList<>());
    }

    @Override
    public Menu getMenuItem(Long restaurantId, Long itemId) {
        List<Menu> menu = getMenu(restaurantId);

        return menu.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено"));
    }
}
