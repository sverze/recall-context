package com.recallcontext.exception;

public class TranscriptProcessingException extends RuntimeException {
    public TranscriptProcessingException(String message) {
        super(message);
    }

    public TranscriptProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
