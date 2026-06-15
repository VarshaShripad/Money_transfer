package com.fidelity.moneytransfer.dto;

public class AuthResponse {

    private String token;
    private Long accountId;

    public AuthResponse() {}

    public AuthResponse(String token, Long accountId) {
        this.token = token;
        this.accountId = accountId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
