package com.team7.service.client;

import com.team7.model.client.Menu;
import com.team7.repository.client.ClientMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {
  private final ClientMenuRepository menuRepository;

  // TODO(legacy-cleanup): remove this constructor after userstory/* is deleted in Wave 3.
  @Deprecated(forRemoval = false, since = "1.1")
  public MenuServiceImpl() {
    this.menuRepository = new ClientMenuRepository();
  }

  @Autowired
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