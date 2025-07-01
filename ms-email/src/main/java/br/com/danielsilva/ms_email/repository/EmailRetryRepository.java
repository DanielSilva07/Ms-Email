package br.com.danielsilva.ms_email.repository;

import br.com.danielsilva.ms_email.domain.EmailRetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailRetryRepository extends JpaRepository<EmailRetry, Long> {
    
    List<EmailRetry> findByProcessedFalseAndNextRetryAtBefore(LocalDateTime now);
    
    @Transactional
    @Modifying
    @Query("UPDATE EmailRetry e SET e.processed = true WHERE e.id = :id")
    void markAsProcessed(Long id);
    
    @Transactional
    @Modifying
    @Query("UPDATE EmailRetry e SET e.lastError = :error, e.lastAttemptAt = :now WHERE e.id = :id")
    void updateError(Long id, String error, LocalDateTime now);
}
