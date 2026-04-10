package com.team7.service.telegramnotificationservice;

import com.team7.model.authcoderequest.AuthCodeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final StreamBridge streamBridge;
    private final SecureRandom random = new SecureRandom();

    public String sendAuthCode(String chatId, String userId) {
        String code = String.format("%06d", random.nextInt(1000000));

        AuthCodeRequest request = new AuthCodeRequest(chatId, code, userId);

        boolean sent = streamBridge.send("authCodeProducer-out-0", request);

        if (sent) {
            log.info("Auth code sent to Kafka for user: {}", userId);
            return code;
        } else {
            log.error("Failed to send auth code to Kafka for user: {}", userId);
            throw new RuntimeException("Failed to send auth code");
        }
    }
}
