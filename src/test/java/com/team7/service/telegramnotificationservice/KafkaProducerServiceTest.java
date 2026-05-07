package com.team7.service.telegramnotificationservice;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

class KafkaProducerServiceTest {

  @Test
  void sendAuthCodeSendsToKafkaTopic() {
    @SuppressWarnings("unchecked")
    KafkaTemplate<String, String> template = Mockito.mock(KafkaTemplate.class);
    KafkaProducerService svc = new KafkaProducerService(template, new SimpleMeterRegistry());

    svc.sendAuthCode("123456");

    verify(template).send("auth-codes-topic", "123456");
  }

  @Test
  void initMetricsCreatesExpectedCounters() {
    @SuppressWarnings("unchecked")
    KafkaTemplate<String, String> template = Mockito.mock(KafkaTemplate.class);
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    KafkaProducerService svc = new KafkaProducerService(template, registry);

    svc.initMetrics();

    assertEquals(8.0, registry.counter("telegram_notifications_sent_total").count());
    assertEquals(8.0, registry.counter("telegram_notifications_failed_total").count());
  }

  @Test
  void sendAuthCodeIncrementsFailedMetricAndRethrows() {
    @SuppressWarnings("unchecked")
    KafkaTemplate<String, String> template = Mockito.mock(KafkaTemplate.class);
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    KafkaProducerService svc = new KafkaProducerService(template, registry);
    RuntimeException failure = new RuntimeException("Kafka down");
    doThrow(failure).when(template).send("auth-codes-topic", "123456");

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> svc.sendAuthCode("123456"));

    assertEquals(failure, thrown);
    assertEquals(1.0, registry.counter("telegram.notifications.failed").count());
  }
}

