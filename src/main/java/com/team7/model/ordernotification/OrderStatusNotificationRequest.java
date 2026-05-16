package com.team7.model.ordernotification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusNotificationRequest {
    private String chatId;
    private String userId;
    private Long orderId;
    private String status;
    private String message;
}