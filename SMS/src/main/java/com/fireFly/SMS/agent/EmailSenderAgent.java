package com.fireFly.SMS.agent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fireFly.SMS.globalexceptionHandler.MissingAttachmentException;
import com.fireFly.SMS.model.EmailRequest;
import com.fireFly.SMS.model.FileMessage;
import com.fireFly.SMS.service.EmailLogService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Email Sender Agent - Sends HTML email with optional file attachments (resume, cover letter).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSenderAgent {
	
	@Autowired
	private EmailLogService emailLogService;
    private final JavaMailSender mailSender;

    public void sendEmail(String toEmail, EmailRequest request) {
        log.info("{}[Email Sender Agent] Preparing email → To: {} | Subject: '{}'",request.getUUID(),
                toEmail, request.getSubject());
        
        
        try {
        	validateAttachments(request);
            log.info("{}[Email Sender Agent] Creating Multipart File: {}",request.getUUID());
        	MultipartFile resume = getMultiParAttachement(request, "resume");
        	MultipartFile cover = getMultiParAttachement(request, "cover");
        	log.info("{}[Email Sender Agent] Multipart Files Created: {}",request.getUUID());
        	
        	log.info("{}[Email Sender Agent] Transaction Created: {}",request.getUUID());
        	emailLogService.createPendingLog(
                    toEmail,
                    request.getToName(),
                    request.getSubject(),
                    request
            );
        	
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // multipart=true is required for both HTML body and attachments
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(request.getSubject());
            helper.setText(formatHtmlBody(request), true);
            // Attach resume if provided
            attachFile(helper, resume, request.getResume().getFileName());

            // Attach cover letter if provided
            attachFile(helper, cover, request.getCoverLetter().getFileName());
            
            mailSender.send(mimeMessage);      
            emailLogService.markSuccess(request.getUUID());
            log.info("{}[Email Sender Agent] ✅ Email sent to: {}",request.getUUID(), toEmail);

        } catch (MessagingException | IOException e) {
            log.error("{}[Email Sender Agent] ❌ Failed to send email",request.getUUID(), e);
            emailLogService.markFailed(request.getUUID(), e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
    
    private MultipartFile getMultiParAttachement (EmailRequest request, String value) {
    	FileMessage fileMessage = null;
    	if(value.equalsIgnoreCase("resume")) {
    		fileMessage = request.getResume();
    	} else if (value.equalsIgnoreCase("cover")) {
    		fileMessage = request.getCoverLetter();
    	}
    	MultipartFile multipartFile =
    		    new MockMultipartFile(
    		        fileMessage.getFileName(),
    		        fileMessage.getOriginalFileName(),
    		        fileMessage.getContentType(),
    		        fileMessage.getData()
    		    );
    	return multipartFile;
    }

    // ─── Attachment Helper ────────────────────────────────────────────────────

    private void attachFile(MimeMessageHelper helper, MultipartFile file, String defaultBaseName)
            throws MessagingException, IOException {

        if (file == null || file.isEmpty()) {
            log.debug("[Email Sender Agent] No file provided for: {}", defaultBaseName);
            return;
        }

        String fileName = (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank())
                ? file.getOriginalFilename()
                : defaultBaseName + guessExtension(file.getContentType());

        helper.addAttachment(fileName, new ByteArrayResource(file.getBytes()));
        log.info("[Email Sender Agent] 📎 Attached: {} ({} bytes)", fileName, file.getSize());
    }
    
//    private void attachFile(MimeMessageHelper helper,
//            File file,
//            String defaultBaseName)
//            		 throws MessagingException, IOException {
//	
//		if (file == null || !file.exists() || !file.isFile()) {
//			log.debug("[Email Sender Agent] No valid file provided for: {}", defaultBaseName);
//			return;
//		}
//
//		String fileName = (file.getName() != null && !file.getName().isBlank()) ? file.getName()
//				: defaultBaseName + guessExtension(getContentType(file));
//			helper.addAttachment(fileName, file);
//			log.info("[Email Sender Agent] 📎 Attached: {} ({} bytes)", fileName, file.length());
//	}
    
    private String getContentType(File file) {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            log.warn("[Email Sender Agent] Could not detect content type for file: {}", file.getName());
            return "application/octet-stream";
        }
    }

    private String guessExtension(String contentType) {
        if (contentType == null) return "";
        return switch (contentType.toLowerCase()) {
            case "application/pdf"  -> ".pdf";
            case "application/msword",
                 "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            case "text/plain"       -> ".txt";
            default                 -> "";
        };
    }

    // ─── HTML Body ────────────────────────────────────────────────────────────

    private String formatHtmlBody(EmailRequest content) {
        String bodyHtml = content.getBody()
        		.replace("\\n", "\n")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body { font-family: Arial, sans-serif; font-size: 15px; color: #333; line-height: 1.6; }
                        .container { max-width: 600px; margin: 30px auto; padding: 24px; border: 1px solid #e0e0e0; border-radius: 8px; background: #fafafa; }
                        .footer { margin-top: 24px; font-size: 12px; color: #999; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        %s
                    </div>
                </body>
                </html>
                """.formatted(bodyHtml);
    }
        
    private void validateAttachments(EmailRequest request) {
	
		validateFile(request.getResume(), "Resume");
		validateFile(request.getCoverLetter(), "Cover letter");
	}
    
    private void validateFile(FileMessage file, String name) {

        if (file == null) {
            throw new MissingAttachmentException(name + " is missing");
        }

        if (file.getData() == null || file.getData().length == 0) {
            throw new MissingAttachmentException(name + " content is empty");
        }

        if (file.getOriginalFileName() == null
                || file.getOriginalFileName().isBlank()) {
            throw new MissingAttachmentException(name + " filename is missing");
        }
    }
}
