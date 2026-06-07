package com.emailagent.agent;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.emailagent.model.EmailRequest;

@Service
public class EmailProducer {

    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final KafkaTemplate<String, EmailRequest> emailKafkaTemplate;

    public EmailProducer(
            @Qualifier("stringKafkaTemplate")
            KafkaTemplate<String, String> stringKafkaTemplate,

            @Qualifier("emailKafkaTemplate")
            KafkaTemplate<String, EmailRequest> emailKafkaTemplate) {

        this.stringKafkaTemplate = stringKafkaTemplate;
        this.emailKafkaTemplate = emailKafkaTemplate;
    }

    public void sendEmailEvent(String message) {
    	stringKafkaTemplate.send("email", message);
    }
    
    public void sendEmailEvent(EmailRequest message) {
    	emailKafkaTemplate.send("email", message);
    }
}