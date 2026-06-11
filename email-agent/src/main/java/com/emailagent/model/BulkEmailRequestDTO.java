package com.emailagent.model;

import com.emailagent.validator.ValidMultipartFile;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailRequestDTO {

    @NotBlank(message = "Body is required")
    private String body;

    @NotBlank(message = "subject is required")
    private String subject;

    private String myNumber;

    @NotBlank(message = "Sender name is required")
    private String myName;

    private String UUID;

    @ValidMultipartFile(message = "Resume is required")
    private MultipartFile resume;

    private MultipartFile coverLetter;

    @NotBlank(message = "skills is required to add skills section - Java, AWS, ")
    private String skills;

    @NotBlank(message = "jobTitle is required ex - Junior Java Developer")
    private String JobTitle;

    @NotBlank(message = "headLineSkill is required - JAVA - AWS - SPRING BOOT - AGENTIC AI")
    private String headLineSkill;

    int pageNo = 0;
}
