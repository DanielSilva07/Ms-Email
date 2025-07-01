package br.com.danielsilva.ms_email.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public Retry emailRetry(RetryRegistry retryRegistry) {
        // Configuração do retry para o serviço de e-mail
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(6)
                .waitDuration(Duration.ofMillis(5000))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .failAfterMaxAttempts(true)
                .build();
        return retryRegistry.retry("emailService", config);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }
}
