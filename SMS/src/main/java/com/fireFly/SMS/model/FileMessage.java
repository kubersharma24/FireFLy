package com.fireFly.SMS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMessage {
    private String fileName;
    private String originalFileName;
    private String contentType;
    private byte[] data;
}