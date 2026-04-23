package com.team7.persistence;

import com.team7.model.client.User;
import com.team7.persistence.entity.UserEntity;

public final class UserEntityMappings {

  private UserEntityMappings() {
  }

  public static User toClientUser(UserEntity e) {
    User user = new User();
    user.setId(e.getId());
    user.setName(e.getFullName());
    user.setEmail(e.getEmail());
    user.setPhone(e.getPhone());
    user.setPassword(e.getPassword());
    user.setRole(null);
    return user;
  }
}
