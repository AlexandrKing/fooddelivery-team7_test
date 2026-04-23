package com.team7.service.client;

import com.team7.model.client.Review;
import com.team7.repository.client.ClientReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

  @Mock
  private ClientReviewRepository reviewRepository;

  private ReviewServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new ReviewServiceImpl(reviewRepository);
  }

  @Test
  void createReviewValidatesRatingsAndDelegates() {
    Review saved = new Review();
    saved.setId(1L);
    given(reviewRepository.createReview(100L, 5, 4, "ok")).willReturn(saved);

    Review result = service.createReview(100L, 5, 4, "ok");

    assertEquals(1L, result.getId());
    verify(reviewRepository).createReview(100L, 5, 4, "ok");
  }

  @Test
  void createReviewThrowsOnInvalidRatings() {
    assertThrows(IllegalArgumentException.class, () -> service.createReview(1L, 0, 5, "x"));
    assertThrows(IllegalArgumentException.class, () -> service.createReview(1L, 5, 6, "x"));
  }

  @Test
  void listAndAggregateRatingsDelegateToRepository() {
    given(reviewRepository.getReviews(1L)).willReturn(List.of(new Review()));
    given(reviewRepository.getRestaurantRating(3L)).willReturn(4.5);
    given(reviewRepository.getCourierRating(9L)).willReturn(4.8);

    assertEquals(1, service.getReviews(1L).size());
    assertEquals(4.5, service.getRestaurantRating(3L));
    assertEquals(4.8, service.getCourierRating(9L));
  }
}
