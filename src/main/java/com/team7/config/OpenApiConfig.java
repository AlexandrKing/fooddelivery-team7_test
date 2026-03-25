package com.team7.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI foodDeliveryOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Food Delivery API")
            .description("REST API documentation for Food Delivery project")
            .version("v1")
            .contact(new Contact().name("Team 7")));
  }
}

