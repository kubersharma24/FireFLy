package com.fireFly.SMS.service;

import com.fireFly.SMS.agent.EmailOrchestratorAgent;
import com.fireFly.SMS.model.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailProcessorService {

    private final EmailOrchestratorAgent orchestratorAgent;

    @Async("emailTaskExecutor")     // ← works now, called from external bean
    public void processAsync(EmailRequest message) {
        log.info("{}[Processor] Async thread started: {}",
                message.getUUID(), Thread.currentThread().getName());
        orchestratorAgent.process(message);
    }
}