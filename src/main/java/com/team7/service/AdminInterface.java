package com.team7.service;
import java.util.List;

public interface AdminInterface {
    list<Courier> getCoriers();

    list<Client> getClients();

    list<Restaurant> getRestaurants();

    void block(Long id);

    void unblock(Long id);

    list<Reviews> getReview();

    void removeReview(Long reviewId);

    void comissionAll(Long orderId);

    void comissionCourier(Long OrderId);
}

