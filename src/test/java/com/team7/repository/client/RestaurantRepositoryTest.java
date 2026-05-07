package com.team7.repository.client;

import com.team7.model.client.Restaurant;
import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.RestaurantJpaRepository;
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
}

