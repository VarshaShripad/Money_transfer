package com.fidelity.moneytransfer.service.impl;

import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.domain.TransactionLog;
import com.fidelity.moneytransfer.exception.AccessDeniedException;
import com.fidelity.moneytransfer.exception.AccountNotFoundException;
import com.fidelity.moneytransfer.repository.AccountRepository;
import com.fidelity.moneytransfer.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        account = mock(Account.class);
        when(account.getId()).thenReturn(1L);
        when(account.getUsername()).thenReturn("user1");
        when(account.getBalance()).thenReturn(new BigDecimal("1000.00"));
    }

    @Test
    void testGetAccount_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Account result = accountService.getAccount(1L, "user1", "ROLE_USER");

        assertEquals(1L, result.getId());
    }

    @Test
    void testGetAccount_NotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccount(1L, "user1", "ROLE_USER"));
    }

    @Test
    void testGetAccount_AccessDenied() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(AccessDeniedException.class,
                () -> accountService.getAccount(1L, "otherUser", "ROLE_USER"));
    }

    @Test
    void testGetBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(1L, "user1", "ROLE_USER");

        assertEquals(new BigDecimal("1000.00"), balance);
    }

    @Test
    void testGetTransactions() {
        TransactionLog t1 = new TransactionLog(1L, 2L,
                new BigDecimal("100.00"), null, null, "key1");
        TransactionLog t2 = new TransactionLog(3L, 1L,
                new BigDecimal("50.00"), null, null, "key2");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionLogRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TransactionLog> result =
                accountService.getTransactions(1L, "user1", "ROLE_USER");

        assertEquals(2, result.size());
    }
}
