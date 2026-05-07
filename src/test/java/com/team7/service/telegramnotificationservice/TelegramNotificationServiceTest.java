package com.team7.service.telegramnotificationservice;

import com.team7.model.authcoderequest.AuthCodeRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.cloud.stream.function.StreamBridge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class TelegramNotificationServiceTest {

  @Test
  void sendAuthCodeReturnsGeneratedCodeWhenBridgeSends() {
    StreamBridge bridge = Mockito.mock(StreamBridge.class);
    given(bridge.send(Mockito.eq("authCodeProducer-out-0"), Mockito.any(AuthCodeRequest.class))).willReturn(true);
    TelegramNotificationService svc = new TelegramNotificationService(bridge);

    String code = svc.sendAuthCode("chat-1", "user-1");
    assertNotNull(code);
    assertEquals(6, code.length());
    assertTrue(code.matches("\\d{6}"));

    ArgumentCaptor<AuthCodeRequest> captor = ArgumentCaptor.forClass(AuthCodeRequest.class);
    verify(bridge).send(Mockito.eq("authCodeProducer-out-0"), captor.capture());
    assertEquals("chat-1", captor.getValue().getChatId());
    assertEquals("user-1", captor.getValue().getUserId());
    assertEquals(code, captor.getValue().getCode());
  }

  @Test
  void sendAuthCodeThrowsWhenBridgeFails() {
    StreamBridge bridge = Mockito.mock(StreamBridge.class);
    given(bridge.send(Mockito.eq("authCodeProducer-out-0"), Mockito.any(AuthCodeRequest.class))).willReturn(false);
    TelegramNotificationService svc = new TelegramNotificationService(bridge);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> svc.sendAuthCode("chat-1", "user-1"));
    assertEquals("Failed to send auth code", ex.getMessage());
  }
}

