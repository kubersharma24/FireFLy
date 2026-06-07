package com.fireFly.SMS.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fireFly.SMS.model.EmailLog;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
	 Optional<EmailLog> findByToEmail(String toEmail);
	 Optional<EmailLog> findByUuid(String uuid);
}