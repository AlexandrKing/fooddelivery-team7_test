package com.team7.config;

public final class LegacyRuntimeGuard {
  private LegacyRuntimeGuard() {
  }

  public static void requireLegacyConsoleEnabled() {
    String propertyValue = System.getProperty("legacy.console.enabled");
    String envValue = System.getenv("LEGACY_CONSOLE_ENABLED");
    String effectiveValue = propertyValue != null ? propertyValue : envValue;

    boolean enabled = effectiveValue != null && "true".equalsIgnoreCase(effectiveValue.trim());
    if (!enabled) {
      throw new IllegalStateException(
          "Legacy console mode is disabled. Run Spring runtime via FoodDeliveryApplication. " +
              "To temporarily enable legacy console, set -Dlegacy.console.enabled=true."
      );
    }
  }
}

