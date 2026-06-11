package com.emailagent.repo;

import com.emailagent.model.BulkEmailBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkEmailBatchRepository extends JpaRepository<BulkEmailBatch, Long> {
    boolean existsByPageAndPageSize(int page, int pageSize);
}