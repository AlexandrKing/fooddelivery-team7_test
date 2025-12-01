package com.team7.service.client;

import com.team7.model.client.Address;
import com.team7.model.client.User;
import com.team7.model.client.UserRole;

public interface AuthService {
    User register(UserRole role, String name, String email, String phone, String password, String confirmPassword);
    User login(String email, String password);
    void logout();
    User getCurrentUser();
    boolean isEmailAvailable(String email);
    boolean isPhoneAvailable(String phone);
    User updateProfile(User updatedUser);
    User addAddress(Long userId, Address address);
    User changePassword(Long userId, String oldPassword, String newPassword);
}
