package com.team7.service.telegramnotificationservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "auth-codes-topic";

    public void sendAuthCode(String message) {
        kafkaTemplate.send(TOPIC, message);
        log.info("Код отправлен в Kafka: {}", message);
    }
}