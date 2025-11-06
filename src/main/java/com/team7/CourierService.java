package com.team7;

public interface CourierService {
    void registerDeliver(String login, String password, String name);

    void start(String login, String activityStatus);

    void end(String login, String activityStatus);

    void acceptOrder(String login, Long orderId);

    void takeOrder(String login, Long orderId);

    void giveOrder(String login, Long orderId);
}
