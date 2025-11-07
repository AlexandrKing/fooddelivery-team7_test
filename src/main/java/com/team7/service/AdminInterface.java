package com.team7.service;
import java.util.List;

public interface AdminInterface {
    List<Courier> getCoriers();

    //List<Client> getClients();

    //List<Restaurant> getRestaurants();

    void block(Long id);

    void unblock(Long id);

    //List<Reviews> getReview();

    void removeReview(Long reviewId);

    void comissionAll(Long orderId);

    void comissionCourier(Long OrderId);
}

