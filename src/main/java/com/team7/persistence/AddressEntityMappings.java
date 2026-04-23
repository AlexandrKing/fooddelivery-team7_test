package com.team7.persistence;

import com.team7.model.client.Address;
import com.team7.persistence.entity.AddressEntity;

public final class AddressEntityMappings {

  private AddressEntityMappings() {
  }

  public static Address toDto(AddressEntity e) {
    Address a = new Address();
    a.setId(e.getId());
    a.setLabel(e.getLabel());
    a.setAddress(e.getAddress());
    a.setApartment(e.getApartment());
    return a;
  }
}
