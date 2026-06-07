package com.emailagent.agent;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.emailagent.model.EmailResponse;
import com.emailagent.model.EmailTemplateRequest;
import com.emailagent.model.FileMessage;
import com.emailagent.model.GeneratedEmailContent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailOrchestratorAgent {

    private final LlmEmailGeneratorAgent llmAgent;

    public EmailResponse process(EmailTemplateRequest request) {
        log.info("======================================== ");
        log.info("[Orchestrator] New task | Topic: {}", request.getTopic());
        log.info("========================================");

        try {
            // Step 1: Generate email via LLM
            log.info("[Orchestrator] Step 1 → LLM Agent generating email...");
            GeneratedEmailContent content = llmAgent.generateEmail(request);
            log.info("[Orchestrator] Step 1 ✅ Email content ready.");
            return EmailResponse.success(
                    "Email generated and sent successfully!",
                    content.getSubject(),
                    content.getBody()
            );

        } catch (Exception e) {
            log.error("[Orchestrator] ❌ Task failed: {}", e.getMessage(), e);
            return EmailResponse.failure(e.getMessage());
        }
    }
    
    public FileMessage convertToFileMessage(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
        	log.error("File is null  ----->");
            return null;
        }

        return FileMessage.builder()
                .fileName(file.getName())
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .data(file.getBytes())
                .build();
    }
}
