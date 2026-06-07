package com.fireFly.SMS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {

    private boolean success;
    private String message;
    private String generatedSubject;
    private String generatedBody;
    private String sentTo;
    private String error;

    public static EmailResponse success(String message, String subject, String body, String sentTo) {
        return EmailResponse.builder()
                .success(true)
                .message(message)
                .generatedSubject(subject)
                .generatedBody(body)
                .sentTo(sentTo)
                .build();
    }

    public static EmailResponse failure(String error) {
        return EmailResponse.builder()
                .success(false)
                .error(error)
                .message("Email sending failed")
                .build();
    }
}
