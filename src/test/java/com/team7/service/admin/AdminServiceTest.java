package com.team7.service.admin;

import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.CourierTransactionJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.RestaurantJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.api.dto.admin.AdminDtos;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.OrderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
  @Mock
  private UserJpaRepository userJpaRepository;
  @Mock
  private CourierUserJpaRepository courierUserJpaRepository;
  @Mock
  private RestaurantJpaRepository restaurantJpaRepository;
  @Mock
  private CourierTransactionJpaRepository courierTransactionJpaRepository;

  private AdminService service;

  @BeforeEach
  void setUp() {
    service = new AdminService(
        appAccountJpaRepository,
        orderJpaRepository,
        userJpaRepository,
        courierUserJpaRepository,
        restaurantJpaRepository,
        courierTransactionJpaRepository
    );
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

  @Test
  void getStatsCountsTotalsAndPaidToCouriers() {
    OrderEntity pending = new OrderEntity();
    pending.setStatus("PENDING");
    OrderEntity delivered = new OrderEntity();
    delivered.setStatus("DELIVERED");
    given(userJpaRepository.count()).willReturn(3L);
    given(courierUserJpaRepository.count()).willReturn(2L);
    given(restaurantJpaRepository.count()).willReturn(4L);
    given(orderJpaRepository.findAll()).willReturn(List.of(pending, delivered));
    given(courierTransactionJpaRepository.sumTotalAmount()).willReturn(BigDecimal.valueOf(250.00));

    AdminDtos.AdminStatsResponse stats = service.getStats();

    assertEquals(3L, stats.totalUsers());
    assertEquals(2L, stats.totalCouriers());
    assertEquals(4L, stats.totalRestaurants());
    assertEquals(2L, stats.totalOrders());
    assertEquals(0, BigDecimal.valueOf(250.00).compareTo(stats.totalPaidToCouriers()));
    assertEquals(1L, stats.ordersByStatus().get("PENDING"));
    assertEquals(1L, stats.ordersByStatus().get("DELIVERED"));
  }

  @Test
  void getStatsUsesZeroWhenPaidSumIsNull() {
    given(orderJpaRepository.findAll()).willReturn(List.of());
    given(courierTransactionJpaRepository.sumTotalAmount()).willReturn(null);

    AdminDtos.AdminStatsResponse stats = service.getStats();

    assertEquals(0, BigDecimal.ZERO.compareTo(stats.totalPaidToCouriers()));
  }
}
