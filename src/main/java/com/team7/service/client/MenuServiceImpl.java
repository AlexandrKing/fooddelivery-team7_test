package com.team7.service.client;

import com.team7.model.client.Menu;
import com.team7.repository.client.ClientMenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {
  private final ClientMenuRepository menuRepository;

  public MenuServiceImpl(ClientMenuRepository menuRepository) {
    this.menuRepository = menuRepository;
  }

  @Override
  public List<Menu> getMenu(Long restaurantId) {
    return menuRepository.getMenu(restaurantId);
  }

  @Override
  public Menu getMenuItem(Long restaurantId, Long itemId) {
    return menuRepository.getMenuItem(restaurantId, itemId);
  }
}