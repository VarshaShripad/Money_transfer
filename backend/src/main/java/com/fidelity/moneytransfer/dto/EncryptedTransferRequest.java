package com.fidelity.moneytransfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EncryptedTransferRequest {

    @NotBlank(message = "Encrypted toAccountId is required")
    private String encryptedToAccountId;

    @NotBlank(message = "Encrypted amount is required")
    private String encryptedAmount;

    @NotNull
    private String idempotencyKey;

    public EncryptedTransferRequest() {
    }

    public EncryptedTransferRequest(String encryptedToAccountId, String encryptedAmount, String idempotencyKey) {
        this.encryptedToAccountId = encryptedToAccountId;
        this.encryptedAmount = encryptedAmount;
        this.idempotencyKey = idempotencyKey;
    }

    // Getters & Setters
    public String getEncryptedToAccountId() {
        return encryptedToAccountId;
    }

    public void setEncryptedToAccountId(String encryptedToAccountId) {
        this.encryptedToAccountId = encryptedToAccountId;
    }

    public String getEncryptedAmount() {
        return encryptedAmount;
    }

    public void setEncryptedAmount(String encryptedAmount) {
        this.encryptedAmount = encryptedAmount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
