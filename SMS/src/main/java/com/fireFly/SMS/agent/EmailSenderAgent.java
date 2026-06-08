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
                .replace("\\n", "\n")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");
        String skillsSection = buildSkillsSection(content);
        String attachmentButtons = buildAttachmentButtons(content);
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <title>%s</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { background-color: #f0f0f2; font-family: Arial, sans-serif; font-size: 15px; color: #1a1a1a; line-height: 1.7; }
                .wrapper { padding: 32px 16px; }
                .card { max-width: 680px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #e4e4e7; }
                .header { background: #1a1a2e; padding: 28px 36px 26px; }
                .avatar-row { margin-bottom: 20px; }
                .avatar-table { border-collapse: collapse; }
                .avatar-cell { width: 44px; height: 44px; background: rgba(255,255,255,0.15); border-radius: 50%%; text-align: center; vertical-align: middle; font-size: 14px; font-weight: bold; color: #ffffff; padding: 0; }
                .avatar-info-cell { padding-left: 12px; vertical-align: middle; }
                .sender-name { font-size: 14px; font-weight: bold; color: #ffffff; }
                .sender-title { font-size: 12px; color: rgba(255,255,255,0.6); margin-top: 2px; }
                .email-subject { font-size: 22px; font-weight: bold; color: #ffffff; line-height: 1.3; margin-bottom: 8px; }
                .email-tagline { font-size: 13px; color: rgba(255,255,255,0.6); }
                .body { padding: 32px 36px; width: 100%%; box-sizing: border-box; }
                .greeting { font-size: 15px; margin-bottom: 16px; color: #1a1a1a; }
                .body-text { font-size: 15px; color: #3f3f46; margin-top: 20px; margin-bottom: 0; }
                .skills-box { background: #f8f8f9; border-left: 3px solid #1a1a2e; border-radius: 0 8px 8px 0; padding: 16px 20px; margin: 0 0 8px 0; width: 100%%; box-sizing: border-box; }
                .skill-pill { font-size: 12px; padding: 5px 14px; background: #ffffff; border: 1px solid #d4d4d8; border-radius: 999px; color: #3f3f46; display: inline-block; white-space: nowrap; flex-shrink: 0; }
                .skills-label { font-size: 11px; font-weight: bold; color: #71717a; text-transform: uppercase; letter-spacing: 0.06em; margin-bottom: 12px; }
                .skills-wrap { display: flex; flex-wrap: wrap; gap: 8px; width: 100%%; box-sizing: border-box; }
                 .footer { border-top: 1px solid #e4e4e7; padding: 20px 36px; }
                .footer-table { width: 100%%; border-collapse: collapse; }
                .footer-name { font-size: 14px; font-weight: bold; color: #1a1a1a; }
                .footer-contact { font-size: 13px; color: #71717a; margin-top: 3px; }
                .btn-primary { font-size: 12px; padding: 8px 18px; background: #1a1a2e; color: #ffffff !important; border-radius: 7px; font-weight: bold; text-decoration: none; display: inline-block; }
                .btn-secondary { font-size: 12px; padding: 8px 18px; border: 1px solid #d4d4d8; color: #3f3f46 !important; border-radius: 7px; text-decoration: none; display: inline-block; margin-left: 8px; }
                @media (max-width: 500px) {
                    .wrapper { padding: 12px 6px; }
                    .header { padding: 20px; }
                    .body { padding: 20px; }
                    .footer { padding: 16px 20px; }
                    .email-subject { font-size: 18px; }
                    .footer-table td { display: block; width: 100%%; }
                    .footer-table td:last-child { padding-top: 14px; }
                    .btn-secondary { margin-left: 0; margin-top: 8px; }
                }
            </style>
        </head>
        <body>
        <div class="wrapper">
            <div class="card">
                <div class="header">
                    <div class="avatar-row">
                        <table class="avatar-table" cellpadding="0" cellspacing="0">
                            <tr>
                                <td class="avatar-cell">%s</td>
                                <td class="avatar-info-cell">
                                    <div class="sender-name">%s</div>
                                    <div class="sender-title">%s</div>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div class="email-subject">%s</div>
                    <div class="email-tagline">%s</div>
                </div>
                <div class="body">
                    <p class="greeting">Dear <strong>%s</strong>,</p>
                    %s
                    <div class="body-text">%s</div>
                </div>
                <div class="footer">
                    <table class="footer-table" cellpadding="0" cellspacing="0">
                        <tr>
                            <td>
                                <div sclass="footer-name">Warm regards,</div>
                                <div class="footer-name">%s</div>
                                <div class="footer-contact">%s</div>
                            </td>
                            <td style="text-align: right; vertical-align: middle;">
                                %s
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
        </body>
        </html>
        """.formatted(
                content.getSubject(),                // <title>
                getInitials(content.getMyName()),     // avatar initials
                content.getMyName(),                  // sender name
                content.getJobTitle(),                // sender title
                content.getSubject(),                 // email subject
                content.getHeadLineSkill(),           // tagline
                content.getToName(),                  // greeting name
                skillsSection,                        // ← BEFORE body text (fixes Gmail collapse)
                bodyHtml,                             // body text
                content.getMyName(),                  // footer name
                content.getMyNumber(),                // footer contact
                attachmentButtons                     // ← dynamic resume/cover letter buttons
        );
    }

    private String buildAttachmentButtons(EmailRequest content) {
        StringBuilder sb = new StringBuilder();
        boolean hasResume = content.getResume() != null && content.getResume().getFileName() != null;
        boolean hasCover  = content.getCoverLetter() != null && content.getCoverLetter().getFileName() != null;
        if (hasResume) {
            sb.append("<span class=\"btn-primary\">Resume attached ✓</span>");
        }
        if (hasCover) {
            if (hasResume) sb.append("&nbsp;&nbsp;");
            sb.append("<span class=\"btn-secondary\">Cover letter attached ✓</span>");
        }
        return sb.toString();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
    private String buildSkillTags(EmailRequest content) {
        if (content.getSkills() == null || content.getSkills().isBlank()) return null;
        String[] skills = content.getSkills().split(",");
        StringBuilder sb = new StringBuilder();
        for (String skill : skills) {
            String trimmed = skill.trim();
            if (!trimmed.isEmpty()) {
                sb.append("<span style=\"")
                        .append("display:inline-block;")
                        .append("white-space:nowrap;")
                        .append("font-size:13px;")
                        .append("font-family:Arial,sans-serif;")
                        .append("color:#3f3f46;")
                        .append("background:#ffffff;")
                        .append("border:1px solid #d4d4d8;")
                        .append("border-radius:999px;")
                        .append("padding:5px 14px;")
                        .append("margin:0 4px 8px 0;")  // ← bottom margin creates row gap when wrapping
                        .append("line-height:1.4;")
                        .append("vertical-align:middle;")
                        .append("\">")
                        .append(trimmed)
                        .append("</span>");
            }
        }
        return sb.toString();
    }

    private String buildSkillsSection(EmailRequest content) {
        String skills = buildSkillTags(content);
        if (skills == null || skills.isBlank()) return "";
        return
                "<div style=\"" +
                        "width:100%;" +
                        "box-sizing:border-box;" +
                        "border-left:3px solid #1a1a2e;" +
                        "border-radius:0 8px 8px 0;" +
                        "background:#f8f8f9;" +
                        "padding:16px 20px 8px 20px;" +  // ← reduced bottom padding since pills have margin-bottom
                        "margin:0 0 20px 0;" +
                        "\">" +
                        "<p style=\"" +
                        "font-size:11px;" +
                        "font-weight:bold;" +
                        "letter-spacing:0.08em;" +
                        "text-transform:uppercase;" +
                        "color:#71717a;" +
                        "margin:0 0 12px 0;" +
                        "font-family:Arial,sans-serif;" +
                        "\">Core Skills</p>" +
                        "<div style=\"" +
                        "font-size:0;" +      // ← removes inline-block whitespace gaps between pills
                        "line-height:0;" +
                        "width:100%;" +
                        "\">" +
                        skills +
                        "</div>" +
                        "</div>";
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
