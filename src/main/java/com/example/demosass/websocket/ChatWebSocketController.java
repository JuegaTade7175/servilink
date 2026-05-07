package com.example.demosass.websocket;

import com.example.demosass.dto.response.Responses.MessageResponse;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;

    @MessageMapping("/chat/{bookingId}")
    public void handleChatMessage(
            @DestinationVariable Long bookingId,
            @Payload Map<String, String> payload,
            Principal principal) {

        if (principal == null) {
            log.warn("Mensaje WebSocket rechazado: usuario no autenticado en booking {}", bookingId);
            return;
        }

        String token = payload.get("token");
        if (token == null || !jwtUtil.isTokenValid(token.replace("Bearer ", ""))) {
            log.warn("Mensaje WebSocket rechazado: token inválido para booking {}", bookingId);
            return;
        }

        Long senderId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        String content = payload.get("content");

        if (content == null || content.isBlank()) {
            log.warn("Mensaje vacío ignorado en booking {} de user {}", bookingId, senderId);
            return;
        }

        try {
            MessageResponse saved = messageService.send(senderId, bookingId, content);

            messagingTemplate.convertAndSend(
                "/topic/booking/" + bookingId,
                saved
            );

            messagingTemplate.convertAndSendToUser(
                saved.receiverId().toString(),
                "/queue/notifications",
                Map.of(
                    "type", "NEW_MESSAGE",
                    "bookingId", bookingId,
                    "senderName", saved.senderName(),
                    "preview", content.length() > 40 ? content.substring(0, 40) + "..." : content
                )
            );

            log.info("Mensaje WebSocket enviado: booking={}, sender={}, receiver={}",
                bookingId, senderId, saved.receiverId());

        } catch (Exception e) {
            log.error("Error procesando mensaje WebSocket en booking {}: {}", bookingId, e.getMessage());
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                Map.of("error", "No se pudo enviar el mensaje: " + e.getMessage())
            );
        }
    }

    @MessageMapping("/chat/{bookingId}/read")
    public void markAsRead(
            @DestinationVariable Long bookingId,
            @Payload Map<String, String> payload) {

        String token = payload.get("token");
        if (token == null || !jwtUtil.isTokenValid(token.replace("Bearer ", ""))) {
            return;
        }

        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        int count = messageService.markAsRead(userId, bookingId);
        log.debug("WebSocket: {} mensajes marcados como leídos en booking {} por user {}", count, bookingId, userId);
    }
}
