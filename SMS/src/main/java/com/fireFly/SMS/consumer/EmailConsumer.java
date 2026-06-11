package com.fireFly.SMS.consumer;

import com.fireFly.SMS.service.EmailProcessorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
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

	private final EmailProcessorService processorService;


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
	public void consume(EmailRequest message, Acknowledgment ack) {
		try {
			log.info("{}[Kafka Consumer Agent] Consumed Message uuid -- ", message.getUUID());
			log.info("{}[Kafka Consumer Agent] Consumed Message -- ", message);

			// ACK immediately — tells Kafka "I received this, don't redeliver"
			ack.acknowledge();

			// Then process async — fire and forget
			processorService.processAsync(message);

		} catch (Exception e) {
			log.error("{}[Kafka Consumer Agent] Error acknowledging message", message.getUUID(), e);
			ack.acknowledge(); // still ack to avoid infinite retry loop
		}
	}
}