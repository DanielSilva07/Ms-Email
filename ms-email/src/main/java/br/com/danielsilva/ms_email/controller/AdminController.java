package br.com.danielsilva.ms_email.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints administrativos")
public class AdminController {


    @GetMapping("/health")
    @Operation(summary = "Verifica a saúde da aplicação")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "ms-email");
        healthInfo.put("timestamp", System.currentTimeMillis());
        healthInfo.put("caches", "Operational");
        healthInfo.put("rateLimit", "Operational");
        
        return ResponseEntity.ok(healthInfo);
    }
}
