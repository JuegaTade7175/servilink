package com.example.demosass.service;

import com.example.demosass.domain.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class FcmPushService {

    @Value("${fcm.enabled:false}")
    private boolean fcmEnabled;

    @Value("${fcm.server-key:}")
    private String serverKey;

    public void sendPushNotification(String fcmToken, String title, String body,
                                      NotificationType type, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("FCM: Token nulo, notificación omitida: {}", title);
            return;
        }

        if (!fcmEnabled) {
            log.info("[FCM-SIMULADO] Push enviado → Token: {}... | Tipo: {} | Título: {} | Body: {}",
                fcmToken.substring(0, Math.min(fcmToken.length(), 20)), type, title, body);
            return;
        }
    }

    public void notifyNewBooking(String professionalFcmToken, String clientName,
                                  String serviceName, Long bookingId) {
        sendPushNotification(
            professionalFcmToken,
            "Nueva reserva recibida",
            clientName + " reservó: " + serviceName,
            NotificationType.BOOKING_CREATED,
            Map.of("bookingId", bookingId.toString(), "action", "VIEW_BOOKING")
        );
    }

    public void notifyBookingConfirmed(String clientFcmToken, String professionalName,
                                        Long bookingId) {
        sendPushNotification(
            clientFcmToken,
            "Reserva confirmada ✓",
            professionalName + " confirmó tu reserva",
            NotificationType.BOOKING_CONFIRMED,
            Map.of("bookingId", bookingId.toString(), "action", "VIEW_BOOKING")
        );
    }

    public void notifyNewMessage(String receiverFcmToken, String senderName,
                                  String messagePreview, Long bookingId) {
        sendPushNotification(
            receiverFcmToken,
            "Mensaje de " + senderName,
            messagePreview,
            NotificationType.NEW_MESSAGE,
            Map.of("bookingId", bookingId.toString(), "action", "OPEN_CHAT")
        );
    }

    public void notifyPaymentReceived(String professionalFcmToken, String amount,
                                       Long bookingId) {
        sendPushNotification(
            professionalFcmToken,
            "Pago recibido S/. " + amount,
            "El cliente completó el pago de su reserva",
            NotificationType.PAYMENT_RECEIVED,
            Map.of("bookingId", bookingId.toString(), "action", "VIEW_PAYMENT")
        );
    }

    public void notifyNewReview(String professionalFcmToken, String clientName,
                                 Integer rating, Long professionalId) {
        String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
        sendPushNotification(
            professionalFcmToken,
            "Nueva reseña " + stars,
            clientName + " te dejó una reseña de " + rating + "/5",
            NotificationType.NEW_REVIEW,
            Map.of("professionalId", professionalId.toString(), "action", "VIEW_REVIEWS")
        );
    }
}
