package com.fidelity.moneytransfer.service.impl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.domain.TransactionLog;
import com.fidelity.moneytransfer.dto.TransferRequest;
import com.fidelity.moneytransfer.dto.TransferResponse;
import com.fidelity.moneytransfer.enums.TransactionStatus;
import com.fidelity.moneytransfer.exception.AccountNotFoundException;
import com.fidelity.moneytransfer.exception.AccountNotActiveException;
import com.fidelity.moneytransfer.exception.DuplicateTransferException;
import com.fidelity.moneytransfer.exception.InsufficientBalanceException;
import com.fidelity.moneytransfer.exception.SameAccountTransferException;
import com.fidelity.moneytransfer.repository.AccountRepository;
import com.fidelity.moneytransfer.repository.TransactionLogRepository;
import com.fidelity.moneytransfer.service.TransactionLogService;
import com.fidelity.moneytransfer.service.TransferService;
import com.fidelity.moneytransfer.service.SnowflakeAnalyticsService;
import com.fidelity.moneytransfer.service.RewardService;

@Service
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final TransactionLogService transactionLogService;
    private final SnowflakeAnalyticsService snowflakeAnalyticsService;
    private final RewardService rewardService;

    public TransferServiceImpl(AccountRepository accountRepository,
                               TransactionLogRepository transactionLogRepository,
                               TransactionLogService transactionLogService,
                               SnowflakeAnalyticsService snowflakeAnalyticsService,
                               RewardService rewardService) {
        this.accountRepository = accountRepository;
        this.transactionLogRepository = transactionLogRepository;
        this.transactionLogService = transactionLogService;
        this.snowflakeAnalyticsService = snowflakeAnalyticsService;
        this.rewardService = rewardService;
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        // Idempotency check
        transactionLogRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .ifPresent(t -> {
                    throw new DuplicateTransferException(request.getIdempotencyKey());
                });

        // Fetch accounts
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getFromAccountId()));

        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getToAccountId()));

        try {
            // Validate transfer rules
            validateTransfer(request, fromAccount, toAccount);

            // Execute transfer
            fromAccount.debit(request.getAmount());
            toAccount.credit(request.getAmount());

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // SUCCESS log
            TransactionLog successLog = new TransactionLog(
                    fromAccount.getId(),
                    toAccount.getId(),
                    request.getAmount(),
                    TransactionStatus.SUCCESS,
                    null,
                    request.getIdempotencyKey()
            );

            transactionLogRepository.save(successLog);

            // ✅ Snowflake analytics push (Integrated from Code 1)
            snowflakeAnalyticsService.recordTransaction(
                    fromAccount.getId(),
                    toAccount.getId(),
                    request.getAmount(),
                    "SUCCESS"
            );

            // ✅ Award rewards for eligible transactions
            rewardService.awardRewards(successLog, fromAccount.getId(), toAccount.getId());

            return new TransferResponse(
                    successLog.getId(),
                    "SUCCESS",
                    "Transfer completed successfully",
                    fromAccount.getId(),
                    toAccount.getId(),
                    request.getAmount()
            );

        } catch (SameAccountTransferException |
                 InsufficientBalanceException |
                 AccountNotActiveException ex) {

            // FAILED log (separate transaction)
            TransactionLog failedLog = new TransactionLog(
                    fromAccount.getId(),
                    toAccount.getId(),
                    request.getAmount(),
                    TransactionStatus.FAILED,
                    ex.getMessage(),
                    request.getIdempotencyKey()
            );

            transactionLogService.log(failedLog);
            throw ex;
        }
    }

    /**
     * Validates authorization and business rules
     */
    private void validateTransfer(TransferRequest request,
                                  Account fromAccount,
                                  Account toAccount) {

        // Same account check
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new SameAccountTransferException(fromAccount.getId());
        }

        // Account status check
        if (!fromAccount.isActive()) {
            throw new AccountNotActiveException(fromAccount.getId());
        }

        if (!toAccount.isActive()) {
            throw new AccountNotActiveException(toAccount.getId());
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String currentUsername = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Ownership check (USER only)
        if (!isAdmin && !fromAccount.getOwnerUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You cannot transfer from another user's account");
        }

        // Balance check
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }
    }
}