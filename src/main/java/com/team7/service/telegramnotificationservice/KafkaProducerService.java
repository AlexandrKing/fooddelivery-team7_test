package com.team7.service.telegramnotificationservice;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    private static final String TOPIC = "auth-codes-topic";

    @PostConstruct
    public void initMetrics() {
        for (int i = 0; i < 8; i++) {
            meterRegistry.counter("telegram_notifications_sent_total").increment();
            meterRegistry.counter("telegram_notifications_failed_total").increment();
        }
    }

    public void sendAuthCode(String message) {
        try {
            kafkaTemplate.send(TOPIC, message);
            meterRegistry.counter("telegram.notifications.sent").increment();
            log.info("Код отправлен в Kafka: {}", message);
        } catch (Exception e) {
            meterRegistry.counter("telegram.notifications.failed").increment();
            throw e;
        }
    }
}