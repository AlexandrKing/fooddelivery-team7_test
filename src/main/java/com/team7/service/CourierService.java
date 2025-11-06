package com.team7.service;

public interface CourierService {
    void registerCourier(String login, String password, String name, String email);

    void login(String login, String password);

    void start(String login, String activityStatus);

    void end(String login, String activityStatus);

    void acceptOrder(String login, Long orderId);

    void takeOrder(String login, Long orderId);

    void giveOrder(String login, Long orderId);
}
