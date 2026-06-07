package com.fireFly.SMS.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fireFly.SMS.agent.EmailOrchestratorAgent;
import com.fireFly.SMS.model.EmailRequest;
import com.fireFly.SMS.model.EmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {
	
	private final EmailOrchestratorAgent orchestratorAgent;
	
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FireFly Agent is running ✅");
    }
    
    @PostMapping(value = "/sendEmail",  
    		consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
            )
    public ResponseEntity<Object> sendEmail(@RequestBody EmailRequest message) {
        log.info("[Controller] /send → topic: '{}' | to: {}", message.getSubject(), message.getToEmail());
        log.info("EmailRequest ----->"+message);
        try {
			log.info("[Kafka Consmer Agent] Cunsumed Message -- "+ message);
			log.info("[Kafka Consmer Agent] Cunsumed Message -- To: {} | Subject: '{}'",message.getToEmail(), message.getSubject());
			EmailResponse response = orchestratorAgent.process(message);
			log.info("[Kafka Consmer Agent] Message Response -- To: {} | Subject: '{}'",message.getToEmail(), message.getSubject());
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        return ResponseEntity.ok("Email sent......");
    }
}
