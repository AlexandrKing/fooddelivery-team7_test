package com.team7.repository.client;

import com.team7.model.client.Address;
import com.team7.model.client.User;
import com.team7.persistence.AddressJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AddressEntity;
import com.team7.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class ClientAuthRepositoryTest {

  private AddressJpaRepository addressJpaRepository;
  private UserJpaRepository userJpaRepository;
  private ClientAuthRepository repo;

  @BeforeEach
  void setUp() {
    addressJpaRepository = Mockito.mock(AddressJpaRepository.class);
    userJpaRepository = Mockito.mock(UserJpaRepository.class);
    repo = new ClientAuthRepository(addressJpaRepository, userJpaRepository);
  }

  @Test
  void createUserSavesEntityAndReturnsUserWithEmptyAddresses() {
    UserEntity saved = new UserEntity();
    saved.setId(10L);
    saved.setFullName("Alex");
    saved.setEmail("a@test");
    saved.setPhone("+7");
    saved.setPassword("hash");
    given(userJpaRepository.save(Mockito.any(UserEntity.class))).willReturn(saved);

    User u = repo.createUser("Alex", "a@test", "+7", "hash");
    assertEquals(10L, u.getId());
    assertNotNull(u.getAddresses());
    assertEquals(0, u.getAddresses().size());
  }

  @Test
  void findByEmailReturnsNullWhenMissing() {
    given(userJpaRepository.findByEmail("x@test")).willReturn(Optional.empty());
    assertNull(repo.findByEmail("x@test"));
  }

  @Test
  void findByIdLoadsAddresses() {
    UserEntity ue = new UserEntity();
    ue.setId(1L);
    ue.setFullName("A");
    ue.setEmail("a@test");
    ue.setPhone("+7");
    given(userJpaRepository.findById(1L)).willReturn(Optional.of(ue));
    AddressEntity ae = new AddressEntity();
    ae.setId(5L);
    ae.setUserId(1L);
    ae.setLabel("home");
    ae.setAddress("street");
    given(addressJpaRepository.findByUserIdOrderByIdAsc(1L)).willReturn(List.of(ae));

    User u = repo.findById(1L);
    assertNotNull(u);
    assertEquals(1, u.getAddresses().size());
    assertEquals("home", u.getAddresses().get(0).getLabel());
  }

  @Test
  void updateProfileReturns0WhenUserMissing() {
    User u = new User();
    u.setId(99L);
    given(userJpaRepository.findById(99L)).willReturn(Optional.empty());
    assertEquals(0, repo.updateProfile(u));
  }

  @Test
  void updatePasswordReturns0WhenUserMissing() {
    given(userJpaRepository.findById(99L)).willReturn(Optional.empty());
    assertEquals(0, repo.updatePassword(99L, "h"));
  }

  @Test
  void addAddressPersistsAndAssignsId() {
    Address address = new Address();
    address.setLabel("home");
    address.setAddress("street");
    address.setApartment("12");
    AddressEntity saved = new AddressEntity();
    saved.setId(123L);
    given(addressJpaRepository.save(Mockito.any(AddressEntity.class))).willReturn(saved);

    Address result = repo.addAddress(1L, address);
    assertEquals(123L, result.getId());
    verify(addressJpaRepository).save(Mockito.any(AddressEntity.class));
  }
}

