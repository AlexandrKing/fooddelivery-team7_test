package com.team7.service.courieradmin;

import com.team7.model.admin.Admin;
import com.team7.service.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    public Admin login(String username, String password) {
        String sql = "SELECT * FROM admin_users WHERE username = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");

                // ПРЯМОЕ СРАВНЕНИЕ ПАРОЛЕЙ (без BCrypt)
                if (password.equals(storedPassword)) {
                    Admin admin = mapResultSetToAdmin(rs);
                    updateLastLogin(admin.getId());
                    return admin;
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при входе администратора: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private void updateLastLogin(Long adminId) {
        String sql = "UPDATE admin_users SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Ошибка обновления времени входа: " + e.getMessage());
        }
    }

    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admin_users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                admins.add(mapResultSetToAdmin(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения администраторов: " + e.getMessage());
        }

        return admins;
    }

    public Admin getAdminById(Long id) {
        String sql = "SELECT * FROM admin_users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAdmin(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения администратора: " + e.getMessage());
        }

        return null;
    }

    public boolean changePassword(Long adminId, String currentPassword, String newPassword) {
        Admin admin = getAdminById(adminId);
        if (admin == null) return false;

        String sql = "SELECT password_hash FROM admin_users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");

                // ПРЯМОЕ СРАВНЕНИЕ (без BCrypt)
                if (currentPassword.equals(storedPassword)) {
                    // Сохраняем новый пароль как есть (без хэширования)
                    String updateSql = "UPDATE admin_users SET password_hash = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, newPassword);
                        updateStmt.setLong(2, adminId);
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка смены пароля: " + e.getMessage());
        }

        return false;
    }

    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setId(rs.getLong("id"));
        admin.setUsername(rs.getString("username"));
        admin.setEmail(rs.getString("email"));
        admin.setPasswordHash(rs.getString("password_hash"));
        admin.setFullName(rs.getString("full_name"));
        admin.setRole(rs.getString("role"));
        admin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        admin.setLastLoginAt(rs.getTimestamp("last_login_at") != null ?
                rs.getTimestamp("last_login_at").toLocalDateTime() : null);
        admin.setIsActive(rs.getBoolean("is_active"));
        admin.setPermissions(rs.getString("permissions"));
        return admin;
    }
}