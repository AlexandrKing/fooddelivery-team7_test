package com.team7.service.client;

import com.team7.api.dto.review.CourierReviewDtos;
import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.ReviewJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.OrderEntity;
import com.team7.persistence.entity.ReviewEntity;
import com.team7.persistence.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class CourierReviewService {
  private final ReviewJpaRepository reviewJpaRepository;
  private final OrderJpaRepository orderJpaRepository;
  private final CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final CourierUserJpaRepository courierUserJpaRepository;

  public CourierReviewService(
      ReviewJpaRepository reviewJpaRepository,
      OrderJpaRepository orderJpaRepository,
      CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository,
      UserJpaRepository userJpaRepository,
      CourierUserJpaRepository courierUserJpaRepository
  ) {
    this.reviewJpaRepository = reviewJpaRepository;
    this.orderJpaRepository = orderJpaRepository;
    this.courierAssignedOrderJpaRepository = courierAssignedOrderJpaRepository;
    this.userJpaRepository = userJpaRepository;
    this.courierUserJpaRepository = courierUserJpaRepository;
  }

  @Transactional
  public CourierReviewDtos.CourierReviewResponse createCourierReview(
      Long linkedUserId,
      Long orderId,
      int rating,
      String comment
  ) {
    OrderEntity order = orderJpaRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    if (!linkedUserId.equals(order.getUserId())) {
      throw new IllegalArgumentException("Нельзя оставить отзыв по чужому заказу");
    }
    if (!"DELIVERY".equalsIgnoreCase(trim(order.getDeliveryType()))) {
      throw new IllegalArgumentException("Отзыв на курьера доступен только для доставки");
    }
    if (!"DELIVERED".equalsIgnoreCase(trim(order.getStatus()))) {
      throw new IllegalArgumentException("Отзыв можно оставить только после доставки");
    }
    CourierAssignedOrderEntity assignment = courierAssignedOrderJpaRepository.findByOrderId(orderId)
        .orElseThrow(() -> new IllegalArgumentException("К заказу не назначен курьер"));
    if (reviewJpaRepository.existsByOrderId(orderId)) {
      throw new IllegalArgumentException("Отзыв по этому заказу уже оставлен");
    }

    String trimmedComment = comment == null ? null : comment.trim();
    if (trimmedComment != null && trimmedComment.isEmpty()) {
      trimmedComment = null;
    }

    ReviewEntity entity = new ReviewEntity();
    entity.setOrderId(orderId);
    entity.setUserId(linkedUserId);
    entity.setRestaurantId(order.getRestaurantId());
    entity.setCourierId(assignment.getCourierId());
    entity.setRestaurantRating(null);
    entity.setCourierRating(rating);
    entity.setComment(trimmedComment);
    entity.setCreatedAt(LocalDateTime.now());

    ReviewEntity saved = reviewJpaRepository.save(entity);
    return toResponse(saved);
  }

  public List<CourierReviewDtos.CourierReviewResponse> listMyCourierReviews(Long linkedUserId) {
    return reviewJpaRepository.findByUserIdAndCourierRatingIsNotNullOrderByCreatedAtDesc(linkedUserId).stream()
        .map(this::toResponse)
        .toList();
  }

  public List<CourierReviewDtos.AdminCourierReviewResponse> listAllCourierReviewsForAdmin() {
    return reviewJpaRepository.findByCourierIdIsNotNullOrderByCreatedAtDesc().stream()
        .map(this::toAdminResponse)
        .toList();
  }

  @Transactional
  public void deleteReviewByAdmin(Long reviewId) {
    ReviewEntity r = reviewJpaRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
    if (r.getCourierId() == null) {
      throw new IllegalArgumentException("Это не отзыв на курьера");
    }
    reviewJpaRepository.deleteById(reviewId);
  }

  private CourierReviewDtos.CourierReviewResponse toResponse(ReviewEntity e) {
    return new CourierReviewDtos.CourierReviewResponse(
        e.getId(),
        e.getOrderId(),
        e.getUserId(),
        e.getCourierId(),
        e.getCourierRating(),
        e.getComment(),
        e.getCreatedAt()
    );
  }

  private CourierReviewDtos.AdminCourierReviewResponse toAdminResponse(ReviewEntity e) {
    String userLabel = userJpaRepository.findById(e.getUserId())
        .map(UserEntity::getEmail)
        .orElse("user#" + e.getUserId());
    String courierLabel = courierUserJpaRepository.findById(e.getCourierId())
        .map(c -> c.getFullName() != null && !c.getFullName().isBlank() ? c.getFullName() : c.getUsername())
        .orElse("courier#" + e.getCourierId());
    return new CourierReviewDtos.AdminCourierReviewResponse(
        e.getId(),
        e.getOrderId(),
        e.getUserId(),
        userLabel,
        e.getCourierId(),
        courierLabel,
        e.getCourierRating(),
        e.getComment(),
        e.getCreatedAt()
    );
  }

  private static String trim(String s) {
    return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
  }
}
