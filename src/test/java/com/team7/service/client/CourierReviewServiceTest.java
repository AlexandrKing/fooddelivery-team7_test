package com.team7.service.client;

import com.team7.api.dto.review.CourierReviewDtos;
import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.ReviewJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.CourierUserEntity;
import com.team7.persistence.entity.OrderEntity;
import com.team7.persistence.entity.ReviewEntity;
import com.team7.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourierReviewServiceTest {

  @Mock private ReviewJpaRepository reviewJpaRepository;
  @Mock private OrderJpaRepository orderJpaRepository;
  @Mock private CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;
  @Mock private UserJpaRepository userJpaRepository;
  @Mock private CourierUserJpaRepository courierUserJpaRepository;

  private CourierReviewService service;

  @BeforeEach
  void setUp() {
    service = new CourierReviewService(
        reviewJpaRepository,
        orderJpaRepository,
        courierAssignedOrderJpaRepository,
        userJpaRepository,
        courierUserJpaRepository
    );
  }

  @Test
  void createCourierReviewHappyPathAndCommentTrim() {
    OrderEntity order = deliveredDeliveryOrder(101L, 1L, 3L);
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setOrderId(101L);
    assignment.setCourierId(9L);
    given(orderJpaRepository.findById(101L)).willReturn(Optional.of(order));
    given(courierAssignedOrderJpaRepository.findByOrderId(101L)).willReturn(Optional.of(assignment));
    given(reviewJpaRepository.existsByOrderId(101L)).willReturn(false);
    given(reviewJpaRepository.save(any(ReviewEntity.class))).willAnswer(a -> {
      ReviewEntity e = a.getArgument(0);
      e.setId(500L);
      return e;
    });

    CourierReviewDtos.CourierReviewResponse result = service.createCourierReview(1L, 101L, 5, "  great courier  ");

    assertEquals(500L, result.id());
    assertEquals(9L, result.courierId());
    assertEquals(5, result.rating());
    assertEquals("great courier", result.comment());
  }

  @Test
  void createCourierReviewDomainErrors() {
    given(orderJpaRepository.findById(1L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.createCourierReview(1L, 1L, 5, "x"));

    OrderEntity foreignOrder = deliveredDeliveryOrder(2L, 10L, 3L);
    given(orderJpaRepository.findById(2L)).willReturn(Optional.of(foreignOrder));
    assertThrows(IllegalArgumentException.class, () -> service.createCourierReview(1L, 2L, 5, "x"));

    OrderEntity pickupOrder = deliveredDeliveryOrder(3L, 1L, 3L);
    pickupOrder.setDeliveryType("PICKUP");
    given(orderJpaRepository.findById(3L)).willReturn(Optional.of(pickupOrder));
    assertThrows(IllegalArgumentException.class, () -> service.createCourierReview(1L, 3L, 5, "x"));

    OrderEntity notDelivered = deliveredDeliveryOrder(4L, 1L, 3L);
    notDelivered.setStatus("READY");
    given(orderJpaRepository.findById(4L)).willReturn(Optional.of(notDelivered));
    assertThrows(IllegalArgumentException.class, () -> service.createCourierReview(1L, 4L, 5, "x"));

    OrderEntity validOrder = deliveredDeliveryOrder(5L, 1L, 3L);
    given(orderJpaRepository.findById(5L)).willReturn(Optional.of(validOrder));
    given(courierAssignedOrderJpaRepository.findByOrderId(5L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.createCourierReview(1L, 5L, 5, "x"));

    OrderEntity duplicate = deliveredDeliveryOrder(6L, 1L, 3L);
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setOrderId(6L);
    assignment.setCourierId(9L);
    given(orderJpaRepository.findById(6L)).willReturn(Optional.of(duplicate));
    given(courierAssignedOrderJpaRepository.findByOrderId(6L)).willReturn(Optional.of(assignment));
    given(reviewJpaRepository.existsByOrderId(6L)).willReturn(true);
    assertThrows(IllegalArgumentException.class, () -> service.createCourierReview(1L, 6L, 5, "x"));
  }

  @Test
  void createCourierReviewNormalizesBlankCommentToNull() {
    OrderEntity order = deliveredDeliveryOrder(7L, 1L, 3L);
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setOrderId(7L);
    assignment.setCourierId(9L);
    given(orderJpaRepository.findById(7L)).willReturn(Optional.of(order));
    given(courierAssignedOrderJpaRepository.findByOrderId(7L)).willReturn(Optional.of(assignment));
    given(reviewJpaRepository.existsByOrderId(7L)).willReturn(false);
    given(reviewJpaRepository.save(any(ReviewEntity.class))).willAnswer(a -> {
      ReviewEntity e = a.getArgument(0);
      e.setId(700L);
      return e;
    });

    service.createCourierReview(1L, 7L, 4, "   ");

    ArgumentCaptor<ReviewEntity> captor = ArgumentCaptor.forClass(ReviewEntity.class);
    verify(reviewJpaRepository).save(captor.capture());
    assertNull(captor.getValue().getComment());
  }

  @Test
  void listMineAndAdminViewsReturnMappedData() {
    ReviewEntity mine = new ReviewEntity();
    mine.setId(1L);
    mine.setOrderId(10L);
    mine.setUserId(1L);
    mine.setCourierId(9L);
    mine.setCourierRating(5);
    mine.setComment("ok");
    mine.setCreatedAt(LocalDateTime.now());
    given(reviewJpaRepository.findByUserIdAndCourierRatingIsNotNullOrderByCreatedAtDesc(1L)).willReturn(List.of(mine));
    List<CourierReviewDtos.CourierReviewResponse> myList = service.listMyCourierReviews(1L);
    assertEquals(1, myList.size());

    ReviewEntity adminEntity = new ReviewEntity();
    adminEntity.setId(2L);
    adminEntity.setOrderId(11L);
    adminEntity.setUserId(1L);
    adminEntity.setCourierId(9L);
    adminEntity.setCourierRating(4);
    adminEntity.setComment("good");
    adminEntity.setCreatedAt(LocalDateTime.now());
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setEmail("user@test.local");
    CourierUserEntity courier = new CourierUserEntity();
    courier.setId(9L);
    courier.setFullName("Courier Name");
    given(reviewJpaRepository.findByCourierIdIsNotNullOrderByCreatedAtDesc()).willReturn(List.of(adminEntity));
    given(userJpaRepository.findById(1L)).willReturn(Optional.of(user));
    given(courierUserJpaRepository.findById(9L)).willReturn(Optional.of(courier));
    List<CourierReviewDtos.AdminCourierReviewResponse> all = service.listAllCourierReviewsForAdmin();
    assertEquals(1, all.size());
    assertEquals("user@test.local", all.get(0).userLabel());
    assertEquals("Courier Name", all.get(0).courierLabel());
  }

  @Test
  void deleteByAdminCoversHappyNotFoundAndWrongType() {
    ReviewEntity courierReview = new ReviewEntity();
    courierReview.setId(10L);
    courierReview.setCourierId(5L);
    given(reviewJpaRepository.findById(10L)).willReturn(Optional.of(courierReview));
    service.deleteReviewByAdmin(10L);
    verify(reviewJpaRepository).deleteById(10L);

    given(reviewJpaRepository.findById(404L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.deleteReviewByAdmin(404L));

    ReviewEntity nonCourierReview = new ReviewEntity();
    nonCourierReview.setId(11L);
    nonCourierReview.setCourierId(null);
    given(reviewJpaRepository.findById(11L)).willReturn(Optional.of(nonCourierReview));
    assertThrows(IllegalArgumentException.class, () -> service.deleteReviewByAdmin(11L));
  }

  private static OrderEntity deliveredDeliveryOrder(Long orderId, Long userId, Long restaurantId) {
    OrderEntity order = new OrderEntity();
    order.setId(orderId);
    order.setUserId(userId);
    order.setRestaurantId(restaurantId);
    order.setDeliveryType("DELIVERY");
    order.setStatus("DELIVERED");
    return order;
  }
}
