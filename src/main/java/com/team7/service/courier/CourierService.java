package com.team7.service.courier;

import com.team7.config.AppProperties;
import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.CourierTransactionJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.CourierTransactionEntity;
import com.team7.persistence.entity.CourierUserEntity;
import com.team7.persistence.entity.OrderEntity;
import com.team7.service.order.OrderStatusTransitionPolicy;
import com.team7.service.telegramnotificationservice.OrderNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class CourierService {
    private final CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final CourierUserJpaRepository courierUserJpaRepository;
    private final CourierTransactionJpaRepository courierTransactionJpaRepository;
    private final AppProperties appProperties;
    private final OrderNotificationService orderNotificationService;

    public CourierService(
            CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository,
            OrderJpaRepository orderJpaRepository,
            CourierUserJpaRepository courierUserJpaRepository,
            CourierTransactionJpaRepository courierTransactionJpaRepository,
            AppProperties appProperties,
            OrderNotificationService orderNotificationService
    ) {
        this.courierAssignedOrderJpaRepository = courierAssignedOrderJpaRepository;
        this.orderJpaRepository = orderJpaRepository;
        this.courierUserJpaRepository = courierUserJpaRepository;
        this.courierTransactionJpaRepository = courierTransactionJpaRepository;
        this.appProperties = appProperties;
        this.orderNotificationService = orderNotificationService;
    }

    public List<CourierAssignedOrderEntity> getAssignedOrders(Long courierId) {
        return courierAssignedOrderJpaRepository.findByCourierIdOrderByAssignedAtDesc(courierId);
    }

    public List<OrderEntity> getAvailableDeliveryOrders() {
        return orderJpaRepository.findAvailableForCourierAssignment();
    }

    public BigDecimal getBalance(Long courierId) {
        return courierUserJpaRepository.findById(courierId)
                .map(CourierUserEntity::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    public List<CourierTransactionEntity> getTransactions(Long courierId) {
        return courierTransactionJpaRepository.findByCourierIdOrderByCreatedAtDesc(courierId);
    }

    public Page<CourierTransactionEntity> getTransactions(Long courierId, Pageable pageable) {
        return courierTransactionJpaRepository.findByCourierIdOrderByCreatedAtDesc(courierId, pageable);
    }

    public CourierStats getStats(Long courierId) {
        BigDecimal balance = getBalance(courierId);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        BigDecimal earnedToday = courierTransactionJpaRepository.sumAmountByCourierIdSince(courierId, todayStart);
        BigDecimal earnedThisWeek = courierTransactionJpaRepository.sumAmountByCourierIdSince(courierId, weekStart);
        return new CourierStats(
                balance,
                earnedToday == null ? BigDecimal.ZERO : earnedToday,
                earnedThisWeek == null ? BigDecimal.ZERO : earnedThisWeek
        );
    }

    @Transactional
    public CourierAssignedOrderEntity claimOrder(Long courierId, Long orderId) {
        OrderEntity order = orderJpaRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        if (!"DELIVERY".equalsIgnoreCase(trimUpper(order.getDeliveryType()))) {
            throw new IllegalArgumentException("Заказ не на доставку");
        }

        if (!OrderStatusTransitionPolicy.canClaimFromOrderStatus(order.getStatus())) {
            throw new IllegalArgumentException("Заказ недоступен для назначения");
        }

        if (courierAssignedOrderJpaRepository.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("Заказ уже назначен курьеру");
        }

        CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
        assignment.setCourierId(courierId);
        assignment.setOrderId(orderId);
        assignment.setStatus("ASSIGNED");
        assignment.setAssignedAt(LocalDateTime.now());

        CourierAssignedOrderEntity saved = courierAssignedOrderJpaRepository.save(assignment);

        orderNotificationService.sendOrderAcceptedForDelivery(
                order.getUserId(),
                orderId
        );

        return saved;
    }

    private static String trimUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }

    @Transactional
    public CourierAssignedOrderEntity updateAssignedOrderStatus(Long courierId, Long orderId, String status) {
        CourierAssignedOrderEntity assignment = courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(courierId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Assigned order not found"));

        String previousStatus = trimUpper(assignment.getStatus());
        String requestedStatus = trimUpper(status);

        if ("DELIVERED".equals(previousStatus) && "DELIVERED".equals(requestedStatus)) {
            return assignment;
        }

        String nextStatus = OrderStatusTransitionPolicy.validateCourierTransition(assignment.getStatus(), status);
        assignment.setStatus(nextStatus);

        if ("PICKED_UP".equals(nextStatus)) {
            assignment.setPickedUpAt(LocalDateTime.now());
        }

        if ("DELIVERED".equals(nextStatus)) {
            assignment.setDeliveryTime(LocalDateTime.now());
        }

        CourierAssignedOrderEntity saved = courierAssignedOrderJpaRepository.save(assignment);

        OrderEntity order = orderJpaRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setStatus(nextStatus);
        orderJpaRepository.save(order);

        if (!"DELIVERED".equals(previousStatus) && "DELIVERED".equals(nextStatus)) {
            accrueDeliveryFee(courierId, orderId);

            orderNotificationService.sendOrderDelivered(
                    order.getUserId(),
                    orderId
            );
        }

        return saved;
    }

    private void accrueDeliveryFee(Long courierId, Long orderId) {
        if (courierTransactionJpaRepository.existsByOrderId(orderId)) {
            return;
        }

        CourierUserEntity courier = courierUserJpaRepository.findById(courierId)
                .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        BigDecimal currentBalance = courier.getBalance() == null ? BigDecimal.ZERO : courier.getBalance();
        BigDecimal deliveryFee = appProperties.getDeliveryFee();

        courier.setBalance(currentBalance.add(deliveryFee));
        courierUserJpaRepository.save(courier);

        CourierTransactionEntity transaction = new CourierTransactionEntity();
        transaction.setCourierId(courierId);
        transaction.setOrderId(orderId);
        transaction.setAmount(deliveryFee);
        transaction.setType("DELIVERY_FEE");
        transaction.setCreatedAt(LocalDateTime.now());

        courierTransactionJpaRepository.save(transaction);
    }

    public record CourierStats(BigDecimal balance, BigDecimal earnedToday, BigDecimal earnedThisWeek) {
    }
}