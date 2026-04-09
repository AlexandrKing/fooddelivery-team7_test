package com.team7.service.client;

import com.team7.model.client.User;
import com.team7.model.client.Address;
import com.team7.repository.client.ClientAuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {
    private User currentUser;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+79[0-9]{9}$");

    private final ClientAuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(ClientAuthRepository authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(String name, String email, String phone, String password, String confirmPassword) {
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

        ClientAuthRepository repository = requireRepository();
        User created = repository.createUser(name, email, phone, passwordEncoder.encode(password));
        created.setPassword(password);
        if (created.getAddresses() == null) {
            created.setAddresses(new ArrayList<>());
        }
        return created;
    }

    @Override
    public User login(String email, String password) {
        User user = requireRepository().findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Пользователь не найден или неверный пароль");
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
        return requireRepository().countByEmail(email) == 0;
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        return requireRepository().countByPhone(phone) == 0;
    }

    @Override
    public User updateProfile(User updatedUser) {
        int rows = requireRepository().updateProfile(updatedUser);
        if (rows <= 0) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        User refreshed = requireUserById(updatedUser.getId());
        if (currentUser != null && currentUser.getId().equals(updatedUser.getId())) {
            currentUser = refreshed;
        }
        return refreshed;
    }

    @Override
    public User addAddress(Long userId, Address address) {
        requireRepository().addAddress(userId, address);
        User user = requireUserById(userId);
        if (currentUser != null && currentUser.getId().equals(userId)) {
            currentUser = user;
        }
        return user;
    }

    @Override
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        ClientAuthRepository repository = requireRepository();
        String storedPassword = repository.findPasswordByUserId(userId);
        if (storedPassword == null || !passwordEncoder.matches(oldPassword, storedPassword)) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }
        int rows = repository.updatePassword(userId, passwordEncoder.encode(newPassword));
        if (rows <= 0) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        User user = requireUserById(userId);
        if (currentUser != null && currentUser.getId().equals(userId)) {
            currentUser = user;
        }
        return user;
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    private ClientAuthRepository requireRepository() {
        return authRepository;
    }

    private User requireUserById(Long userId) {
        User user = requireRepository().findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        return user;
    }
}