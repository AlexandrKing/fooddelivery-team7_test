package com.team7.api.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dummy")
@Validated
class TestValidationController {

  @PostMapping
  Map<String, Object> post(@Valid @RequestBody Payload payload) {
    return Map.of("ok", true);
  }

  @GetMapping("/query")
  Map<String, Object> query(@RequestParam @Min(1) int value) {
    return Map.of("value", value);
  }

  @GetMapping("/illegal")
  Map<String, Object> illegal() {
    throw new IllegalArgumentException("bad");
  }

  @GetMapping("/runtime")
  Map<String, Object> runtime() {
    throw new RuntimeException("boom");
  }

  @GetMapping("/checked-exception")
  Map<String, Object> checkedException() throws Exception {
    throw new Exception("boomAny");
  }

  record Payload(@NotNull @NotBlank String name) {
  }
}

