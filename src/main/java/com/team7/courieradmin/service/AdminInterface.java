package com.team7.courieradmin.service;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class AdminInterface {
    private final List<Courier> couriers;
    private List<Admin> admins;
    private Admin currentAdmin;

    public AdminInterface(List<Courier> couriers) {
        this.couriers = couriers;
        this.admins = new ArrayList<>();
        initializeAdmins();
    }

    private void initializeAdmins() {
        admins.add(new Admin("admin", "admin123", "admin@system.com"));
    }


    public List<Courier> getCouriers() {
        System.out.println("\n=== Список всех курьеров ===");
        if (couriers.isEmpty()) {
            System.out.println("Нет зарегистрированных курьеров.");
        } else {
            for (Courier courier : couriers) {
                System.out.println("ID: " + courier.getId() +
                        " | Курьер: " + courier.getName() +
                        " | Логин: " + courier.getLogin() +
                        " | Email: " + courier.getEmail() +
                        " | Статус: " + courier.getStatus() +
                        " | Активность: " + courier.getActivityStatus());
            }
            System.out.println("Всего курьеров: " + couriers.size());
        }
        return couriers;
    }

    public boolean login(String login, String password) {
        for (Admin admin : admins) {
            if (admin.getLogin().equals(login) && admin.getPassword().equals(password)) {
                currentAdmin = admin;
                System.out.println("Администратор " + login + " успешно вошел в систему");
                return true;
            }
        }
        System.out.println("Неверный логин или пароль администратора");
        return false;
    }

    public void logout() {
        if (currentAdmin != null) {
            System.out.println("Администратор " + currentAdmin.getLogin() + " вышел из системы");
            currentAdmin = null;
        }
    }

    public boolean isLoggedIn() {
        return currentAdmin != null;
    }

    public Admin getCurrentAdmin() {
        return currentAdmin;
    }

    public void loginFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Вход для администратора ===");

        System.out.print("Логин: ");
        String login = scanner.nextLine();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        login(login, password);
    }

    public void block(Long id) {

    }


    public void unblock(Long id) {

    }


    public void removeReview(Long reviewId) {

    }


    public void comissionAll(Long orderId) {

    }


    public void comissionCourier(Long OrderId) {

    }

    //public List<Client> getClients(){

    //}
    //public List<Restaurant> getRestaurants(){

    //}
    //public List<Review> getReviews(){

    //}
}
