package com.team7.api.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponsesTest {

  @Test
  void successResponseFactoriesSetSuccessAndTimestamp() {
    ApiSuccessResponse<String> r1 = ApiSuccessResponse.of("data");
    assertTrue(r1.success());
    assertEquals("data", r1.data());
    assertNull(r1.message());
    assertNotNull(r1.timestamp());

    ApiSuccessResponse<Integer> r2 = ApiSuccessResponse.of(10, "ok");
    assertTrue(r2.success());
    assertEquals(10, r2.data());
    assertEquals("ok", r2.message());
    assertNotNull(r2.timestamp());
  }

  @Test
  void errorResponseFactoriesSetFailureAndDetails() {
    ApiErrorResponse e1 = ApiErrorResponse.of(400, "Bad Request", "msg");
    assertFalse(e1.success());
    assertEquals(400, e1.status());
    assertEquals("Bad Request", e1.error());
    assertEquals("msg", e1.message());
    assertNotNull(e1.timestamp());
    assertNotNull(e1.details());
    assertEquals(0, e1.details().size());

    ApiErrorResponse e2 = ApiErrorResponse.of(400, "Validation Error", "failed", List.of("a", "b"));
    assertEquals(List.of("a", "b"), e2.details());
  }
}

