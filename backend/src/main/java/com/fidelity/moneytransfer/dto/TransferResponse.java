package com.fidelity.moneytransfer.dto;

import java.math.BigDecimal;

public class TransferResponse {

    private String transactionId;
    private String status;
    private String message;
    private Long debitedFrom;
    private Long creditedTo;
    private BigDecimal amount;

    public TransferResponse() {
    }

    public TransferResponse(String transactionId,
                            String status,
                            String message,
                            Long debitedFrom,
                            Long creditedTo,
                            BigDecimal amount) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.debitedFrom = debitedFrom;
        this.creditedTo = creditedTo;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Long getDebitedFrom() {
        return debitedFrom;
    }

    public Long getCreditedTo() {
        return creditedTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
