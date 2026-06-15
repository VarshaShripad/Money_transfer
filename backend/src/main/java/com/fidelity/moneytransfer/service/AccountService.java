package com.fidelity.moneytransfer.service;

import java.math.BigDecimal;
import java.util.List;

import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.domain.TransactionLog;

public interface AccountService {

    Account getAccount(Long accountId, String requesterUsername, String requesterRole);

    BigDecimal getBalance(Long accountId, String requesterUsername, String requesterRole);

    List<TransactionLog> getTransactions(Long accountId, String requesterUsername, String requesterRole);
}
