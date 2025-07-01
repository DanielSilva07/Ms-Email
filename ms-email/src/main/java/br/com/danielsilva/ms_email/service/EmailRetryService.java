package br.com.danielsilva.ms_email.service;

import br.com.danielsilva.ms_email.domain.EmailRetry;
import br.com.danielsilva.ms_email.repository.EmailRetryRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRetryService {

        private final EmailRetryRepository emailRetryRepository;
        private final JavaMailSender javaMailSender;

        @Transactional
        public void scheduleRetry(String to, String subject, String content) {
            EmailRetry retry = EmailRetry.create(to, subject, content);
            emailRetryRepository.save(retry);
            log.info("Nova tentativa de e-mail agendada para: {}", to);
        }

        @Scheduled(fixedRate = 6000) //Runs every 6 seconds
        @Transactional
        public void processPendingEmails() {
            log.debug("Verificando e-mails pendentes para reenvio...");

            emailRetryRepository.findByProcessedFalseAndNextRetryAtBefore(LocalDateTime.now())
                    .forEach(this::processEmailRetry);
        }

        private void processEmailRetry(EmailRetry retry) {
            try {
                log.info("Tentando reenviar e-mail para: {} (tentativa {}/{})",
                        retry.getToEmail(), retry.getRetryCount() + 1, retry.getMaxRetries());

                MimeMessage message = createMimeMessage(retry);
                javaMailSender.send(message);

                emailRetryRepository.markAsProcessed(retry.getId());
                log.info("E-mail reenviado com sucesso para: {}", retry.getToEmail());

            } catch (Exception e) {
                handleRetryFailure(retry, e);
            }
        }

        private MimeMessage createMimeMessage(EmailRetry retry) throws MessagingException {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(retry.getToEmail());
            helper.setSubject(retry.getSubject());
            helper.setText(retry.getContent(), true);
            return message;
        }

        private void handleRetryFailure(EmailRetry retry, Exception e) {
            log.error("Falha ao reenviar e-mail para: {}", retry.getToEmail(), e);

            retry.incrementRetry();

            if (retry.getRetryCount() >= retry.getMaxRetries()) {
                log.error("Número máximo de tentativas atingido para o e-mail: {}", retry.getToEmail());
                emailRetryRepository.markAsProcessed(retry.getId());
            } else {
                emailRetryRepository.save(retry);
                log.info("Próxima tentativa para {} em: {}",
                        retry.getToEmail(), retry.getNextRetryAt());
            }

            emailRetryRepository.updateError(
                    retry.getId(),
                    e.getMessage(),
                    LocalDateTime.now()
            );
        }
}

