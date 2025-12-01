package com.team7.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    private Long id;
    private String name;
    private Double rating;
    private Integer deliveryTime;
    private Double minOrderAmount;
    private List<String> workingHours;
    private Boolean isActive;
}
