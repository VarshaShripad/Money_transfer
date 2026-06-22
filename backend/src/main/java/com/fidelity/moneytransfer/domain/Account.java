package com.fidelity.moneytransfer.domain;

import com.fidelity.moneytransfer.enums.AccountStatus;
import com.fidelity.moneytransfer.exception.AccountNotActiveException;
import com.fidelity.moneytransfer.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String holderName;

    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Version
    private Integer version;

    private LocalDateTime lastUpdated;

    @Column(nullable = false, unique = true)
    private String username; // owner username

    public Account() {}

    public Account(String holderName,
                   BigDecimal balance,
                   AccountStatus status,
                   String username) {
        this.holderName = holderName;
        this.balance = balance;
        this.status = status;
        this.username = username;
    }

    // ---------- Lifecycle ----------
    @PrePersist
    public void onCreate() {
        this.lastUpdated = LocalDateTime.now();
    }

    // ---------- Getters ----------
    public Long getId() { return id; }
    public String getHolderName() { return holderName; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Integer getVersion() { return version; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public String getUsername() { return username; }

    // For ownership checks in services
    public String getOwnerUsername() { return username; }

    // ---------- Setters ----------
    public void setId(Long id) { this.id = id; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public void setUsername(String username) { this.username = username; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    // ---------- Business Logic ----------
    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public void debit(BigDecimal amount) {
        if (!isActive()) {
            throw new AccountNotActiveException(this.id);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.balance = this.balance.subtract(amount);
        this.lastUpdated = LocalDateTime.now();
    }

    public void credit(BigDecimal amount) {
        if (!isActive()) {
            throw new AccountNotActiveException(this.id);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.lastUpdated = LocalDateTime.now();
    }
}
