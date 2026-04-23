package com.team7.service.admin;

import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.OrderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock
  private AppAccountJpaRepository appAccountJpaRepository;
  @Mock
  private OrderJpaRepository orderJpaRepository;

  private AdminService service;

  @BeforeEach
  void setUp() {
    service = new AdminService(appAccountJpaRepository, orderJpaRepository);
  }

  @Test
  void getAccountsAndOrdersReturnRepositoryData() {
    given(appAccountJpaRepository.findAll()).willReturn(List.of(new AppAccountEntity()));
    given(orderJpaRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(new OrderEntity()));
    assertEquals(1, service.getAccounts().size());
    assertEquals(1, service.getOrders().size());
  }

  @Test
  void setAccountActiveUpdatesStateAndTimestamp() {
    AppAccountEntity acc = new AppAccountEntity();
    acc.setId(7L);
    acc.setIsActive(true);
    given(appAccountJpaRepository.findById(7L)).willReturn(Optional.of(acc));
    given(appAccountJpaRepository.save(any(AppAccountEntity.class))).willAnswer(a -> a.getArgument(0));

    AppAccountEntity updated = service.setAccountActive(7L, false);

    assertEquals(false, updated.getIsActive());
    assertNotNull(updated.getUpdatedAt());
    verify(appAccountJpaRepository).save(acc);
  }

  @Test
  void setAccountActiveThrowsWhenAccountNotFound() {
    given(appAccountJpaRepository.findById(404L)).willReturn(Optional.empty());
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.setAccountActive(404L, true));
    assertEquals("Account not found", ex.getMessage());
  }
}
