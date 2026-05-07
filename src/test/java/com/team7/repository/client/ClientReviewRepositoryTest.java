package com.team7.repository.client;

import com.team7.model.client.Review;
import com.team7.persistence.ReviewJpaRepository;
import com.team7.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class ClientReviewRepositoryTest {

  private ReviewJpaRepository reviewJpaRepository;
  private ClientReviewRepository repo;

  @BeforeEach
  void setUp() {
    reviewJpaRepository = Mockito.mock(ReviewJpaRepository.class);
    repo = new ClientReviewRepository(reviewJpaRepository);
  }

  @Test
  void createReviewPersistsEntityAndMapsDto() {
    ReviewEntity saved = new ReviewEntity();
    saved.setId(1L);
    saved.setOrderId(10L);
    saved.setUserId(1L);
    saved.setRestaurantId(1L);
    saved.setCourierId(1L);
    saved.setRestaurantRating(5);
    saved.setCourierRating(4);
    saved.setComment("ok");
    saved.setCreatedAt(LocalDateTime.now());
    given(reviewJpaRepository.save(Mockito.any(ReviewEntity.class))).willReturn(saved);

    Review r = repo.createReview(10L, 5, 4, "ok");
    assertEquals(1L, r.getId());

    ArgumentCaptor<ReviewEntity> captor = ArgumentCaptor.forClass(ReviewEntity.class);
    verify(reviewJpaRepository).save(captor.capture());
    assertEquals(10L, captor.getValue().getOrderId());
    assertEquals("ok", captor.getValue().getComment());
  }

  @Test
  void ratingsReturnZeroWhenRepositoryReturnsNull() {
    given(reviewJpaRepository.averageRestaurantRatingByRestaurantId(1L)).willReturn(null);
    given(reviewJpaRepository.averageCourierRatingByCourierId(1L)).willReturn(null);
    assertEquals(0.0, repo.getRestaurantRating(1L));
    assertEquals(0.0, repo.getCourierRating(1L));
  }

  @Test
  void getReviewsMapsEntities() {
    ReviewEntity e = new ReviewEntity();
    e.setId(1L);
    e.setOrderId(2L);
    e.setUserId(3L);
    e.setRestaurantId(4L);
    e.setCourierId(5L);
    e.setCreatedAt(LocalDateTime.now());
    given(reviewJpaRepository.findByUserId(3L)).willReturn(List.of(e));
    assertEquals(1, repo.getReviews(3L).size());
  }
}

