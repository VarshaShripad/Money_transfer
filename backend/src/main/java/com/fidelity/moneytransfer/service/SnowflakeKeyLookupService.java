package com.fidelity.moneytransfer.service;

import java.time.LocalDate;

public interface SnowflakeKeyLookupService {

    long getAccountKey(long accountId);

    int getDateKey(LocalDate date);
}
