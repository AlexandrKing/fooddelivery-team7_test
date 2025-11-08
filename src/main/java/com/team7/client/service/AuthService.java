package com.team7.client.service;

import com.team7.client.model.Address;
import com.team7.client.model.User;
import com.team7.client.model.UserRole;

public interface AuthService {
    User register(UserRole role, String email, String phone, String password, String confirmPassword);
    User login(String email, String password);
    void logout();
    User getCurrentUser();
    boolean isEmailAvailable(String email);
    boolean isPhoneAvailable(String phone);
    User updateProfile(User updatedUser);
    User addAddress(String userId, Address address);
    User changePassword(String userId, String oldPassword, String newPassword);
}
