package com.fireFly.SMS.agent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.springframework.stereotype.Component;

import com.fireFly.SMS.model.EmailRequest;
import com.fireFly.SMS.model.EmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailOrchestratorAgent {

    private final EmailSenderAgent senderAgent;

    public EmailResponse process(EmailRequest request) {
        log.info("========================================");
        log.info("{}[Orchestrator] To: {} <{}>",request.getUUID() ,request.getToName(), request.getToEmail());
        log.info("========================================");

        try {
        	changeName(request);        	
            // Step 1: Send email with attachments
            log.info("{}[Orchestrator] Step 1 -- Sending email with attachments...",request.getUUID() );
            senderAgent.sendEmail(request.getToEmail(), request);
            log.info("{}[Orchestrator] Step 1 -- Email dispatched.",request.getUUID() );
            return EmailResponse.success(
                    "Email generated and sent successfully!",
                    request.getSubject(),
                    request.getBody(),
                    request.getToEmail()
            );

        } catch (Exception e) {
            log.error("{}[Orchestrator] -- Task failed: {}",request.getUUID() , e.getMessage(), e);
            return EmailResponse.failure(e.getMessage());
        }
    }
    
    private void changeName(EmailRequest request){
    	String body = request.getBody();
    	body = body.replace("{{RECIPIENT_NAME}}",request.getToName());
    	request.setBody(body); 
    }
    
    private void logToCsv(EmailRequest request, String status, String errorMessage) {
        String fileName = "email-log.csv";

        try (FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            File file = new File(fileName);

            // Header if file is empty
            if (file.length() == 0) {
                bw.write("ToName,ToEmail,Subject,Body,MyName,ToPhone,Status,Error\n");
            }

            bw.write(String.format(
                    "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    safe(request.getToName()),
                    safe(request.getToEmail()),
                    safe(request.getSubject()),
                    safe(request.getBody()),
                    safe(request.getMyName()),
                    safe(request.getMyNumber()),
                    status,
                    errorMessage == null ? "" : safe(errorMessage)
            ));

        } catch (Exception e) {
            log.error("{}CSV logging failed: {}",request.getUUID() , e.getMessage(), e);
        }
    }
    
    private String safe(String value) {
        if (value == null) return "";
        return value.replace("\"", "'").replace("\n", " ").replace("\r", " ");
    }
}
