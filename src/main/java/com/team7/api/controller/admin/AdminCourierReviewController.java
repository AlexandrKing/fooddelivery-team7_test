package com.team7.api.controller.admin;

import com.team7.api.dto.review.CourierReviewDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.service.client.CourierReviewService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/courier-reviews")
public class AdminCourierReviewController {
  private final CourierReviewService courierReviewService;

  public AdminCourierReviewController(CourierReviewService courierReviewService) {
    this.courierReviewService = courierReviewService;
  }

  @GetMapping
  public ApiSuccessResponse<List<CourierReviewDtos.AdminCourierReviewResponse>> list() {
    return ApiSuccessResponse.of(courierReviewService.listAllCourierReviewsForAdmin());
  }

  @DeleteMapping("/{id}")
  public ApiSuccessResponse<Map<String, Boolean>> delete(@PathVariable Long id) {
    courierReviewService.deleteReviewByAdmin(id);
    return ApiSuccessResponse.of(Map.of("deleted", true), "Отзыв удалён");
  }
}
