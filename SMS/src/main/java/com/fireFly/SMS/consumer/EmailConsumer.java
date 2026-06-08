package com.fireFly.SMS.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fireFly.SMS.agent.EmailOrchestratorAgent;
import com.fireFly.SMS.model.EmailRequest;
import com.fireFly.SMS.model.EmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailConsumer {
	
	private final EmailOrchestratorAgent orchestratorAgent;

	@KafkaListener(
	        topics = "health",
	        containerFactory = "stringKafkaListenerContainerFactory"
	)
    public void consumerHb(String message) {
        System.out.println("HB : " + message);
    }
    
    @KafkaListener(
            topics = "email",
            containerFactory = "emailKafkaListenerContainerFactory"
    )
    public void consume(EmailRequest message) {
    	
    	try {
			log.info("{}[Kafka Consmer Agent] Cunsumed Message uuid -- ",message.getUUID());
			log.info("{}[Kafka Consmer Agent] Cunsumed Message -- ",message);
			log.info("{}[Kafka Consmer Agent] Cunsumed Message -- To: {} | Subject: '{}'",message.getUUID(), message.getToEmail(), message.getSubject());
			EmailResponse response = orchestratorAgent.process(message);
			log.info("{}[Kafka Consmer Agent] Message Response -- To: {} | Subject: '{}'",message.getUUID(), message.getToEmail(), message.getSubject());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}