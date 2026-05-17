package com.team7.service.telegramnotificationservice;

import com.team7.model.ordernotification.OrderStatusNotificationRequest;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final StreamBridge streamBridge;
    private final UserJpaRepository userJpaRepository;

    public void sendTelegramConnected(Long userId) {
        send(
                userId,
                null,
                "TELEGRAM_CONNECTED",
                "Telegram подключен к вашему аккаунту. Теперь вы будете получать уведомления о заказах."
        );
    }

    public void sendOrderCreated(Long userId, Long orderId, LocalDateTime createdAt, String restaurantName) {
        String message = String.format(
                "Ваш заказ №%d создан.%n" +
                        "Ресторан: %s%n" +
                        "Время создания: %s%n" +
                        "Статус: ожидает обработки.",
                orderId,
                safeText(restaurantName, "не указан"),
                formatDateTime(createdAt)
        );

        send(
                userId,
                orderId,
                "CREATED",
                message
        );
    }

    public void sendOrderAcceptedForDelivery(Long userId, Long orderId, String courierName) {
        String message = String.format(
                "Ваш заказ №%d принят в доставку.%n" +
                        "Ваш курьер: %s.",
                orderId,
                safeText(courierName, "не указан")
        );

        send(
                userId,
                orderId,
                "ACCEPTED_FOR_DELIVERY",
                message
        );
    }

    public void sendOrderPickedUp(Long userId, Long orderId) {
        String message = String.format(
                "Ваш заказ №%d забран курьером.%n" +
                        "Курьер уже направляется к вам.",
                orderId
        );

        send(
                userId,
                orderId,
                "PICKED_UP",
                message
        );
    }

    public void sendOrderDelivered(Long userId, Long orderId, LocalDateTime deliveredAt) {
        String message = String.format(
                "Ваш заказ №%d доставлен.%n" +
                        "Время доставки: %s.",
                orderId,
                formatDateTime(deliveredAt)
        );

        send(
                userId,
                orderId,
                "DELIVERED",
                message
        );
    }

    private void send(Long userId, Long orderId, String status, String message) {
        try {
            UserEntity user = userJpaRepository.findById(userId).orElse(null);

            if (user == null) {
                log.warn("Order notification skipped: user {} not found, order {}", userId, orderId);
                return;
            }

            if (user.getTelegramChatId() == null || user.getTelegramChatId().isBlank()) {
                log.warn("Order notification skipped: user {} has no telegram_chat_id, order {}", userId, orderId);
                return;
            }

            OrderStatusNotificationRequest request = new OrderStatusNotificationRequest(
                    user.getTelegramChatId(),
                    String.valueOf(userId),
                    orderId,
                    status,
                    message
            );

            boolean sent = streamBridge.send("orderStatusProducer-out-0", request);

            if (sent) {
                log.info(
                        "Order status notification sent to Kafka: user={}, order={}, status={}",
                        userId, orderId, status
                );
            } else {
                log.error(
                        "Failed to send order status notification to Kafka: user={}, order={}, status={}",
                        userId, orderId, status
                );
            }
        } catch (Exception e) {
            log.error(
                    "Order notification failed but business flow continues: user={}, order={}, status={}, error={}",
                    userId, orderId, status, e.getMessage()
            );
        }
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "не указано";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private static String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}