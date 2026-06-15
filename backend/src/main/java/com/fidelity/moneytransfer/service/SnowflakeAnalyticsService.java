package com.fidelity.moneytransfer.service;

import java.math.BigDecimal;

public interface SnowflakeAnalyticsService {

    void recordTransaction(
            long fromAccountId,
            long toAccountId,
            BigDecimal amount,
            String status
    );
}
