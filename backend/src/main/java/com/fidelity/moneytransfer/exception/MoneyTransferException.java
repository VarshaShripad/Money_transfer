package com.fidelity.moneytransfer.exception;

public abstract class MoneyTransferException extends RuntimeException {

    public MoneyTransferException(String message) {
        super(message);
    }
}
