package com.fidelity.moneytransfer.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.domain.TransactionLog;
import com.fidelity.moneytransfer.exception.AccessDeniedException;
import com.fidelity.moneytransfer.exception.AccountNotFoundException;
import com.fidelity.moneytransfer.repository.AccountRepository;
import com.fidelity.moneytransfer.repository.TransactionLogRepository;
import com.fidelity.moneytransfer.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;

    public AccountServiceImpl(AccountRepository accountRepository,
                              TransactionLogRepository transactionLogRepository) {
        this.accountRepository = accountRepository;
        this.transactionLogRepository = transactionLogRepository;
    }

    @Override
    public Account getAccount(Long accountId, String requesterUsername, String requesterRole) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if ("ROLE_USER".equals(requesterRole) && !account.getUsername().equals(requesterUsername)) {
            throw new AccessDeniedException("Users can only access their own account");
        }

        return account;
    }

    @Override
    public BigDecimal getBalance(Long accountId, String requesterUsername, String requesterRole) {
        return getAccount(accountId, requesterUsername, requesterRole).getBalance();
    }

    @Override
    public List<TransactionLog> getTransactions(Long accountId, String requesterUsername, String requesterRole) {
        // Verify access first
        getAccount(accountId, requesterUsername, requesterRole);

        return transactionLogRepository.findAll()
                .stream()
                .filter(t -> t.getFromAccountId().equals(accountId) 
                          || t.getToAccountId().equals(accountId))
                .toList();
    }
}
