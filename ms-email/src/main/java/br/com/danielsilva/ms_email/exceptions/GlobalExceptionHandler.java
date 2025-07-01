package br.com.danielsilva.ms_email.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<Object> handleEmailException(EmailException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Falha no processamento de e-mail");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Requisição inválida");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Erro interno no servidor");
        body.put("message", "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.");
        
        // Em produção, não exponha detalhes do erro
        if (isDevelopment()) {
            body.put("details", ex.getMessage());
        }
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private boolean isDevelopment() {
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        return env != null && (env.contains("dev") || env.contains("local"));
    }
}
