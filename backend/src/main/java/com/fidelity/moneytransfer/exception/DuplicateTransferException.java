package com.fidelity.moneytransfer.exception;

public class DuplicateTransferException extends MoneyTransferException {
    public DuplicateTransferException(String key) {
        super("Duplicate transfer detected for key: " + key);
    }
}
