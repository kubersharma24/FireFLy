package com.emailagent.model;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDTO {
	@Email(message = "Invalid email address")
    @NotBlank(message = "Recipient email is required")
    private String toEmail;

    @NotBlank(message = "Body is required")
    private String body;

    @NotBlank(message = "subject is required")
    private String subject;

    private String myNumber;

    @NotBlank(message = "Sender name is required")
    private String myName;

    @NotBlank(message = "Recipient name is required")
    private String toName;
    
    @NotBlank(message = "UUID is required")
    private String UUID;
    
    @NotBlank(message = "resume is required")
    private MultipartFile resume;

    private MultipartFile coverLetter;

    @NotBlank(message = "skills is required to add skills section - Java, AWS, ")
    private String skills;

    @NotBlank(message = "jobTitle is required ex - Junior Java Developer")
    private String JobTitle;

    @NotBlank(message = "headLineSkill is required - JAVA - AWS - SPRING BOOT - AGENTIC AI")
    private String headLineSkill;
}
