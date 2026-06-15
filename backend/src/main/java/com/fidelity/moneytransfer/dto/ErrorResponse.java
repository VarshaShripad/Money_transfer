package com.fidelity.moneytransfer.dto;

public class ErrorResponse {

    private String errorCode;
    private String message;

    public ErrorResponse() {
        // required for Jackson
    }

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
