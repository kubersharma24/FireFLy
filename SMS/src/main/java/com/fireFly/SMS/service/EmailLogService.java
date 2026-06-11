package com.fireFly.SMS.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fireFly.SMS.Repo.EmailLogRepository;
import com.fireFly.SMS.model.EmailLog;
import com.fireFly.SMS.model.EmailRequest;

@Slf4j
@Service
public class EmailLogService {

    @Autowired
    private EmailLogRepository repository;
    
    public EmailLog createPendingLog(String toEmail, String toName, String subject, EmailRequest request) {
        if (repository.existsByUuid(request.getUUID())) {
            log.warn("[EmailLogService] Duplicate UUID {} — skipping insert", request.getUUID());
            return repository.findFirstByUuid(request.getUUID())
                    .orElseThrow();
        }

        EmailLog log = new EmailLog();
        log.setToEmail(toEmail);
        log.setToName(toName);
        log.setSubject(subject);
        log.setUuid(request.getUUID());
        log.setToName(request.getToName());
        log.setStatus("PENDING");


        return repository.save(log);
    }

    public void markSuccess(String uuid) {

        EmailLog log = repository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Email log not found for: " + uuid));

        log.setStatus("SUCCESS");
        log.setErrorMessage(null);

        repository.save(log);
    }
    
    public void markFailed(String uuid, String errorMessage) {

        EmailLog log = repository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Email log not found for: " + uuid));

        log.setStatus("FAILED");
        log.setErrorMessage(errorMessage);

        repository.save(log);
    }
}