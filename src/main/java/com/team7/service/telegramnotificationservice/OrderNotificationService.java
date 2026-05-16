package com.team7.service.telegramnotificationservice;

import com.team7.model.ordernotification.OrderStatusNotificationRequest;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final StreamBridge streamBridge;
    private final UserJpaRepository userJpaRepository;

    public void sendTelegramConnected(Long userId) {
        send(
                userId,
                null,
                "TELEGRAM_CONNECTED",
                "✅ Telegram подключён к вашему аккаунту. Теперь вы будете получать уведомления о заказах."
        );
    }

    public void sendOrderCreated(Long userId, Long orderId) {
        send(
                userId,
                orderId,
                "CREATED",
                "✅ Ваш заказ №" + orderId + " создан и ожидает обработки."
        );
    }

    public void sendOrderAcceptedForDelivery(Long userId, Long orderId) {
        send(
                userId,
                orderId,
                "ACCEPTED_FOR_DELIVERY",
                "🚚 Ваш заказ №" + orderId + " принят курьером в доставку."
        );
    }

    public void sendOrderDelivered(Long userId, Long orderId) {
        send(
                userId,
                orderId,
                "DELIVERED",
                "🎉 Ваш заказ №" + orderId + " доставлен. Приятного аппетита!"
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
}