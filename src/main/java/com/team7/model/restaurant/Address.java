package com.team7.model.restaurant;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
  private String street;
  private String city;
  private String postalCode;
}
