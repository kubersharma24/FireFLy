package com.emailagent.controller;

import com.emailagent.agent.EmailOrchestratorAgent;
import com.emailagent.agent.EmailProducer;
import com.emailagent.model.EmailRequest;
import com.emailagent.model.EmailRequestDTO;
import com.emailagent.model.EmailResponse;
import com.emailagent.model.EmailTemplateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailOrchestratorAgent orchestratorAgent;
    private final EmailProducer producer;

    private final RestTemplate rest = new RestTemplate();


    /**
     * POST /api/v1/email/send
     * <p>
     * Accepts multipart/form-data so files (resume, coverLetter) can be attached.
     * <p>
     * Form fields:
     * toEmail, description, topic, myNumber, myName, toName   (text)
     * resume       (file, optional)
     * coverLetter  (file, optional)
     */
    @PostMapping(
            value = "/getEmail",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmailResponse> generateEmail(
            @Valid @RequestBody EmailTemplateRequest request
    ) {

        log.info("[Controller] /getEmail → topic: '{}' | myname: {}",
                request.getTopic(),
                request.getMyName());

        EmailResponse response = orchestratorAgent.process(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }


    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email Agent is running ✅");
    }

    @PostMapping("/sendEmail")
    public String sendEmail(@RequestParam String message) {
        producer.sendEmailEvent(message);
        return "Message sent to Kafka topic email";
    }

//    @PostMapping(value = "/sendEmail",  
//    		consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//            )
//    public ResponseEntity<Object> sendEmail(@RequestBody EmailRequest request) {
//        log.info("[Controller] /send → topic: '{}' | to: {}", request.getSubject(), request.getToEmail());
//        log.info("EmailRequest ----->"+request);
//        	String uuid = UUID.randomUUID().toString();
//            log.info("UUID ----->"+uuid);
//            request.setUUID(uuid);
//            producer.sendEmailEvent(request);
//        return ResponseEntity.ok("Email sent......");
//    }

    @PostMapping(
            value = "/sendEmail",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> sendEmail(
            @Valid @ModelAttribute EmailRequestDTO request) {


        try {
            for(int i =0 ; i<request.getCount(); i++) {
                String uuid = UUID.randomUUID().toString();
                log.info("UUID ----->" + uuid);
                EmailRequest emailRequest = EmailRequest.builder()
                        .UUID(uuid)
                        .toEmail(request.getToEmail())
                        .body(request.getBody())
                        .subject(request.getSubject())
                        .myNumber(request.getMyNumber())
                        .myName(request.getMyName())
                        .toName(request.getToName())
                        .resume(orchestratorAgent.convertToFileMessage(request.getResume()))
                        .coverLetter(orchestratorAgent.convertToFileMessage(request.getCoverLetter()))
                        .skills(request.getSkills())
                        .headLineSkill(request.getHeadLineSkill())
                        .JobTitle(request.getJobTitle())
                        .build();

                log.info("Email request  -----> {}", emailRequest);
                producer.sendEmailEvent(emailRequest);
            }
            return ResponseEntity.ok("Email queued successfully");
        } catch (Exception e) {
            log.error("Error processing request", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to process request");
        }
    }

    @PostMapping(value = "/sendEmailwithTemplate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> sendEmailwithTemplate(@RequestBody EmailRequest request) {
        log.info("[Controller] /send → topic: '{}' | to: {}", request.getSubject(), request.getToEmail());
        log.info("EmailRequest ----->" + request);
        List<String> list = Arrays.asList("kubersharma562@gmail.com", "kubersharma5621@gmail.com", "kubersharma1515@gmail.com", "kubersharma1549@gmail.com", "sharmakuber562@gmail.com", "panerbasant987@gmail.com");
        for (int i = -1; i < list.size(); i++) {
            if (i != -1) {
                request.setToEmail(list.get(i));
            }
            String uuid = UUID.randomUUID().toString();
            log.info("UUID ----->" + uuid);
            request.setUUID(uuid);
            ResponseEntity<Object> obj = rest.postForEntity("http://localhost:8081/api/v1/email/sendEmail", request, null);
        }
        return ResponseEntity.ok("Email sent......");
    }
}
