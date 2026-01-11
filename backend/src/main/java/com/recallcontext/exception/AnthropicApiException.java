package com.recallcontext.exception;

public class AnthropicApiException extends RuntimeException {
    private final int statusCode;

    public AnthropicApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public AnthropicApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
