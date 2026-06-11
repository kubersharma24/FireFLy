package com.emailagent.controller;

import com.emailagent.agent.EmailProducer;
import com.emailagent.model.BulkEmailRequestDTO;
import com.emailagent.model.EmailRequest;
import com.emailagent.service.HrBulkSendService;
import lombok.extern.slf4j.Slf4j;

import com.emailagent.service.FileStorageService;
import com.emailagent.service.HrExcelImportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Map;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/v1/hr")
public class HrController {

    @Autowired private HrExcelImportService importService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private  EmailProducer producer;
    @Autowired private HrBulkSendService hrBulkSendService;

    @PostMapping("/LoadHrList")
    public ResponseEntity<Map<String, Object>> loadHrList(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "File is empty"));
        }

        try {
            int count = importService.loadHrList(file);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "HR records imported successfully",
                    "recordsLoaded", count
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping(
            value = "/sendBulkEmail",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> sendEmail(
            @Valid @ModelAttribute BulkEmailRequestDTO request) {
        try {
            Map<String, Object> result = hrBulkSendService.sendBulkEmails(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[Controller] Bulk email failed", e);
            return ResponseEntity.internalServerError().body("Failed to process request");
        }
    }
}