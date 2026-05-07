package com.team7.repository.client;

import com.team7.model.client.Menu;
import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.entity.DishEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class ClientMenuRepositoryTest {

  private DishJpaRepository dishJpaRepository;
  private ClientMenuRepository repo;

  @BeforeEach
  void setUp() {
    dishJpaRepository = Mockito.mock(DishJpaRepository.class);
    repo = new ClientMenuRepository(dishJpaRepository);
  }

  @Test
  void getMenuMapsAvailableDishes() {
    DishEntity e = new DishEntity();
    e.setId(1L);
    e.setRestaurantId(2L);
    e.setName("Burger");
    e.setPrice(10.0);
    e.setIsAvailable(true);
    given(dishJpaRepository.findByRestaurantIdAndIsAvailableTrue(2L)).willReturn(List.of(e));

    List<Menu> menu = repo.getMenu(2L);
    assertEquals(1, menu.size());
    assertEquals("Burger", menu.get(0).getName());
  }

  @Test
  void getMenuItemThrowsWhenMissing() {
    given(dishJpaRepository.findByRestaurantIdAndIdAndIsAvailableTrue(2L, 9L)).willReturn(Optional.empty());
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> repo.getMenuItem(2L, 9L));
    assertEquals("Блюдо не найдено в меню ресторана", ex.getMessage());
  }
}

