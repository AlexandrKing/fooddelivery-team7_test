package com.team7.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LegacyRuntimeGuardTest {

  @AfterEach
  void cleanup() {
    System.clearProperty("legacy.console.enabled");
  }

  @Test
  void throwsWhenNotEnabled() {
    IllegalStateException ex = assertThrows(IllegalStateException.class, LegacyRuntimeGuard::requireLegacyConsoleEnabled);
    // message is stable and used for guidance
    org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("Legacy console mode is disabled"));
  }

  @Test
  void doesNotThrowWhenEnabledViaSystemProperty() {
    System.setProperty("legacy.console.enabled", "true");
    assertDoesNotThrow(LegacyRuntimeGuard::requireLegacyConsoleEnabled);
  }
}

