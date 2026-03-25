package com.team7.repository.client;

import com.team7.model.client.Menu;
import com.team7.model.client.Restaurant;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RestaurantRepository {
  private final JdbcTemplate jdbcTemplate;

  private static final RowMapper<Restaurant> RESTAURANT_ROW_MAPPER = (rs, rowNum) -> {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(rs.getLong("id"));
    restaurant.setName(rs.getString("name"));
    restaurant.setAddress(rs.getString("address"));
    restaurant.setCuisineType(rs.getString("cuisine_type"));
    restaurant.setRating(rs.getDouble("rating"));
    restaurant.setDeliveryTime(rs.getInt("delivery_time"));
    restaurant.setMinOrderAmount(rs.getDouble("min_order_amount"));
    restaurant.setIsActive(rs.getBoolean("is_active"));
    return restaurant;
  };

  private static final RowMapper<Menu> MENU_ROW_MAPPER = (rs, rowNum) -> {
    Menu item = new Menu();
    item.setId(rs.getLong("id"));
    item.setRestaurantId(rs.getLong("restaurant_id"));
    item.setName(rs.getString("name"));
    item.setDescription(rs.getString("description"));
    item.setPrice(rs.getDouble("price"));
    item.setAvailable(rs.getBoolean("is_available"));
    return item;
  };

  public RestaurantRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * No-arg fallback constructor for legacy userstory code.
   */
  // TODO(legacy-cleanup): remove this fallback constructor in Wave 2.
  @Deprecated(forRemoval = false, since = "1.1")
  public RestaurantRepository() {
    DataSource ds = DatabaseConfigDataSource.createFallbackDataSource();
    this.jdbcTemplate = new JdbcTemplate(ds);
  }

  public List<Restaurant> getRestaurants() {
    return jdbcTemplate.query(
        "SELECT * FROM restaurants WHERE is_active = true",
        RESTAURANT_ROW_MAPPER
    );
  }

  public Restaurant getRestaurantById(Long id) {
    try {
      return jdbcTemplate.queryForObject(
          "SELECT * FROM restaurants WHERE id = ? AND is_active = true",
          RESTAURANT_ROW_MAPPER,
          id
      );
    } catch (EmptyResultDataAccessException e) {
      throw new IllegalArgumentException("Ресторан не найден");
    }
  }

  public List<Restaurant> filterRestaurants(Double rating, Integer deliveryTime) {
    StringBuilder sql = new StringBuilder("SELECT * FROM restaurants WHERE is_active = true");
    List<Object> params = new ArrayList<>();

    if (rating != null) {
      sql.append(" AND rating >= ?");
      params.add(rating);
    }
    if (deliveryTime != null) {
      sql.append(" AND delivery_time <= ?");
      params.add(deliveryTime);
    }

    return jdbcTemplate.query(sql.toString(), RESTAURANT_ROW_MAPPER, params.toArray());
  }

  public List<Menu> getMenu(Long restaurantId) {
    return jdbcTemplate.query(
        "SELECT * FROM menu WHERE restaurant_id = ? AND is_available = true",
        MENU_ROW_MAPPER,
        restaurantId
    );
  }
}

