package com.team7.service.client;

import com.team7.model.client.Menu;
import com.team7.model.client.Restaurant;
import com.team7.repository.client.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    private RestaurantServiceImpl restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantServiceImpl(restaurantRepository);
    }

    @Test
    void getRestaurantsReturnsRestaurantsWithAndWithoutCoordinates() {
        Restaurant withCoordinates = restaurant(1L, 55.751244, 37.618423);
        Restaurant withoutCoordinates = restaurant(2L, null, null);
        when(restaurantRepository.getRestaurants()).thenReturn(List.of(withCoordinates, withoutCoordinates));

        List<Restaurant> result = restaurantService.getRestaurants();

        assertEquals(List.of(withCoordinates, withoutCoordinates), result);
        verify(restaurantRepository).getRestaurants();
    }

    @Test
    void getRestaurantsReturnsEmptyList() {
        when(restaurantRepository.getRestaurants()).thenReturn(List.of());

        List<Restaurant> result = restaurantService.getRestaurants();

        assertEquals(List.of(), result);
        verify(restaurantRepository).getRestaurants();
    }

    @Test
    void getRestaurantByIdReturnsRestaurant() {
        Restaurant restaurant = restaurant(1L, 55.751244, 37.618423);
        when(restaurantRepository.getRestaurantById(1L)).thenReturn(restaurant);

        Restaurant result = restaurantService.getRestaurantById(1L);

        assertSame(restaurant, result);
        verify(restaurantRepository).getRestaurantById(1L);
    }

    @Test
    void getRestaurantByIdPropagatesNotFoundError() {
        when(restaurantRepository.getRestaurantById(404L))
            .thenThrow(new IllegalArgumentException("Restaurant not found"));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> restaurantService.getRestaurantById(404L)
        );

        assertEquals("Restaurant not found", error.getMessage());
        verify(restaurantRepository).getRestaurantById(404L);
    }

    @Test
    void filterRestaurantsReturnsFilteredRestaurants() {
        Restaurant restaurant = restaurant(1L, 55.751244, 37.618423);
        when(restaurantRepository.filterRestaurants(4.5, 30)).thenReturn(List.of(restaurant));

        List<Restaurant> result = restaurantService.filterRestaurants(4.5, 30);

        assertEquals(List.of(restaurant), result);
        verify(restaurantRepository).filterRestaurants(4.5, 30);
    }

    @Test
    void filterRestaurantsReturnsEmptyListWhenNoRestaurantMatches() {
        when(restaurantRepository.filterRestaurants(5.0, 10)).thenReturn(List.of());

        List<Restaurant> result = restaurantService.filterRestaurants(5.0, 10);

        assertEquals(List.of(), result);
        verify(restaurantRepository).filterRestaurants(5.0, 10);
    }

    @Test
    void getMenuReturnsRestaurantMenu() {
        Menu item = new Menu(1L, 1L, "Pizza", "Cheese pizza", 299.0, true);
        when(restaurantRepository.getMenu(1L)).thenReturn(List.of(item));

        List<Menu> result = restaurantService.getMenu(1L);

        assertEquals(List.of(item), result);
        verify(restaurantRepository).getMenu(1L);
    }

    @Test
    void getMenuReturnsEmptyListWhenRestaurantHasNoMenuItems() {
        when(restaurantRepository.getMenu(1L)).thenReturn(List.of());

        List<Menu> result = restaurantService.getMenu(1L);

        assertEquals(List.of(), result);
        verify(restaurantRepository).getMenu(1L);
    }

    private static Restaurant restaurant(Long id, Double latitude, Double longitude) {
        return new Restaurant(
            id,
            "Restaurant " + id,
            "Address " + id,
            latitude,
            longitude,
            "Italian",
            4.7,
            25,
            500.0,
            List.of("09:00-22:00"),
            true
        );
    }
}
