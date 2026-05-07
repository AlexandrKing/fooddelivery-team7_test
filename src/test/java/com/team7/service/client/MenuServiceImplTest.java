package com.team7.service.client;

import com.team7.model.client.Menu;
import com.team7.repository.client.ClientMenuRepository;
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
class MenuServiceImplTest {

    @Mock
    private ClientMenuRepository menuRepository;

    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuServiceImpl(menuRepository);
    }

    @Test
    void getMenuReturnsActiveAndInactiveItemsFromRepository() {
        Menu activeItem = menu(1L, true);
        Menu inactiveItem = menu(2L, false);
        when(menuRepository.getMenu(20L)).thenReturn(List.of(activeItem, inactiveItem));

        List<Menu> result = menuService.getMenu(20L);

        assertEquals(List.of(activeItem, inactiveItem), result);
        verify(menuRepository).getMenu(20L);
    }

    @Test
    void getMenuReturnsEmptyListWhenRestaurantHasNoItems() {
        when(menuRepository.getMenu(20L)).thenReturn(List.of());

        List<Menu> result = menuService.getMenu(20L);

        assertEquals(List.of(), result);
        verify(menuRepository).getMenu(20L);
    }

    @Test
    void getMenuItemReturnsItem() {
        Menu item = menu(7L, true);
        when(menuRepository.getMenuItem(20L, 7L)).thenReturn(item);

        Menu result = menuService.getMenuItem(20L, 7L);

        assertSame(item, result);
        verify(menuRepository).getMenuItem(20L, 7L);
    }

    @Test
    void getMenuItemPropagatesNotFoundError() {
        when(menuRepository.getMenuItem(20L, 404L))
            .thenThrow(new IllegalArgumentException("Menu item not found"));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> menuService.getMenuItem(20L, 404L)
        );

        assertEquals("Menu item not found", error.getMessage());
        verify(menuRepository).getMenuItem(20L, 404L);
    }

    private static Menu menu(Long id, boolean available) {
        return new Menu(id, 20L, "Dish " + id, "Description", 199.0, available);
    }
}
