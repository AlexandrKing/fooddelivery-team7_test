package com.team7.service.admin;

import com.team7.api.dto.admin.AdminDtos;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.CourierTransactionJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.RestaurantJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.OrderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {
  private final AppAccountJpaRepository appAccountJpaRepository;
  private final OrderJpaRepository orderJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final CourierUserJpaRepository courierUserJpaRepository;
  private final RestaurantJpaRepository restaurantJpaRepository;
  private final CourierTransactionJpaRepository courierTransactionJpaRepository;

  public AdminService(
      AppAccountJpaRepository appAccountJpaRepository,
      OrderJpaRepository orderJpaRepository,
      UserJpaRepository userJpaRepository,
      CourierUserJpaRepository courierUserJpaRepository,
      RestaurantJpaRepository restaurantJpaRepository,
      CourierTransactionJpaRepository courierTransactionJpaRepository
  ) {
    this.appAccountJpaRepository = appAccountJpaRepository;
    this.orderJpaRepository = orderJpaRepository;
    this.userJpaRepository = userJpaRepository;
    this.courierUserJpaRepository = courierUserJpaRepository;
    this.restaurantJpaRepository = restaurantJpaRepository;
    this.courierTransactionJpaRepository = courierTransactionJpaRepository;
  }

  public List<AppAccountEntity> getAccounts() {
    return appAccountJpaRepository.findAll();
  }

  @Transactional
  public AppAccountEntity setAccountActive(Long accountId, boolean active) {
    AppAccountEntity account = appAccountJpaRepository.findById(accountId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    account.setIsActive(active);
    account.setUpdatedAt(LocalDateTime.now());
    return appAccountJpaRepository.save(account);
  }

  public List<OrderEntity> getOrders() {
    return orderJpaRepository.findAllByOrderByCreatedAtDesc();
  }

  public AdminDtos.AdminStatsResponse getStats() {
    List<OrderEntity> orders = orderJpaRepository.findAll();
    Map<String, Long> ordersByStatus = orders.stream()
        .collect(Collectors.groupingBy(
            o -> o.getStatus() == null || o.getStatus().isBlank() ? "UNKNOWN" : o.getStatus(),
            Collectors.counting()
        ));
    BigDecimal totalPaid = courierTransactionJpaRepository.sumTotalAmount();
    return new AdminDtos.AdminStatsResponse(
        userJpaRepository.count(),
        courierUserJpaRepository.count(),
        restaurantJpaRepository.count(),
        orders.size(),
        totalPaid == null ? BigDecimal.ZERO : totalPaid,
        ordersByStatus
    );
  }
}

