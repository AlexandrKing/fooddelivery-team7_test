package com.team7.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeModeConfig {
  private static final Logger log = LoggerFactory.getLogger(RuntimeModeConfig.class);

  @PostConstruct
  void logRuntimeMode() {
    log.info("Primary runtime mode: FoodDeliveryApplication + REST API (legacy console is deprecated).");
  }
}

