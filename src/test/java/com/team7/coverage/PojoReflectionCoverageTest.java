package com.team7.coverage;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PojoReflectionCoverageTest {

  @Test
  void smokeAllModelAndEntityPojosViaReflection() {
    List<Class<?>> types = List.of(
        // model.*
        com.team7.model.client.Address.class,
        com.team7.model.client.Cart.class,
        com.team7.model.client.CartItem.class,
        com.team7.model.client.Menu.class,
        com.team7.model.client.Order.class,
        com.team7.model.client.OrderItem.class,
        com.team7.model.client.Restaurant.class,
        com.team7.model.client.Review.class,
        com.team7.model.client.User.class,
        com.team7.model.authcoderequest.AuthCodeRequest.class,
        com.team7.model.admin.Admin.class,
        com.team7.model.courier.AssignedOrder.class,
        com.team7.model.courier.Courier.class,
        com.team7.model.review.Review.class,
        com.team7.model.restaurant.Address.class,
        com.team7.model.restaurant.Dish.class,
        com.team7.model.restaurant.MenuCategory.class,
        com.team7.model.restaurant.Restaurant.class,

        // persistence.entity.*
        com.team7.persistence.entity.UserEntity.class,
        com.team7.persistence.entity.RestaurantEntity.class,
        com.team7.persistence.entity.DishEntity.class,
        com.team7.persistence.entity.CartEntity.class,
        com.team7.persistence.entity.CartItemEntity.class,
        com.team7.persistence.entity.OrderEntity.class,
        com.team7.persistence.entity.OrderItemEntity.class,
        com.team7.persistence.entity.OrderStatusHistoryEntity.class,
        com.team7.persistence.entity.CourierUserEntity.class,
        com.team7.persistence.entity.CourierAssignedOrderEntity.class,
        com.team7.persistence.entity.ReviewEntity.class,
        com.team7.persistence.entity.AddressEntity.class,
        com.team7.persistence.entity.AdminUserEntity.class,
        com.team7.persistence.entity.AppAccountEntity.class
    );

    for (Class<?> type : types) {
      Object instance = newInstance(type);
      assertNotNull(instance);

      for (Method m : type.getMethods()) {
        if (m.getDeclaringClass() == Object.class) continue;
        if (m.getName().equals("getClass")) continue;

        if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
          Object arg = dummyValue(m.getParameterTypes()[0]);
          assertDoesNotThrow(() -> m.invoke(instance, arg), type.getName() + "#" + m.getName());
        }
      }

      for (Method m : type.getMethods()) {
        if (m.getDeclaringClass() == Object.class) continue;
        if (m.getName().equals("getClass")) continue;

        if ((m.getName().startsWith("get") || m.getName().startsWith("is")) && m.getParameterCount() == 0) {
          assertDoesNotThrow(() -> m.invoke(instance), type.getName() + "#" + m.getName());
        }
      }

      assertDoesNotThrow(instance::toString);
      assertDoesNotThrow(instance::hashCode);
      assertDoesNotThrow(() -> instance.equals(instance));
    }
  }

  private static Object dummyValue(Class<?> t) {
    if (t == String.class) return "x";
    if (t == Long.class || t == long.class) return 1L;
    if (t == Integer.class || t == int.class) return 1;
    if (t == Double.class || t == double.class) return 1.0;
    if (t == Boolean.class || t == boolean.class) return true;
    if (t == BigDecimal.class) return BigDecimal.ONE;
    if (t == LocalDateTime.class) return LocalDateTime.now();
    if (List.class.isAssignableFrom(t)) return new ArrayList<>();
    if (t.isEnum()) return t.getEnumConstants().length > 0 ? t.getEnumConstants()[0] : null;
    // For other reference types, pass null to keep it safe.
    return null;
  }

  private static Object newInstance(Class<?> type) {
    try {
      Constructor<?> c = type.getDeclaredConstructor();
      c.setAccessible(true);
      return c.newInstance();
    } catch (NoSuchMethodException e) {
      // fall back: try any constructor with all-null args
      try {
        Constructor<?> c = type.getDeclaredConstructors()[0];
        c.setAccessible(true);
        Object[] args = new Object[c.getParameterCount()];
        return c.newInstance(args);
      } catch (Exception ex) {
        throw new AssertionError("Cannot instantiate " + type.getName(), ex);
      }
    } catch (Exception e) {
      throw new AssertionError("Cannot instantiate " + type.getName(), e);
    }
  }
}

