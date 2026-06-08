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
            log.info("{}[Email Sender Agent] Creating Multipart File: ",request.getUUID());
        	MultipartFile resume = getMultiParAttachement(request, "resume");
        	MultipartFile cover = getMultiParAttachement(request, "cover");
        	log.info("{}[Email Sender Agent] Multipart Files Created: ",request.getUUID());
        	
        	log.info("{}[Email Sender Agent] Transaction Created: ",request.getUUID());
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
            log.info("{}[Email Sender Agent]  Email sent to: {}",request.getUUID(), toEmail);

        } catch (MessagingException | IOException e) {
            log.error("{}[Email Sender Agent] Failed to send email",request.getUUID(), e);
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
    
//    private String getContentType(File file) {
//        try {
//            return Files.probeContentType(file.toPath());
//        } catch (IOException e) {
//            log.warn("[Email Sender Agent] Could not detect content type for file: {}", file.getName());
//            return "application/octet-stream";
//        }
//    }

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
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");

        String skillsSection = buildSkillsSection(content);

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <title>%s</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { background-color: #f4f4f5; font-family: Arial, sans-serif; font-size: 15px; color: #1a1a1a; line-height: 1.7; }
                    .wrapper { padding: 32px 16px; }
                    .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #e4e4e7; }

                    .header { background: #1a1a2e; padding: 28px 32px 24px; }
                    .avatar-row { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
                    .avatar { width: 40px; height: 40px; border-radius: 50%%; background: rgba(255,255,255,0.15); display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: bold; color: #fff; flex-shrink: 0; }
                    .sender-name { font-size: 14px; font-weight: bold; color: #fff; }
                    .sender-title { font-size: 12px; color: rgba(255,255,255,0.6); margin-top: 2px; }
                    .email-subject { font-size: 20px; font-weight: bold; color: #fff; line-height: 1.3; }
                    .email-tagline { font-size: 13px; color: rgba(255,255,255,0.6); margin-top: 6px; }

                    .body { padding: 28px 32px; }
                    .greeting { font-size: 15px; margin-bottom: 16px; }
                    .body-text { font-size: 15px; color: #3f3f46; margin-bottom: 16px; }

                    .skills-box { background: #f8f8f9; border-left: 3px solid #1a1a2e; border-radius: 8px; padding: 16px 20px; margin: 20px 0; }
                    .skills-label { font-size: 11px; font-weight: bold; color: #71717a; text-transform: uppercase; letter-spacing: 0.06em; margin-bottom: 10px; }
                    .skills-wrap { display: flex; flex-wrap: wrap; gap: 8px; }
                    .skill-pill { font-size: 12px; padding: 4px 12px; background: #fff; border: 1px solid #e4e4e7; border-radius: 999px; color: #3f3f46; }

                    .footer { border-top: 1px solid #e4e4e7; padding: 20px 32px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
                    .footer-name { font-size: 14px; font-weight: bold; color: #1a1a1a; }
                    .footer-contact { font-size: 13px; color: #71717a; margin-top: 3px; }
                    .btn-group { display: flex; gap: 8px; flex-wrap: wrap; }
                    .btn-primary { font-size: 12px; padding: 7px 16px; background: #1a1a2e; color: #fff; border-radius: 7px; font-weight: bold; text-decoration: none; }
                    .btn-secondary { font-size: 12px; padding: 7px 16px; border: 1px solid #d4d4d8; color: #3f3f46; border-radius: 7px; text-decoration: none; }

                    @media (max-width: 480px) {
                        .wrapper { padding: 16px 8px; }
                        .header { padding: 20px; }
                        .body { padding: 20px; }
                        .footer { padding: 16px 20px; flex-direction: column; align-items: flex-start; }
                        .email-subject { font-size: 17px; }
                    }
                </style>
            </head>
            <body>
            <div class="wrapper">
                <div class="card">
                    <div class="header">
                        <div class="avatar-row">
                            <div class="avatar">%s</div>
                            <div>
                                <div class="sender-name">%s</div>
                                <div class="sender-title">%s</div>
                            </div>
                        </div>
                        <div class="email-subject">%s</div>
                        <div class="email-tagline">%s</div>
                    </div>
                    <div class="body">
                        <p class="greeting">Dear <strong>%s</strong>,</p>
                        <div class="body-text">%s</div>
                        <div class="skills-box">
                            <div class="skills-label">Core Skills</div>
                            <div class="skills-wrap">%s</div>
                        </div>
                    </div>
                    <div class="footer">
                        <div>
                            <div class="footer-name">%s</div>
                            <div class="footer-contact">%s</div>
                        </div>
                        <div class="btn-group">
                            <a class="btn-primary" href="#">View Resume</a>
                            <a class="btn-secondary" href="#">Cover Letter</a>
                        </div>
                    </div>
                </div>
            </div>
            </body>
            </html>
            """.formatted(
                content.getSubject(),           // <title>
                getInitials(content.getMyName()), // avatar initials
                content.getMyName(),             // sender name
                content.getJobTitle(),
                content.getSubject(),            // email subject
                content.getHeadLineSkill(),
                content.getToName(),             // greeting name
                bodyHtml,                        // body
                skillsSection,                          // skill pills
                content.getMyName(),             // footer name
                content.getMyNumber()            // footer contact
        );
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase() ;
    }

    private String buildSkillTags(EmailRequest content) {
        // Hardcoded for now — you can make this dynamic via EmailRequest later
        if(content.getSkills() == null){  return null;}
        String[] skills = content.getSkills().split(",");
        StringBuilder sb = new StringBuilder();
        for (String skill : skills) {
            sb.append("<span class=\"skill-pill\">").append(skill).append("</span>");
        }
        return sb.toString();
    }

    private String buildSkillsSection(EmailRequest content) {
        String skills = buildSkillTags(content);
        if (skills == null || skills.isBlank()) return "";

        return """
            <div class="skills-box">
                <div class="skills-label">Core Skills</div>
                <div class="skills-wrap">%s</div>
            </div>
            """.formatted(skills);
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
