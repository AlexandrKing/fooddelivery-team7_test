package com.team7.repository.client;

import com.team7.model.client.Menu;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class ClientMenuRepository {
  private final JdbcTemplate jdbcTemplate;

  private static final RowMapper<Menu> MENU_ROW_MAPPER = (rs, rowNum) -> {
    Menu item = new Menu();
    item.setId(rs.getLong("id"));
    item.setRestaurantId(rs.getLong("restaurant_id"));
    item.setName(rs.getString("name"));
    item.setDescription(rs.getString("description"));
    item.setPrice(rs.getDouble("price"));
    item.setAvailable(rs.getBoolean("is_available"));
    item.setCategory(rs.getString("category"));
    item.setCalories(rs.getObject("calories", Integer.class));
    item.setImageUrl(rs.getString("image_url"));
    item.setCookingTime(rs.getObject("preparation_time_min", Integer.class));
    return item;
  };

  public ClientMenuRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Deprecated(forRemoval = false, since = "1.1")
  public ClientMenuRepository() {
    DataSource ds = DatabaseConfigDataSource.createFallbackDataSource();
    this.jdbcTemplate = new JdbcTemplate(ds);
  }

  public List<Menu> getMenu(Long restaurantId) {
    return jdbcTemplate.query(
        "SELECT * FROM dishes WHERE restaurant_id = ? AND is_available = true",
        MENU_ROW_MAPPER,
        restaurantId
    );
  }

  public Menu getMenuItem(Long restaurantId, Long itemId) {
    List<Menu> items = jdbcTemplate.query(
        "SELECT * FROM dishes WHERE restaurant_id = ? AND id = ? AND is_available = true",
        MENU_ROW_MAPPER,
        restaurantId, itemId
    );
    if (items.isEmpty()) {
      throw new IllegalArgumentException("Блюдо не найдено в меню ресторана");
    }
    return items.get(0);
  }
}

