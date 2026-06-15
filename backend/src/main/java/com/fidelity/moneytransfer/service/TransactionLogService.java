package com.fidelity.moneytransfer.service;

import com.fidelity.moneytransfer.domain.TransactionLog;

public interface TransactionLogService {
    void log(TransactionLog log);
}
