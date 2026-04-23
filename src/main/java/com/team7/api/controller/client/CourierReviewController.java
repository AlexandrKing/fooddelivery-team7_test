package com.team7.api.controller.client;

import com.team7.api.dto.review.CourierReviewDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.persistence.UserJpaRepository;
import com.team7.repository.client.UserSecurityRepository;
import com.team7.service.client.CourierReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/client/courier-reviews")
public class CourierReviewController {
  private final CourierReviewService courierReviewService;
  private final UserSecurityRepository userSecurityRepository;
  private final UserJpaRepository userJpaRepository;

  public CourierReviewController(
      CourierReviewService courierReviewService,
      UserSecurityRepository userSecurityRepository,
      UserJpaRepository userJpaRepository
  ) {
    this.courierReviewService = courierReviewService;
    this.userSecurityRepository = userSecurityRepository;
    this.userJpaRepository = userJpaRepository;
  }

  @PostMapping
  public ApiSuccessResponse<CourierReviewDtos.CourierReviewResponse> create(
      Principal principal,
      @Valid @RequestBody CourierReviewDtos.CreateCourierReviewRequest request
  ) {
    Long userId = requireLinkedUserId(principal);
    CourierReviewDtos.CourierReviewResponse created = courierReviewService.createCourierReview(
        userId,
        request.orderId(),
        request.rating(),
        request.comment()
    );
    return ApiSuccessResponse.of(created);
  }

  @GetMapping("/mine")
  public ApiSuccessResponse<List<CourierReviewDtos.CourierReviewResponse>> mine(Principal principal) {
    Long userId = requireLinkedUserId(principal);
    return ApiSuccessResponse.of(courierReviewService.listMyCourierReviews(userId));
  }

  /**
   * Должен совпадать с логикой входа для USER: при отсутствии linked_user_id в app_accounts
   * пользователь всё ещё может войти по email — отзыв не должен падать только из‑за NULL в колонке.
   */
  private Long requireLinkedUserId(Principal principal) {
    if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
      throw new IllegalArgumentException("Пользователь не авторизован");
    }
    String email = principal.getName();
    UserSecurityRepository.SecurityUserRecord rec = userSecurityRepository.findByEmail(email);
    if (rec == null) {
      throw new IllegalArgumentException("Учётная запись не найдена");
    }
    if (rec.linkedUserId() != null) {
      return rec.linkedUserId();
    }
    if ("USER".equals(rec.role())) {
      return userJpaRepository.findByEmail(email)
          .map(u -> u.getId())
          .orElseThrow(() -> new IllegalArgumentException("Профиль пользователя не найден"));
    }
    throw new IllegalArgumentException("Профиль пользователя не привязан к аккаунту");
  }
}
