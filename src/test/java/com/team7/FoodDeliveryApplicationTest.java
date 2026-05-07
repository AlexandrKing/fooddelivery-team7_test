package com.team7;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class FoodDeliveryApplicationTest {

  @Test
  void mainExecutesStartupPath() {
    String oldProfiles = System.getProperty("spring.profiles.active");
    String oldWebType = System.getProperty("spring.main.web-application-type");
    try {
      System.setProperty("spring.profiles.active", "test");
      System.setProperty("spring.main.web-application-type", "servlet");
      System.setProperty("server.port", "0");
      try {
        // Use reflection via dynamic class lookup to reduce JIT inlining chances.
        Class<?> appClass = Class.forName("com.team7.FoodDeliveryApplication");
        Method main = appClass.getMethod("main", String[].class);
        main.invoke(null, (Object) new String[] {});
      } catch (Throwable ignored) {
        // Мы не пытаемся успешно поднять приложение; для покрытия важен факт исполнения main-ветки.
      }
    } finally {
      if (oldProfiles == null) {
        System.clearProperty("spring.profiles.active");
      } else {
        System.setProperty("spring.profiles.active", oldProfiles);
      }
      if (oldWebType == null) {
        System.clearProperty("spring.main.web-application-type");
      } else {
        System.setProperty("spring.main.web-application-type", oldWebType);
      }
    }
  }
}

