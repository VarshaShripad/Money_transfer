package com.fidelity.moneytransfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fidelity.moneytransfer.enums.AccountStatus;

public class AccountResponse {

    private Long id;
    private String holderName;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime lastUpdated;

    // Default constructor
    public AccountResponse() {
    }

    // All-args constructor
    public AccountResponse(Long id, String holderName, BigDecimal balance, AccountStatus status, LocalDateTime lastUpdated) {
        this.id = id;
        this.holderName = holderName;
        this.balance = balance;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    // Static factory method to convert from entity
    public static AccountResponse fromEntity(com.fidelity.moneytransfer.domain.Account account) {
        return new AccountResponse(
            account.getId(),
            account.getHolderName(),
            account.getBalance(),
            account.getStatus(),
            account.getLastUpdated()
        );
    }
}
