package com.team7.model.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private Long id;
    private String label;
    private String address;
    private String apartment;
    private String entrance;
    private String floor;
    private String comment;
}