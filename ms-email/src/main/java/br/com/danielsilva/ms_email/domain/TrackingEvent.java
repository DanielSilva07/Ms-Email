package br.com.danielsilva.ms_email.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {
    private String id;
    private String trackingId;
    private String email;
    private EventType eventType;
    private String details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    
    public enum EventType {
        EMAIL_SENT,     // E-mail enviado
        EMAIL_OPENED,   // E-mail aberto
        LINK_CLICKED,   // Link clicado
        EMAIL_BOUNCED,  // E-mail retornou
        EMAIL_REPORTED  // E-mail reportado como spam
    }
}
