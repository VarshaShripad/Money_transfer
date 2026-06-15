package com.fidelity.moneytransfer.exception;

public class AccountNotActiveException extends MoneyTransferException {
    public AccountNotActiveException(Long accountId) {
        super("Account not active: " + accountId);
    }
}
