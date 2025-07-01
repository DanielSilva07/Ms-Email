package br.com.danielsilva.ms_email.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private Counter emailSentCounter;
    private Counter emailErrorCounter;
    private Counter emailOpenedCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        emailSentCounter = Counter.builder("email.sent")
                .description("Número de e-mails enviados com sucesso")
                .register(meterRegistry);

        emailErrorCounter = Counter.builder("email.errors")
                .description("Número de falhas no envio de e-mails")
                .tag("type", "send_failure")
                .register(meterRegistry);

        emailOpenedCounter = Counter.builder("email.opened")
                .description("Número de e-mails abertos")
                .register(meterRegistry);
    }

    public void incrementEmailSent() {
        emailSentCounter.increment();
    }

    public void incrementEmailError() {
        emailErrorCounter.increment();
    }

    public void incrementEmailOpened() {
        emailOpenedCounter.increment();
    }
}
