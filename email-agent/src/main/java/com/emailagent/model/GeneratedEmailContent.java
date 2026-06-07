package com.emailagent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedEmailContent {

    private String subject;
    private String body;
    private String recipientName;
    private String senderName;
}
