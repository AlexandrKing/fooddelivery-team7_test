package com.team7.service.courieradmin;

import com.team7.model.courier.Courier;
import com.team7.service.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourierService {

    public Courier login(String username, String password) {
        // Используем правильное имя таблицы - courier_users
        String sql = "SELECT * FROM courier_users WHERE username = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // ВНИМАНИЕ: В ваших тестовых данных используется колонка password, а не password_hash
                // Проверяем обе возможности
                String storedHash = null;

                // Пробуем получить password_hash
                try {
                    storedHash = rs.getString("password_hash");
                } catch (SQLException e) {
                    // Если колонки нет, пробуем password
                    storedHash = rs.getString("password");
                }

                // Если все равно null, ищем в других возможных колонках
                if (storedHash == null) {
                    storedHash = rs.getString("password");
                }

                if (storedHash != null && BCrypt.checkpw(password, storedHash)) {
                    Courier courier = mapResultSetToCourier(rs);
                    updateLastLogin(courier.getId());
                    System.out.println("✅ Успешный вход курьера: " + username);
                    return courier;
                } else {
                    System.out.println("❌ Неверный пароль для пользователя: " + username);
                }
            } else {
                System.out.println("❌ Пользователь не найден или неактивен: " + username);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при входе курьера: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private void updateLastLogin(Long courierId) {
        String sql = "UPDATE courier_users SET last_login_at = CURRENT_TIMESTAMP, last_activity_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, courierId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Ошибка обновления времени входа: " + e.getMessage());
        }
    }

    public boolean registerCourier(String username, String password, String fullName,
                                   String email, String phone, String vehicleType) {
        System.out.println("Попытка регистрации курьера: " + username);

        // Сначала проверяем существование пользователя
        try {
            if (isUsernameExists(username)) {
                System.err.println("❌ Логин уже занят: " + username);
                throw new IllegalArgumentException("Логин уже занят");
            }

            if (isEmailExists(email)) {
                System.err.println("❌ Email уже зарегистрирован: " + email);
                throw new IllegalArgumentException("Email уже зарегистрирован");
            }
        } catch (IllegalArgumentException e) {
            throw e; // Пробрасываем дальше для обработки в UI
        }

        // Используем правильное имя таблицы - courier_users
        // ВНИМАНИЕ: В ваших тестовых данных колонка называется password, а не password_hash
        // Проверим структуру таблицы и используем правильные имена колонок
        String sql = "INSERT INTO courier_users (username, password_hash, full_name, email, phone, vehicle_type, status, rating, completed_orders, balance, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword); // password_hash
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setString(6, vehicleType != null ? vehicleType : "bicycle");
            stmt.setString(7, "offline"); // начальный статус
            stmt.setBigDecimal(8, new BigDecimal("0.0")); // начальный рейтинг
            stmt.setInt(9, 0); // выполненных заказов
            stmt.setBigDecimal(10, new BigDecimal("0.00")); // начальный баланс
            stmt.setBoolean(11, true); // активен

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Курьер успешно зарегистрирован: " + username);

                // Получаем ID нового курьера
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long newId = generatedKeys.getLong(1);
                        System.out.println("ID нового курьера: " + newId);
                    }
                }
                return true;
            } else {
                System.err.println("❌ Не удалось зарегистрировать курьера, affectedRows = 0");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка регистрации курьера: " + e.getMessage());
            e.printStackTrace();

            // Пробуем альтернативный вариант с колонкой password вместо password_hash
            if (e.getMessage().contains("password_hash")) {
                System.out.println("Пробуем с колонкой 'password' вместо 'password_hash'...");
                return registerCourierAlternative(username, password, fullName, email, phone, vehicleType);
            }
        }

        return false;
    }

    private boolean registerCourierAlternative(String username, String password, String fullName,
                                               String email, String phone, String vehicleType) {
        // Альтернативный вариант с колонкой password
        String sql = "INSERT INTO courier_users (username, password, full_name, email, phone, vehicle_type, status, rating, completed_orders, balance, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword); // password
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setString(6, vehicleType != null ? vehicleType : "bicycle");
            stmt.setString(7, "offline");
            stmt.setBigDecimal(8, new BigDecimal("0.0"));
            stmt.setInt(9, 0);
            stmt.setBigDecimal(10, new BigDecimal("0.00"));
            stmt.setBoolean(11, true);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Курьер успешно зарегистрирован (альтернативный метод): " + username);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка регистрации курьера (альтернативный метод): " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM courier_users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                System.out.println("Проверка логина '" + username + "': " + (exists ? "существует" : "не существует"));
                return exists;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка проверки логина: " + e.getMessage());
            e.printStackTrace();
            // Если ошибка - считаем что существует, чтобы не создавать дубликаты
            return true;
        }

        return false;
    }

    private boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM courier_users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                System.out.println("Проверка email '" + email + "': " + (exists ? "существует" : "не существует"));
                return exists;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка проверки email: " + e.getMessage());
            e.printStackTrace();
            // Если ошибка - считаем что существует, чтобы не создавать дубликаты
            return true;
        }

        return false;
    }

    public List<Courier> getAllCouriers() {
        List<Courier> couriers = new ArrayList<>();
        String sql = "SELECT * FROM courier_users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                couriers.add(mapResultSetToCourier(rs));
            }

            System.out.println("Получено курьеров: " + couriers.size());

        } catch (SQLException e) {
            System.err.println("Ошибка получения курьеров: " + e.getMessage());
            e.printStackTrace();
        }

        return couriers;
    }

    public List<Courier> getAvailableCouriers() {
        List<Courier> couriers = new ArrayList<>();
        String sql = "SELECT * FROM courier_users WHERE status = 'available' AND is_active = TRUE ORDER BY rating DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                couriers.add(mapResultSetToCourier(rs));
            }

            System.out.println("Доступных курьеров: " + couriers.size());

        } catch (SQLException e) {
            System.err.println("Ошибка получения доступных курьеров: " + e.getMessage());
        }

        return couriers;
    }

    public Courier getCourierById(Long id) {
        String sql = "SELECT * FROM courier_users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCourier(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения курьера: " + e.getMessage());
        }

        return null;
    }

    public boolean updateCourierStatus(Long courierId, String status) {
        String sql = "UPDATE courier_users SET status = ?, last_activity_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setLong(2, courierId);

            int result = stmt.executeUpdate();
            System.out.println("Статус курьера #" + courierId + " изменен на '" + status + "', affected rows: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка обновления статуса курьера: " + e.getMessage());
        }

        return false;
    }

    public boolean updateCourierLocation(Long courierId, String location) {
        String sql = "UPDATE courier_users SET current_location = ?, last_activity_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location);
            stmt.setLong(2, courierId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка обновления локации курьера: " + e.getMessage());
        }

        return false;
    }

    public boolean addMoneyToCourier(Long courierId, BigDecimal amount) {
        String sql = "UPDATE courier_users SET balance = balance + ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, amount);
            stmt.setLong(2, courierId);

            int result = stmt.executeUpdate();
            System.out.println("Баланс курьера #" + courierId + " увеличен на " + amount + ", affected rows: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка пополнения баланса курьера: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean incrementCompletedOrders(Long courierId) {
        String sql = "UPDATE courier_users SET completed_orders = completed_orders + 1 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, courierId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка обновления счетчика заказов: " + e.getMessage());
        }

        return false;
    }

    public boolean blockCourier(Long courierId) {
        String sql = "UPDATE courier_users SET is_active = FALSE, status = 'offline' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, courierId);

            int result = stmt.executeUpdate();
            System.out.println("Курьер #" + courierId + " заблокирован, affected rows: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка блокировки курьера: " + e.getMessage());
        }

        return false;
    }

    public boolean unblockCourier(Long courierId) {
        String sql = "UPDATE courier_users SET is_active = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, courierId);

            int result = stmt.executeUpdate();
            System.out.println("Курьер #" + courierId + " разблокирован, affected rows: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка разблокировки курьера: " + e.getMessage());
        }

        return false;
    }

    public boolean addCommissionToAll(Long orderId) {
        String sql = "UPDATE courier_users SET balance = balance + 10 WHERE status = 'available' AND is_active = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            int count = stmt.executeUpdate(sql);
            System.out.println("Начислено 10 руб. " + count + " активным курьерам за заказ " + orderId);
            return count > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка начисления комиссии всем курьерам: " + e.getMessage());
        }

        return false;
    }

    public boolean addCommissionToCourier(Long orderId, Long courierId) {
        try {
            String orderSql = "SELECT total_amount FROM client_orders WHERE id = ?";
            BigDecimal orderAmount = null;

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(orderSql)) {

                stmt.setLong(1, orderId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    orderAmount = rs.getBigDecimal("total_amount");
                    System.out.println("Сумма заказа #" + orderId + ": " + orderAmount);
                }
            }

            if (orderAmount != null) {
                BigDecimal commission = orderAmount.multiply(new BigDecimal("0.50"));
                System.out.println("Комиссия 50%: " + commission);

                String updateSql = "UPDATE courier_users SET balance = balance + ? WHERE id = ?";
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateSql)) {

                    stmt.setBigDecimal(1, commission);
                    stmt.setLong(2, courierId);

                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        System.out.println("Начислено " + commission + " руб. курьеру #" + courierId + " за заказ " + orderId);
                        return true;
                    } else {
                        System.err.println("Не удалось начислить комиссию курьеру #" + courierId);
                    }
                }
            } else {
                System.err.println("Не удалось получить сумму заказа #" + orderId);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка начисления комиссии курьеру: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private Courier mapResultSetToCourier(ResultSet rs) throws SQLException {
        Courier courier = new Courier();

        try {
            courier.setId(rs.getLong("id"));
            courier.setUsername(rs.getString("username"));
            courier.setEmail(rs.getString("email"));

            // Пробуем получить password_hash или password
            try {
                courier.setPasswordHash(rs.getString("password_hash"));
            } catch (SQLException e) {
                courier.setPasswordHash(rs.getString("password"));
            }

            courier.setFullName(rs.getString("full_name"));
            courier.setPhone(rs.getString("phone"));
            courier.setVehicleType(rs.getString("vehicle_type"));
            courier.setStatus(rs.getString("status"));
            courier.setCurrentLocation(rs.getString("current_location"));

            BigDecimal rating = rs.getBigDecimal("rating");
            if (rs.wasNull()) {
                rating = new BigDecimal("0.0");
            }
            courier.setRating(rating);

            courier.setCompletedOrders(rs.getInt("completed_orders"));

            BigDecimal balance = rs.getBigDecimal("balance");
            if (rs.wasNull()) {
                balance = new BigDecimal("0.00");
            }
            courier.setBalance(balance);

            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                courier.setCreatedAt(createdAt.toLocalDateTime());
            }

            Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
            if (lastLoginAt != null) {
                courier.setLastLoginAt(lastLoginAt.toLocalDateTime());
            }

            Timestamp lastActivityAt = rs.getTimestamp("last_activity_at");
            if (lastActivityAt != null) {
                courier.setLastActivityAt(lastActivityAt.toLocalDateTime());
            }

            courier.setIsActive(rs.getBoolean("is_active"));

        } catch (SQLException e) {
            System.err.println("Ошибка при маппинге ResultSet в Courier: " + e.getMessage());
            e.printStackTrace();
        }

        return courier;
    }

    // Дополнительный метод для создания таблицы если её нет
    public static boolean createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS courier_users (" +
                "id SERIAL PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "password_hash VARCHAR(255) NOT NULL," +
                "full_name VARCHAR(100) NOT NULL," +
                "phone VARCHAR(20)," +
                "vehicle_type VARCHAR(20)," +
                "status VARCHAR(20) DEFAULT 'offline'," +
                "current_location TEXT," +
                "rating DECIMAL(3,2) DEFAULT 0.0," +
                "completed_orders INT DEFAULT 0," +
                "balance DECIMAL(10,2) DEFAULT 0.00," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_login_at TIMESTAMP," +
                "last_activity_at TIMESTAMP," +
                "is_active BOOLEAN DEFAULT TRUE" +
                ")";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("✅ Таблица 'courier_users' создана или уже существует");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Ошибка создания таблицы 'courier_users': " + e.getMessage());
            return false;
        }
    }

    // Метод для проверки структуры таблицы
    public static void checkTableStructure() {
        String sql = "SELECT column_name, data_type, is_nullable " +
                "FROM information_schema.columns " +
                "WHERE table_name = 'courier_users' " +
                "ORDER BY ordinal_position";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nСтруктура таблицы 'courier_users':");
            System.out.println("====================================");
            while (rs.next()) {
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                String nullable = rs.getString("is_nullable");
                System.out.printf("%-20s %-20s %s%n", columnName, dataType, nullable);
            }
            System.out.println("====================================\n");

        } catch (SQLException e) {
            System.err.println("Ошибка проверки структуры таблицы: " + e.getMessage());
        }
    }
}