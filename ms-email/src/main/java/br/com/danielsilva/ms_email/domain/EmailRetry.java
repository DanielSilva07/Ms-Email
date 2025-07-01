package br.com.danielsilva.ms_email.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_retries")
public class EmailRetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String toEmail;
    
    @Column(columnDefinition = "TEXT")
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private int retryCount = 0;
    
    @Column(nullable = false)
    private int maxRetries = 3;
    
    @Column(nullable = false)
    private LocalDateTime nextRetryAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime lastAttemptAt;
    
    @Column
    private String lastError;
    
    @Column(nullable = false)
    private boolean processed = false;


    public static EmailRetry create(String toEmail, String subject, String content) {
        EmailRetry retry = new EmailRetry();
        retry.setToEmail(toEmail);
        retry.setSubject(subject);
        retry.setContent(content);
        retry.calculateNextRetry();
        return retry;
    }
    
    public void incrementRetry() {
        this.retryCount++;
        this.lastAttemptAt = LocalDateTime.now();
        calculateNextRetry();
    }
    
    private void calculateNextRetry() {
        // Exponencial backoff: 5min, 15min, 30min, 1h, 2h, 4h, etc.
        int[] backoffMinutes = {5, 15, 30, 60, 120, 240};
        int index = Math.min(retryCount, backoffMinutes.length - 1);
        int minutes = backoffMinutes[index];
        this.nextRetryAt = LocalDateTime.now().plusMinutes(minutes);
    }
    
    public boolean canRetry() {
        return !processed && retryCount < maxRetries &&
                LocalDateTime.now().isAfter(nextRetryAt);
    }
}
