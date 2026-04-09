package com.team7.repository.client;

import com.team7.model.client.Address;
import com.team7.model.client.User;
import com.team7.persistence.AddressEntityMappings;
import com.team7.persistence.AddressJpaRepository;
import com.team7.persistence.UserEntityMappings;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AddressEntity;
import com.team7.persistence.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ClientAuthRepository {
  private final AddressJpaRepository addressJpaRepository;
  private final UserJpaRepository userJpaRepository;

  public ClientAuthRepository(
      AddressJpaRepository addressJpaRepository,
      UserJpaRepository userJpaRepository
  ) {
    this.addressJpaRepository = addressJpaRepository;
    this.userJpaRepository = userJpaRepository;
  }

  public User createUser(String name, String email, String phone, String encodedPassword) {
    UserEntity entity = new UserEntity();
    entity.setFullName(name);
    entity.setEmail(email);
    entity.setPassword(encodedPassword);
    entity.setPhone(phone);
    entity.setIsActive(true);
    UserEntity saved = userJpaRepository.save(entity);
    User user = UserEntityMappings.toClientUser(saved);
    user.setAddresses(new ArrayList<>());
    return user;
  }

  public User findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
        .map(e -> {
          User u = UserEntityMappings.toClientUser(e);
          u.setAddresses(getUserAddresses(u.getId()));
          return u;
        })
        .orElse(null);
  }

  public User findById(Long userId) {
    return userJpaRepository.findById(userId)
        .map(e -> {
          User u = UserEntityMappings.toClientUser(e);
          u.setAddresses(getUserAddresses(u.getId()));
          return u;
        })
        .orElse(null);
  }

  public int countByEmail(String email) {
    return (int) userJpaRepository.countByEmail(email);
  }

  public int countByPhone(String phone) {
    return (int) userJpaRepository.countByPhone(phone);
  }

  public int updateProfile(User user) {
    return userJpaRepository.findById(user.getId())
        .map(e -> {
          e.setFullName(user.getName());
          e.setPhone(user.getPhone());
          e.setEmail(user.getEmail());
          userJpaRepository.save(e);
          return 1;
        })
        .orElse(0);
  }

  public Address addAddress(Long userId, Address address) {
    AddressEntity entity = new AddressEntity();
    entity.setUserId(userId);
    entity.setLabel(address.getLabel());
    entity.setAddress(address.getAddress());
    entity.setApartment(address.getApartment());
    AddressEntity saved = addressJpaRepository.save(entity);
    address.setId(saved.getId());
    return address;
  }

  public String findPasswordByUserId(Long userId) {
    return userJpaRepository.findById(userId)
        .map(UserEntity::getPassword)
        .orElse(null);
  }

  public int updatePassword(Long userId, String encodedPassword) {
    return userJpaRepository.findById(userId)
        .map(e -> {
          e.setPassword(encodedPassword);
          userJpaRepository.save(e);
          return 1;
        })
        .orElse(0);
  }

  private List<Address> getUserAddresses(Long userId) {
    return addressJpaRepository.findByUserIdOrderByIdAsc(userId).stream()
        .map(AddressEntityMappings::toDto)
        .collect(Collectors.toList());
  }
}
