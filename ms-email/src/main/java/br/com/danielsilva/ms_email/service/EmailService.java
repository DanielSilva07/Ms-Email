package br.com.danielsilva.ms_email.service;

import br.com.danielsilva.ms_email.dto.Cliente;
import br.com.danielsilva.ms_email.exceptions.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Serviço responsável pelo envio assíncrono de e-mails com templates HTML.
 * Utiliza a anotação @Async para execução em uma thread separada.
 */
@Service
public class EmailService {
    
    private final MetricsService metricsService;
    private final RetryRegistry retryRegistry;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String SENDER_EMAIL = "danielcodelab2@gmail.com";
    private static final String TEMPLATE_IMC_EMAIL = "imc-email";
    
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(MetricsService metricsService, 
                       RetryRegistry retryRegistry, 
                       JavaMailSender emailSender, 
                       TemplateEngine templateEngine) {
        this.metricsService = metricsService;
        this.retryRegistry = retryRegistry;
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }


    @Async
    @Retry(name = "emailService", fallbackMethod = "sendEmailFallback")
    public void sendEmailAsync(Cliente cliente) {
        if (cliente == null || cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            logger.error("Falha ao enviar e-mail: Cliente ou e-mail inválido");
            metricsService.incrementEmailError();
            throw new IllegalArgumentException("Cliente ou e-mail não pode ser nulo");
        }

        try {
            MimeMessage message = createMimeMessage(cliente);
            emailSender.send(message);
            logger.info("E-mail HTML enviado com sucesso para: {}", cliente.getEmail());
            metricsService.incrementEmailSent();
        } catch (MailException | MessagingException ex) {
            logger.error("Erro ao enviar e-mail para {}: {}", cliente.getEmail(), ex.getMessage(), ex);
            metricsService.incrementEmailError();
            throw new EmailException("Falha ao enviar e-mail: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.error("Erro inesperado ao enviar e-mail para {}: {}", cliente.getEmail(), ex.getMessage(), ex);
            metricsService.incrementEmailError();
            throw new EmailException("Erro inesperado ao processar o envio de e-mail", ex);
        }
    }

    /**
     * Método de fallback para o envio de e-mails
     */
    public void sendEmailFallback(Cliente cliente, Exception ex) {
        String errorMsg = String.format("Falha ao enviar e-mail após tentativas para %s: %s",
            cliente != null ? cliente.getEmail() : "null",
            ex.getMessage());
        logger.error(errorMsg);
        metricsService.incrementEmailError();

    }


    private MimeMessage createMimeMessage(Cliente cliente) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // Configurações básicas da mensagem
        helper.setFrom(SENDER_EMAIL);
        helper.setTo(cliente.getEmail());
        helper.setSubject(cliente.getSubject() != null ?
                         cliente.getSubject() : "Seu IMC - Acompanhamento de Saúde");
        
        // Processa o template HTML
        String htmlContent = processTemplate(cliente);
        helper.setText(htmlContent, true); // true = isHTML

        try {
            // Adiciona a imagem inline
            ClassPathResource resource = new ClassPathResource("static/Daniel_s_logo_2_.ico");
            if (resource.exists()) {
                // O primeiro parâmetro deve corresponder ao cid: usado no template HTML
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
    
    /**
     * Processa o template Thymeleaf com os dados do cliente.
     */
    private String processTemplate(Cliente cliente) {
        Context context = new Context();
        context.setVariable("cliente", cliente);
        context.setVariable("currentYear", java.time.Year.now().getValue());
        return templateEngine.process(TEMPLATE_IMC_EMAIL, context);
    }
    

}
