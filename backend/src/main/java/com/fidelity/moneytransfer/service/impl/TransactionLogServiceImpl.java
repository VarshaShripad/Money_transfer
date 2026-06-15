package com.fidelity.moneytransfer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fidelity.moneytransfer.domain.TransactionLog;
import com.fidelity.moneytransfer.repository.TransactionLogRepository;
import com.fidelity.moneytransfer.service.TransactionLogService;

@Service
public class TransactionLogServiceImpl implements TransactionLogService {

    private final TransactionLogRepository repository;

    public TransactionLogServiceImpl(TransactionLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(TransactionLog log) {
        repository.save(log);
    }
}
