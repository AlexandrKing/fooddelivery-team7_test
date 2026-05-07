package com.team7.repository.client;

import com.team7.model.client.Restaurant;
import com.team7.model.client.Menu;
import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.RestaurantJpaRepository;
import com.team7.persistence.entity.DishEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class RestaurantRepositoryTest {

  private RestaurantJpaRepository restaurantJpaRepository;
  private DishJpaRepository dishJpaRepository;
  private RestaurantRepository repo;

  @BeforeEach
  void setUp() {
    restaurantJpaRepository = Mockito.mock(RestaurantJpaRepository.class);
    dishJpaRepository = Mockito.mock(DishJpaRepository.class);
    repo = new RestaurantRepository(restaurantJpaRepository, dishJpaRepository);
  }

  @Test
  void mapsRestaurantsWithNullDefaults() {
    var e = Mockito.mock(com.team7.persistence.entity.RestaurantEntity.class);
    given(e.getId()).willReturn(1L);
    given(e.getName()).willReturn("R");
    given(e.getAddress()).willReturn("A");
    given(e.getLatitude()).willReturn(null);
    given(e.getLongitude()).willReturn(null);
    given(e.getCuisineType()).willReturn(null);
    given(e.getRating()).willReturn(null);
    given(e.getDeliveryTime()).willReturn(null);
    given(e.getMinOrderAmount()).willReturn(null);
    given(e.getIsActive()).willReturn(null);
    given(restaurantJpaRepository.findByIsActiveTrue()).willReturn(List.of(e));

    List<Restaurant> list = repo.getRestaurants();
    assertEquals(1, list.size());
    Restaurant r = list.get(0);
    assertEquals(1L, r.getId());
    assertEquals("R", r.getName());
    assertEquals("A", r.getAddress());
    assertNull(r.getLatitude());
    assertNull(r.getLongitude());
    assertNull(r.getCuisineType());
    assertEquals(0.0, r.getRating());
    assertEquals(0, r.getDeliveryTime());
    assertEquals(0.0, r.getMinOrderAmount());
    assertFalse(r.getIsActive());
  }

  @Test
  void mapsRestaurantsWithConcreteValuesAndEmptyLists() {
    var e = Mockito.mock(com.team7.persistence.entity.RestaurantEntity.class);
    given(e.getId()).willReturn(3L);
    given(e.getName()).willReturn("Cafe");
    given(e.getAddress()).willReturn("Tverskaya 1");
    given(e.getLatitude()).willReturn(55.75);
    given(e.getLongitude()).willReturn(37.61);
    given(e.getCuisineType()).willReturn("Cafe");
    given(e.getRating()).willReturn(4.7);
    given(e.getDeliveryTime()).willReturn(35);
    given(e.getMinOrderAmount()).willReturn(500.0);
    given(e.getIsActive()).willReturn(true);
    given(restaurantJpaRepository.findByIsActiveTrue()).willReturn(List.of(e));
    given(restaurantJpaRepository.findByIdAndIsActiveTrue(3L)).willReturn(Optional.of(e));
    given(restaurantJpaRepository.findActiveFiltered(null, null)).willReturn(List.of());

    Restaurant listed = repo.getRestaurants().get(0);
    assertEquals(3L, listed.getId());
    assertEquals("Cafe", listed.getName());
    assertEquals("Tverskaya 1", listed.getAddress());
    assertEquals(55.75, listed.getLatitude());
    assertEquals(37.61, listed.getLongitude());
    assertEquals("Cafe", listed.getCuisineType());
    assertEquals(4.7, listed.getRating());
    assertEquals(35, listed.getDeliveryTime());
    assertEquals(500.0, listed.getMinOrderAmount());
    assertTrue(listed.getIsActive());

    Restaurant found = repo.getRestaurantById(3L);
    assertEquals("Cafe", found.getName());
    assertTrue(repo.filterRestaurants(null, null).isEmpty());
  }

  @Test
  void getRestaurantByIdThrowsWhenMissing() {
    given(restaurantJpaRepository.findByIdAndIsActiveTrue(99L)).willReturn(Optional.empty());
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> repo.getRestaurantById(99L));
    assertEquals("Ресторан не найден", ex.getMessage());
  }

  @Test
  void filterRestaurantsDelegatesToRepo() {
    var e = Mockito.mock(com.team7.persistence.entity.RestaurantEntity.class);
    given(e.getId()).willReturn(2L);
    given(e.getName()).willReturn("R2");
    given(e.getAddress()).willReturn("A2");
    given(restaurantJpaRepository.findActiveFiltered(Mockito.any(), Mockito.any())).willReturn(List.of(e));
    assertEquals(1, repo.filterRestaurants(4.0, 60).size());
  }

  @Test
  void getRestaurantsAndMenuReturnEmptyListsWhenJpaReturnsEmpty() {
    given(restaurantJpaRepository.findByIsActiveTrue()).willReturn(List.of());
    given(dishJpaRepository.findByRestaurantIdAndIsAvailableTrue(5L)).willReturn(List.of());

    assertTrue(repo.getRestaurants().isEmpty());
    assertTrue(repo.getMenu(5L).isEmpty());
  }

  @Test
  void getMenuMapsAvailableDishesWithValuesAndDefaults() {
    DishEntity priced = new DishEntity();
    priced.setId(11L);
    priced.setRestaurantId(5L);
    priced.setName("Burger");
    priced.setDescription("Tasty");
    priced.setPrice(350.0);
    priced.setIsAvailable(true);
    priced.setCategory("Main");
    priced.setCalories(700);
    priced.setImageUrl("burger.jpg");
    priced.setPreparationTimeMin(20);

    DishEntity defaults = new DishEntity();
    defaults.setId(12L);
    defaults.setRestaurantId(5L);
    defaults.setName("Water");
    defaults.setPrice(null);
    defaults.setIsAvailable(null);

    given(dishJpaRepository.findByRestaurantIdAndIsAvailableTrue(5L)).willReturn(List.of(priced, defaults));

    List<Menu> menu = repo.getMenu(5L);

    assertEquals(2, menu.size());
    Menu first = menu.get(0);
    assertEquals(11L, first.getId());
    assertEquals(5L, first.getRestaurantId());
    assertEquals("Burger", first.getName());
    assertEquals("Tasty", first.getDescription());
    assertEquals(350.0, first.getPrice());
    assertTrue(first.getIsAvailable());
    assertEquals("Main", first.getCategory());
    assertEquals(700, first.getCalories());
    assertEquals("burger.jpg", first.getImageUrl());
    assertEquals(20, first.getCookingTime());

    Menu second = menu.get(1);
    assertEquals(0.0, second.getPrice());
    assertTrue(second.getIsAvailable());
  }
}

