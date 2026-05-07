package com.team7.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

  @Test
  void buildsOpenApiWithExpectedInfo() {
    OpenApiConfig cfg = new OpenApiConfig();
    var openApi = cfg.foodDeliveryOpenAPI();
    assertNotNull(openApi);
    assertNotNull(openApi.getInfo());
    assertEquals("Food Delivery API", openApi.getInfo().getTitle());
    assertEquals("v1", openApi.getInfo().getVersion());
    assertNotNull(openApi.getInfo().getContact());
    assertEquals("Team 7", openApi.getInfo().getContact().getName());
  }
}

