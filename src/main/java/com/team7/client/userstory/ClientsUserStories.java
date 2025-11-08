package com.team7.client.userstory;

import com.team7.client.model.User;
import com.team7.client.model.UserRole;
import com.team7.client.model.Address;
import com.team7.client.service.AuthService;
import com.team7.client.service.AuthServiceImpl;

public class ClientsUserStories {

    public static void main(String[] args) {
        checkUserStory1();
        checkUserStory2();
        checkUserStory3();
        checkUserStory4();
        checkUserStory5();
        checkUserStory6();
    }

    public static void checkUserStory1() {
        System.out.println("User Story 1: Регистрация нового пользователя");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.register(
                    UserRole.CLIENT,
                    "ivan@mail.ru",
                    "+79161234567",
                    "password123",
                    "password123"
            );
            System.out.println("Пользователь зарегистрирован: " + user.getEmail());
            System.out.println("ID пользователя: " + user.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
    }

    public static void checkUserStory2() {
        System.out.println("\nUser Story 2: Вход в систему");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.login("ivan@mail.ru", "password123");
            System.out.println("Успешный вход: " + user.getEmail());
            System.out.println("Текущий пользователь: " + authService.getCurrentUser().getEmail());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка входа: " + e.getMessage());
        }
    }

    public static void checkUserStory3() {
        System.out.println("\nUser Story 3: Изменение данных профиля");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.login("ivan@mail.ru", "password123");

            user.setName("Иван Сидоров");
            user.setPhone("+79167778899");
            User updatedUser = authService.updateProfile(user);

            System.out.println("Данные обновлены: " + updatedUser.getName());
            System.out.println("Новый телефон: " + updatedUser.getPhone());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка изменения данных: " + e.getMessage());
        }
    }

    public static void checkUserStory4() {
        System.out.println("\nUser Story 4: Управление адресами доставки");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.login("ivan@mail.ru", "password123");
            Address address = new Address();
            address.setLabel("дом");
            address.setAddress("ул. Ленина, 10");
            address.setApartment("25");

            User userWithAddress = authService.addAddress(user.getId().toString(), address);

            System.out.println("Адрес добавлен: " + address.getAddress());
            System.out.println("Всего адресов: " + userWithAddress.getAddresses().size());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка добавления адреса: " + e.getMessage());
        }
    }

    public static void checkUserStory5() {
        System.out.println("\nUser Story 5: Смена пароля");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.login("ivan@mail.ru", "password123");

            User updatedUser = authService.changePassword(
                    user.getId().toString(),
                    "password123",
                    "newPassword456"
            );

            System.out.println("Пароль успешно изменен");

            authService.logout();
            authService.login("ivan@mail.ru", "newPassword456");
            System.out.println("Вход с новым паролем выполнен успешно");

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка смены пароля: " + e.getMessage());
        }
    }

    public static void checkUserStory6() {
        System.out.println("\nUser Story 6: Валидация данных");

        AuthService authService = new AuthServiceImpl();

        System.out.println("Проверка доступности email 'test@mail.ru': " + authService.isEmailAvailable("test@mail.ru"));
        System.out.println("Проверка доступности телефона '+79161111111': " + authService.isPhoneAvailable("+79161111111"));

        try {
            authService.register(UserRole.CLIENT, "invalid-email", "+79161111111", "pass", "pass");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации email: " + e.getMessage());
        }

        try {
            authService.register(UserRole.CLIENT, "test@mail.ru", "89161111111", "pass", "pass");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации телефона: " + e.getMessage());
        }
    }
}
