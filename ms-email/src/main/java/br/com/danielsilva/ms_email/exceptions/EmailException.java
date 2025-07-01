package br.com.danielsilva.ms_email.exceptions;

/**
 * Exceção lançada quando ocorre um erro no processamento de e-mails.
 */
public class EmailException extends RuntimeException {
    
    public EmailException(String message) {
        super(message);
    }
    
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
