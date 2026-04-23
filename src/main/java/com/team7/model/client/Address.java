package com.team7.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
  private Long id;
  private String label;
  private String address;
  private Double latitude;
  private Double longitude;
  private String apartment;
  private String entrance;
  private String floor;
  private String comment;
}