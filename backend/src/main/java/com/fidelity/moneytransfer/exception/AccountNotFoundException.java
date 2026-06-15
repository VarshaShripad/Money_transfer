package com.fidelity.moneytransfer.exception;

public class AccountNotFoundException extends MoneyTransferException {
    public AccountNotFoundException(Long accountId) {
        super("Account not found: " + accountId);
    }
}
