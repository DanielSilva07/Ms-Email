package br.com.danielsilva.ms_email.controller;

import br.com.danielsilva.ms_email.dto.Cliente;
import br.com.danielsilva.ms_email.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private static final Logger logger = LoggerFactory
            .getLogger(EmailController.class);

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }


    @PostMapping
    public ResponseEntity<Void> receberSolicitacaoEmail(@RequestBody @Valid Cliente cliente) {
        try {
            logger.info("Requisição HTTP recebida para envio de e-mail para: {}", cliente.getEmail());

            emailService.sendEmailAsync(cliente);

            logger.info("E-mail processado e enfileirado para envio com sucesso.");
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (Exception e) {
            logger.error("Erro ao processar envio de e-mail via HTTP: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
