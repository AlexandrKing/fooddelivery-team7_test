package com.team7.service.admin;

import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.OrderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {
  private final AppAccountJpaRepository appAccountJpaRepository;
  private final OrderJpaRepository orderJpaRepository;

  public AdminService(AppAccountJpaRepository appAccountJpaRepository, OrderJpaRepository orderJpaRepository) {
    this.appAccountJpaRepository = appAccountJpaRepository;
    this.orderJpaRepository = orderJpaRepository;
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
}

