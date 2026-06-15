package com.fidelity.moneytransfer.service.impl;

import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.domain.TransactionLog;
import com.fidelity.moneytransfer.dto.TransferRequest;
import com.fidelity.moneytransfer.enums.TransactionStatus;
import com.fidelity.moneytransfer.exception.AccountNotFoundException;
import com.fidelity.moneytransfer.exception.DuplicateTransferException;
import com.fidelity.moneytransfer.exception.InsufficientBalanceException;
import com.fidelity.moneytransfer.repository.AccountRepository;
import com.fidelity.moneytransfer.repository.TransactionLogRepository;
import com.fidelity.moneytransfer.service.TransactionLogService;
import com.fidelity.moneytransfer.service.SnowflakeAnalyticsService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @Mock
    private SnowflakeAnalyticsService snowflakeAnalyticsService;

     @Mock
    private TransactionLogService transactionLogService;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Account fromAccount;
    private Account toAccount;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        fromAccount = mock(Account.class);
        toAccount = mock(Account.class);

        when(fromAccount.getId()).thenReturn(1L);
        when(toAccount.getId()).thenReturn(2L);

        when(fromAccount.getOwnerUsername()).thenReturn("user1");
        when(toAccount.getOwnerUsername()).thenReturn("user2");

        when(fromAccount.getBalance()).thenReturn(new BigDecimal("1000.00"));

        // IMPORTANT FIX
        when(fromAccount.isActive()).thenReturn(true);
        when(toAccount.isActive()).thenReturn(true);

        request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("200.00"));
        request.setIdempotencyKey("idem-123");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "user1",
                        "password",
                        List.of(() -> "ROLE_USER")
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testTransfer_Success() {
        when(transactionLogRepository.findByIdempotencyKey("idem-123"))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        TransactionLog savedLog = new TransactionLog(
                1L, 2L, request.getAmount(),
                TransactionStatus.SUCCESS, null, "idem-123"
        );

        when(transactionLogRepository.save(any(TransactionLog.class)))
                .thenReturn(savedLog);

        var response = transferService.transfer(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("200.00"), response.getAmount());

        verify(fromAccount).debit(new BigDecimal("200.00"));
        verify(toAccount).credit(new BigDecimal("200.00"));
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionLogRepository).save(any(TransactionLog.class));
    }

    @Test
    void testTransfer_DuplicateIdempotencyKey() {
        when(transactionLogRepository.findByIdempotencyKey("idem-123"))
                .thenReturn(Optional.of(mock(TransactionLog.class)));

        assertThrows(DuplicateTransferException.class,
                () -> transferService.transfer(request));
    }

    @Test
    void testTransfer_FromAccountNotFound() {
        when(transactionLogRepository.findByIdempotencyKey("idem-123"))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> transferService.transfer(request));
    }

    @Test
    void testTransfer_AccessDenied() {
        when(fromAccount.getOwnerUsername()).thenReturn("otherUser");

        when(transactionLogRepository.findByIdempotencyKey("idem-123"))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        assertThrows(AccessDeniedException.class,
                () -> transferService.transfer(request));
    }

    @Test
    void testTransfer_InsufficientBalance() {
        when(fromAccount.getBalance()).thenReturn(new BigDecimal("50.00"));

        when(transactionLogRepository.findByIdempotencyKey("idem-123"))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        assertThrows(InsufficientBalanceException.class,
                () -> transferService.transfer(request));
    }
}
