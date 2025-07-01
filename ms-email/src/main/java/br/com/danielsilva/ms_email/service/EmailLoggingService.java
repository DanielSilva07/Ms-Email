package br.com.danielsilva.ms_email.service;

import br.com.danielsilva.ms_email.dto.Cliente;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(EmailLoggingService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    private final JavaMailSender mailSender;
    private final MetricsService metricsService;
    
    @Autowired
    public EmailLoggingService(JavaMailSender mailSender, MetricsService metricsService) {
        this.mailSender = mailSender;
        this.metricsService = metricsService;
    }
    
    @Async
    public void logEmailSent(Cliente cliente, String subject, String content) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logMessage = String.format(
            "[%s] E-mail enviado para %s - Assunto: %s",
            timestamp, cliente.getEmail(), subject
        );
        
        logger.info(logMessage);
        metricsService.incrementEmailSent();
    }
    
    @Async
    public void logEmailError(Cliente cliente, String subject, Exception exception) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String errorMessage = exception.getMessage();
        
        String logMessage = String.format(
            "[%s] Falha ao enviar e-mail para %s - Assunto: %s - Erro: %s",
            timestamp, cliente.getEmail(), subject, errorMessage
        );
        
        logger.error(logMessage, exception);
        metricsService.incrementEmailError();
        
        // Se for um erro de e-mail, podemos notificar os administradores
        if (exception instanceof MailException || exception instanceof MessagingException) {
            notifyAdminsAboutFailure(cliente, subject, errorMessage);
        }
    }
    
    private void notifyAdminsAboutFailure(Cliente cliente, String subject, String error) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("admin@example.com"); // Deveria vir de configuração
            message.setSubject("[ALERTA] Falha no envio de e-mail");
            
            String content = String.format(
                "Falha ao enviar e-mail:\n" +
                "Destinatário: %s\n" +
                "Assunto: %s\n" +
                "Erro: %s\n\n" +
                "Data/Hora: %s",
                cliente.getEmail(),
                subject,
                error,
                LocalDateTime.now().format(DATE_FORMAT)
            );
            
            message.setText(content);
            mailSender.send(message);
            logger.info("Notificação de falha enviada para os administradores");
        } catch (Exception e) {
            logger.error("Falha ao enviar notificação de falha para os administradores: {}", e.getMessage(), e);
        }
    }
    
    public void logEmailOpened(String trackingId, String recipient) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logMessage = String.format(
            "[%s] E-mail aberto - ID: %s - Destinatário: %s",
            timestamp, trackingId, recipient
        );
        
        logger.info(logMessage);
        metricsService.incrementEmailOpened();
    }
    
    public void logLinkClicked(String trackingId, String recipient, String url) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logMessage = String.format(
            "[%s] Link clicado - ID: %s - Destinatário: %s - URL: %s",
            timestamp, trackingId, recipient, url
        );
        
        logger.info(logMessage);
        metricsService.incrementEmailOpened();
    }
}
