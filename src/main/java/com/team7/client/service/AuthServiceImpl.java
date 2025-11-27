package com.team7.client.service;

import com.team7.client.model.User;
import com.team7.client.model.UserRole;
import com.team7.client.model.Address;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AuthServiceImpl implements AuthService {
    private User currentUser;
    private final DatabaseService dbService = new DatabaseService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+79[0-9]{9}$");

    @Override
    public User register(UserRole role, String name, String email, String phone, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Неверный формат email");
        }

        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Телефон должен быть в формате +79XXXXXXXXX");
        }

        if (!isEmailAvailable(email)) {
            throw new IllegalArgumentException("Email уже используется");
        }

        if (!isPhoneAvailable(phone)) {
            throw new IllegalArgumentException("Номер телефона уже используется");
        }

        String sql = "INSERT INTO users (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, password);
            pstmt.setString(5, role.name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getLong(1));
                        user.setRole(role);
                        user.setName(name);
                        user.setEmail(email);
                        user.setPhone(phone);
                        user.setPassword(password);
                        user.setAddresses(new ArrayList<>());
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при регистрации: " + e.getMessage(), e);
        }

        throw new RuntimeException("Не удалось зарегистрировать пользователя");
    }

    @Override
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(UserRole.valueOf(rs.getString("role")));
                    user.setAddresses(getUserAddresses(user.getId()));

                    currentUser = user;
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при входе: " + e.getMessage(), e);
        }

        throw new IllegalArgumentException("Неверный email или пароль");
    }

    @Override
    public void logout() {
        currentUser = null;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public boolean isEmailAvailable(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при проверке email: " + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ?";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при проверке телефона: " + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public User updateProfile(User updatedUser) {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ? WHERE id = ?";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, updatedUser.getName());
            pstmt.setString(2, updatedUser.getEmail());
            pstmt.setString(3, updatedUser.getPhone());
            pstmt.setLong(4, updatedUser.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                if (currentUser != null && currentUser.getId().equals(updatedUser.getId())) {
                    currentUser.setName(updatedUser.getName());
                    currentUser.setEmail(updatedUser.getEmail());
                    currentUser.setPhone(updatedUser.getPhone());
                }
                return updatedUser;
            } else {
                throw new IllegalArgumentException("Пользователь не найден");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении профиля: " + e.getMessage(), e);
        }
    }

    @Override
    public User addAddress(Long userId, Address address) {
        String sql = "INSERT INTO addresses (user_id, label, address, apartment, entrance, floor, comment) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, address.getLabel());
            pstmt.setString(3, address.getAddress());
            pstmt.setString(4, address.getApartment());
            pstmt.setString(5, address.getEntrance());
            pstmt.setString(6, address.getFloor());
            pstmt.setString(7, address.getComment());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        address.setId(rs.getLong(1));

                        if (currentUser != null && currentUser.getId().equals(userId)) {
                            currentUser.getAddresses().add(address);
                        }
                        return currentUser;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении адреса: " + e.getMessage(), e);
        }

        throw new RuntimeException("Не удалось добавить адрес");
    }

    @Override
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ? AND password = ?";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setLong(2, userId);
            pstmt.setString(3, oldPassword);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                if (currentUser != null && currentUser.getId().equals(userId)) {
                    currentUser.setPassword(newPassword);
                }
                return currentUser;
            } else {
                throw new IllegalArgumentException("Неверный текущий пароль или пользователь не найден");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при смене пароля: " + e.getMessage(), e);
        }
    }

    private List<Address> getUserAddresses(Long userId) {
        List<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ?";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Address address = new Address();
                    address.setId(rs.getLong("id"));
                    address.setLabel(rs.getString("label"));
                    address.setAddress(rs.getString("address"));
                    address.setApartment(rs.getString("apartment"));
                    address.setEntrance(rs.getString("entrance"));
                    address.setFloor(rs.getString("floor"));
                    address.setComment(rs.getString("comment"));
                    addresses.add(address);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке адресов: " + e.getMessage(), e);
        }

        return addresses;
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }
}