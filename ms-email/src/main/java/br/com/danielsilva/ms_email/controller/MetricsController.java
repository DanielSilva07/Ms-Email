package br.com.danielsilva.ms_email.controller;

import br.com.danielsilva.ms_email.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics", description = "Endpoint para métricas da aplicação")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/health")
    @Operation(summary = "Verifica a saúde da aplicação")
    public Map<String, Object> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "ms-email");
        healthInfo.put("timestamp", System.currentTimeMillis());
        return healthInfo;
    }
}
