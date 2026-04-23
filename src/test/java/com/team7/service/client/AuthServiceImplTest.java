package com.team7.service.client;

import com.team7.model.client.Address;
import com.team7.model.client.User;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.repository.client.ClientAuthRepository;
import com.team7.service.telegramnotificationservice.TelegramNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock
  private ClientAuthRepository authRepository;
  @Mock
  private AppAccountJpaRepository appAccountJpaRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private TelegramNotificationService telegramNotificationService;

  private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    authService = new AuthServiceImpl(
        authRepository,
        appAccountJpaRepository,
        passwordEncoder,
        telegramNotificationService
    );
  }

  @Test
  void registerCreatesUserAndAccountWhenEmailNotExists() {
    User created = user(10L, "Alex", "alex@test.local", "+79990000001", "encoded");
    created.setAddresses(null);
    given(passwordEncoder.encode("secret123")).willReturn("encoded");
    given(authRepository.createUser("Alex", "alex@test.local", "+79990000001", "encoded")).willReturn(created);
    given(appAccountJpaRepository.findByEmail("alex@test.local")).willReturn(Optional.empty());

    User result = authService.register("Alex", "alex@test.local", "+79990000001", "secret123", "secret123");

    assertEquals(10L, result.getId());
    assertEquals("secret123", result.getPassword());
    assertNotNull(result.getAddresses());
    ArgumentCaptor<AppAccountEntity> accountCaptor = ArgumentCaptor.forClass(AppAccountEntity.class);
    verify(appAccountJpaRepository).save(accountCaptor.capture());
    AppAccountEntity saved = accountCaptor.getValue();
    assertEquals("alex@test.local", saved.getEmail());
    assertEquals(10L, saved.getLinkedUserId());
  }

  @Test
  void registerDoesNotCreateAccountWhenAlreadyExists() {
    User created = user(10L, "Alex", "alex@test.local", "+79990000001", "encoded");
    given(passwordEncoder.encode("secret123")).willReturn("encoded");
    given(authRepository.createUser("Alex", "alex@test.local", "+79990000001", "encoded")).willReturn(created);
    given(appAccountJpaRepository.findByEmail("alex@test.local")).willReturn(Optional.of(new AppAccountEntity()));

    authService.register("Alex", "alex@test.local", "+79990000001", "secret123", "secret123");

    verify(appAccountJpaRepository, never()).save(any(AppAccountEntity.class));
  }

  @Test
  void registerThrowsWhenPasswordsMismatch() {
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> authService.register("Alex", "alex@test.local", "+79990000001", "secret123", "wrong")
    );
    assertEquals("Пароли не совпадают", ex.getMessage());
  }

  @Test
  void loginStoresCurrentUserOnSuccess() {
    User user = user(1L, "Alex", "alex@test.local", "+79990000001", "encoded");
    given(authRepository.findByEmail("alex@test.local")).willReturn(user);
    given(passwordEncoder.matches("secret", "encoded")).willReturn(true);

    User result = authService.login("alex@test.local", "secret");

    assertEquals(1L, result.getId());
    assertEquals(1L, authService.getCurrentUser().getId());
  }

  @Test
  void loginThrowsOnUnknownUserOrPasswordMismatch() {
    given(authRepository.findByEmail("alex@test.local")).willReturn(null);
    assertThrows(IllegalArgumentException.class, () -> authService.login("alex@test.local", "secret"));

    User user = user(1L, "Alex", "alex@test.local", "+79990000001", "encoded");
    given(authRepository.findByEmail("alex@test.local")).willReturn(user);
    given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);
    assertThrows(IllegalArgumentException.class, () -> authService.login("alex@test.local", "wrong"));
  }

  @Test
  void logoutClearsCurrentUser() {
    User user = user(1L, "Alex", "alex@test.local", "+79990000001", "encoded");
    given(authRepository.findByEmail("alex@test.local")).willReturn(user);
    given(passwordEncoder.matches("secret", "encoded")).willReturn(true);
    authService.login("alex@test.local", "secret");

    authService.logout();

    assertNull(authService.getCurrentUser());
  }

  @Test
  void availabilityChecksDelegateToRepository() {
    given(authRepository.countByEmail("a@test.local")).willReturn(0);
    given(authRepository.countByPhone("+79990000001")).willReturn(1);

    assertTrue(authService.isEmailAvailable("a@test.local"));
    assertEquals(false, authService.isPhoneAvailable("+79990000001"));
  }

  @Test
  void updateProfileReturnsRefreshedUserAndUpdatesCurrentUser() {
    User current = user(5L, "Old", "old@test.local", "+79990000001", "encoded");
    given(authRepository.findByEmail("old@test.local")).willReturn(current);
    given(passwordEncoder.matches("secret", "encoded")).willReturn(true);
    authService.login("old@test.local", "secret");

    User updated = user(5L, "New", "new@test.local", "+79990000002", "encoded");
    given(authRepository.updateProfile(updated)).willReturn(1);
    given(authRepository.findById(5L)).willReturn(updated);

    User result = authService.updateProfile(updated);

    assertEquals("New", result.getName());
    assertEquals("new@test.local", authService.getCurrentUser().getEmail());
  }

  @Test
  void updateProfileThrowsWhenNoRowsUpdated() {
    User updated = user(8L, "N", "n@test.local", "+79990000002", "encoded");
    given(authRepository.updateProfile(updated)).willReturn(0);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(updated));

    assertEquals("Пользователь не найден", ex.getMessage());
  }

  @Test
  void addAddressReturnsUserAndRefreshesCurrentUser() {
    User current = user(5L, "Alex", "alex@test.local", "+79990000001", "encoded");
    given(authRepository.findByEmail("alex@test.local")).willReturn(current);
    given(passwordEncoder.matches("secret", "encoded")).willReturn(true);
    authService.login("alex@test.local", "secret");

    Address address = new Address();
    address.setLabel("home");
    address.setAddress("street");
    User refreshed = user(5L, "Alex", "alex@test.local", "+79990000001", "encoded");
    refreshed.setAddresses(new ArrayList<>());
    refreshed.getAddresses().add(address);
    given(authRepository.findById(5L)).willReturn(refreshed);

    User result = authService.addAddress(5L, address);

    assertEquals(1, result.getAddresses().size());
    assertEquals(1, authService.getCurrentUser().getAddresses().size());
  }

  @Test
  void changePasswordUpdatesPasswordAndReturnsUser() {
    given(authRepository.findPasswordByUserId(5L)).willReturn("old-hash");
    given(passwordEncoder.matches("old123", "old-hash")).willReturn(true);
    given(passwordEncoder.encode("new123")).willReturn("new-hash");
    given(authRepository.updatePassword(5L, "new-hash")).willReturn(1);
    User refreshed = user(5L, "Alex", "alex@test.local", "+79990000001", "new-hash");
    given(authRepository.findById(5L)).willReturn(refreshed);

    User result = authService.changePassword(5L, "old123", "new123");

    assertEquals(5L, result.getId());
    verify(authRepository).updatePassword(5L, "new-hash");
  }

  @Test
  void changePasswordThrowsOnWrongCurrentPassword() {
    given(authRepository.findPasswordByUserId(5L)).willReturn("old-hash");
    given(passwordEncoder.matches("wrong", "old-hash")).willReturn(false);

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> authService.changePassword(5L, "wrong", "new123")
    );

    assertEquals("Неверный текущий пароль", ex.getMessage());
  }

  @Test
  void changePasswordThrowsWhenUserWasNotUpdated() {
    given(authRepository.findPasswordByUserId(5L)).willReturn("old-hash");
    given(passwordEncoder.matches("old123", "old-hash")).willReturn(true);
    given(passwordEncoder.encode("new123")).willReturn("new-hash");
    given(authRepository.updatePassword(5L, "new-hash")).willReturn(0);

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> authService.changePassword(5L, "old123", "new123")
    );

    assertEquals("Пользователь не найден", ex.getMessage());
  }

  @Test
  void initiateAuthCodeSendingDelegatesToTelegramService() {
    given(telegramNotificationService.sendAuthCode("chat-1", "user-7")).willReturn("123456");

    String code = authService.initiateAuthCodeSending("chat-1", "user-7");

    assertEquals("123456", code);
    verify(telegramNotificationService).sendAuthCode(eq("chat-1"), eq("user-7"));
  }

  @Test
  void updateProfileThrowsWhenRefreshedUserMissing() {
    User updated = user(8L, "N", "n@test.local", "+79990000002", "encoded");
    given(authRepository.updateProfile(updated)).willReturn(1);
    given(authRepository.findById(8L)).willReturn(null);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(updated));

    assertEquals("Пользователь не найден", ex.getMessage());
  }

  private static User user(Long id, String name, String email, String phone, String password) {
    User user = new User();
    user.setId(id);
    user.setName(name);
    user.setEmail(email);
    user.setPhone(phone);
    user.setPassword(password);
    user.setAddresses(new ArrayList<>());
    return user;
  }
}
