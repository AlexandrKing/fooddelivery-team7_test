package com.team7.service.telegramnotificationservice;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

class KafkaProducerServiceTest {

  @Test
  void sendAuthCodeSendsToKafkaTopic() {
    @SuppressWarnings("unchecked")
    KafkaTemplate<String, String> template = Mockito.mock(KafkaTemplate.class);
    KafkaProducerService svc = new KafkaProducerService(template);

    svc.sendAuthCode("123456");

    verify(template).send("auth-codes-topic", "123456");
  }
}

