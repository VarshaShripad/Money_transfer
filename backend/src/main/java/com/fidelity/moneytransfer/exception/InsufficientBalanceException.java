package com.fidelity.moneytransfer.exception;

public class InsufficientBalanceException extends MoneyTransferException {
    public InsufficientBalanceException() {
        super("Insufficient balance");
    }
}
