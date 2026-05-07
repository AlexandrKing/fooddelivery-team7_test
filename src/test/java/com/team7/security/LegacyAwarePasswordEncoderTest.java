package com.team7.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LegacyAwarePasswordEncoderTest {

  private final LegacyAwarePasswordEncoder encoder = new LegacyAwarePasswordEncoder();

  @Test
  void encodeCreatesBcryptAndMatches() {
    String hash = encoder.encode("secret");
    assertNotNull(hash);
    assertTrue(hash.startsWith("$2"));
    assertTrue(encoder.matches("secret", hash));
    assertFalse(encoder.matches("wrong", hash));
  }

  @Test
  void matchesReturnsFalseWhenEncodedNull() {
    assertFalse(encoder.matches("x", null));
  }

  @Test
  void supportsLegacyPlaintextFallback() {
    assertTrue(encoder.matches("secret", "secret"));
    assertFalse(encoder.matches("secret", "other"));
    assertFalse(encoder.matches(null, "secret"));
  }
}

