package com.emailagent.service;

import com.emailagent.agent.EmailProducer;
import com.emailagent.model.*;
import com.emailagent.repo.BulkEmailBatchRepository;
import com.emailagent.repo.EmailLogRepository;
import com.emailagent.repo.HrContactRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class HrBulkSendService {

    private static final int PAGE_SIZE = 400;

    @Autowired private EmailProducer producer;
    @Autowired private HrContactRepository hrContactRepository;
    @Autowired private BulkEmailBatchRepository batchRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private EmailLogRepository emailLogRepository;

    public Map<String, Object> sendBulkEmails(BulkEmailRequestDTO request) throws IOException {

        int page = request.getPageNo();
        // Step 1: Check if this page was already processed
        if (batchRepository.existsByPageAndPageSize(page, PAGE_SIZE)) {
            log.warn("[BulkService] Page {} already processed — skipping", page);
            return Map.of(
                    "status", "skipped",
                    "reason", "Page " + page + " already processed",
                    "page", page,
                    "pageSize", PAGE_SIZE
            );
        }

        // Step 2: Save attachments
        log.info("[BulkService] Saving resume...");
        String resumePath = fileStorageService.save(request.getResume());

        log.info("[BulkService] Saving cover letter...");
        String coverLetterPath = fileStorageService.save(request.getCoverLetter());

        // Step 3: Fetch paginated HR contacts
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<HrContact> hrPage = hrContactRepository.findAll(pageable);
        List<HrContact> hrList = hrPage.getContent();


        log.info("[BulkService] Processing {} HR contacts (page={})", hrList.size(), page);

        // Step 4: Queue emails to Kafka
        for (HrContact hr : hrList) {
            String uuid = UUID.randomUUID().toString();
              boolean duplicate = emailLogRepository.existsByToEmail(hr.getEmail());
              if(duplicate) {
                  log.warn("Email Already send to ----->> : {}",hr.getEmail());
                  continue;
              }
            EmailRequest emailRequest = EmailRequest.builder()
                    .UUID(uuid)
                    .toEmail(hr.getEmail())
                    .toName(hr.getName())
                    .body(request.getBody())
                    .subject(request.getSubject())
                    .myName(request.getMyName())
                    .myNumber(request.getMyNumber())
                    .resumePath(resumePath)
                    .coverLetterPath(coverLetterPath)
                    .skills(request.getSkills())
                    .headLineSkill(request.getHeadLineSkill())
                    .JobTitle(request.getJobTitle())
                    .build();

            log.info("[BulkService] Queuing → {} | UUID: {}", hr.getEmail(), uuid);
            producer.sendEmailEvent(emailRequest);
        }

        // Step 5: Mark this page as done
        BulkEmailBatch batch = BulkEmailBatch.builder()
                .page(page)
                .pageSize(PAGE_SIZE)
                .emailsQueued(hrList.size())
                .processedAt(LocalDateTime.now())
                .build();
        batchRepository.save(batch);

        log.info("[BulkService] Page {} saved to bulk_email_batch table", page);

        return Map.of(
                "status", "success",
                "page", page,
                "pageSize", PAGE_SIZE,
                "emailsQueued", hrList.size(),
                "totalHrContacts", hrPage.getTotalElements(),
                "totalPages", hrPage.getTotalPages(),
                "isLastPage", hrPage.isLast()
        );
    }


}
