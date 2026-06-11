package com.emailagent.repo;

import com.emailagent.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
	 Optional<EmailLog> findByToEmail(String toEmail);
	 Optional<EmailLog> findByUuid(String uuid);
	Optional<EmailLog> findFirstByUuid(String uuid);
	boolean existsByUuid(String uuid);
	boolean existsByToEmail(String email);
}