package com.emailagent.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_email_batch",
       uniqueConstraints = @UniqueConstraint(columnNames = {"page", "page_size"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int page;
    private int pageSize;
    private int emailsQueued;
    private LocalDateTime processedAt;
}