package br.com.danielsilva.ms_email.service;

import br.com.danielsilva.ms_email.dto.Cliente;
import br.com.danielsilva.ms_email.exceptions.EmailException;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String SENDER_EMAIL = "danielcodelab2@gmail.com";
    private static final String TEMPLATE_IMC_EMAIL = "imc-email";
    private final RetryRegistry retryRegistry;
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(RetryRegistry retryRegistry,
                        JavaMailSender emailSender,
                        TemplateEngine templateEngine) {
        this.retryRegistry = retryRegistry;
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }


    @Async
    @Retry(name = "emailService", fallbackMethod = "sendEmailFallback")
    public void sendEmailAsync(Cliente cliente) {
        logger.info("Received message from IMC-APP");
        if (cliente == null || cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            logger.error("Falha ao enviar e-mail: Cliente ou e-mail inválido");
            throw new IllegalArgumentException("Cliente ou e-mail não pode ser nulo");
        }

        try {
            MimeMessage message = createMimeMessage(cliente);
            emailSender.send(message);
            logger.info("E-mail HTML enviado com sucesso para: {}", cliente.getEmail());
        } catch (MailException | MessagingException ex) {
            logger.error("Erro ao enviar e-mail para {}: {}", cliente.getEmail(), ex.getMessage(), ex);
            throw new EmailException("Falha ao enviar e-mail: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.error("Erro inesperado ao enviar e-mail para {}: {}", cliente.getEmail(), ex.getMessage(), ex);
            throw new EmailException("Erro inesperado ao processar o envio de e-mail", ex);
        }
    }

    public void sendEmailFallback(Cliente cliente, Exception ex) {
        String errorMsg = String.format("Falha ao enviar e-mail após tentativas para %s: %s",
                cliente != null ? cliente.getEmail() : "null",
                ex.getMessage());
        logger.error(errorMsg);
    }


    private MimeMessage createMimeMessage(Cliente cliente) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(SENDER_EMAIL);
        helper.setTo(cliente.getEmail());
        helper.setSubject(cliente.getSubject() != null ?
                cliente.getSubject() : "Seu IMC - Acompanhamento de Saúde");

        String htmlContent = processTemplate(cliente);
        helper.setText(htmlContent, true); // true = isHTML

        try {
            ClassPathResource resource = new ClassPathResource("static/Daniel_s_logo_2_.ico");
            if (resource.exists()) {
                helper.addInline("Daniel_s_logo_2_.ico", resource, "image/x-icon");
                logger.info("Logo adicionado com sucesso ao e-mail");
            } else {
                logger.warn("Arquivo de logo não encontrado em: static/Daniel_s_logo_2_.ico");
            }
        } catch (Exception e) {
            logger.error("Erro ao adicionar a imagem ao e-mail", e);
        }
        return message;
    }

    private String processTemplate(Cliente cliente) {
        Context context = new Context();
        context.setVariable("cliente", cliente);
        context.setVariable("currentYear", java.time.Year.now().getValue());
        return templateEngine.process(TEMPLATE_IMC_EMAIL, context);
    }
}
