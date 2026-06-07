package com.fireFly.SMS.globalexceptionHandler;
public class MissingAttachmentException extends RuntimeException {

    public MissingAttachmentException(String message) {
        super(message);
    }
}