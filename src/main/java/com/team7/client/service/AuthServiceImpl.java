package com.team7.client.service;

import com.team7.client.model.User;
import com.team7.client.model.UserRole;
import com.team7.client.model.Address;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AuthServiceImpl implements AuthService {
    private static final List<User> USERS = new ArrayList<>();
    private User currentUser;

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

        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setRole(role);
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);

        USERS.add(user);
        return user;
    }

    @Override
    public User login(String email, String password) {
        User user = USERS.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        currentUser = user;
        return user;
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
        return USERS.stream().noneMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        return USERS.stream().noneMatch(user -> user.getPhone().equals(phone));
    }

    @Override
    public User updateProfile(User updatedUser) {
        User existingUser = USERS.stream()
                .filter(u -> u.getId().equals(updatedUser.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        boolean phoneExists = USERS.stream()
                .filter(u -> !u.getId().equals(updatedUser.getId()))
                .anyMatch(u -> u.getPhone().equals(updatedUser.getPhone()));

        if (phoneExists) {
            throw new IllegalArgumentException("Номер телефона уже используется");
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setEmail(updatedUser.getEmail());

        if (currentUser != null && currentUser.getId().equals(updatedUser.getId())) {
            currentUser = existingUser;
        }
        return existingUser;
    }

    @Override
    public User addAddress(Long userId, Address address) {
        User user = USERS.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        address.setId(System.currentTimeMillis());
        user.getAddresses().add(address);
        return user;
    }

    @Override
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        User user = USERS.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (!user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }

        user.setPassword(newPassword);
        return user;
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }
}