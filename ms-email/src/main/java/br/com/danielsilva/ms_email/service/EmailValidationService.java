package br.com.danielsilva.ms_email.service;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class EmailValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$", 
        Pattern.CASE_INSENSITIVE
    );


    private final EmailValidator emailValidator;

    public EmailValidationService() {
        this.emailValidator = EmailValidator.getInstance();
    }


    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return emailValidator.isValid(email);
    }

    public boolean isDisposableEmail(String email) {
        // Lista de domínios de e-mail descartáveis (simplificada)
        String[] disposableDomains = {
            "tempmail.com", "mailinator.com", "guerrillamail.com",
            "10minutemail.com", "throwawaymail.com", "yopmail.com"
        };

        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        
        for (String disposable : disposableDomains) {
            if (domain.endsWith(disposable)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasValidFormat(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public String sanitizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
