package com.fireFly.SMS.model;

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
public class EmailRequest {

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
    private FileMessage resume;
    private FileMessage coverLetter;
    private String skills;
    private String JobTitle;
    private String headLineSkill;

    @Override
    public String toString() {
        return "EmailRequest{" +
                "toEmail='" + toEmail + '\'' +
                ", body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", myNumber='" + myNumber + '\'' +
                ", myName='" + myName + '\'' +
                ", toName='" + toName + '\'' +
                ", UUID='" + UUID + '\'' +
                ", resume=" + (resume != null) +
                ", coverLetter=" + (coverLetter != null) +
                ", skills='" + skills + '\'' +
                ", JobTitle='" + JobTitle + '\'' +
                ", headLineSkill='" + headLineSkill + '\'' +
                '}';
    }
}
