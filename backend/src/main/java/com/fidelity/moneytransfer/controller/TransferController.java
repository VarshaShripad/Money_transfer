package com.fidelity.moneytransfer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelity.moneytransfer.dto.TransferRequest;
import com.fidelity.moneytransfer.dto.TransferResponse;
import com.fidelity.moneytransfer.dto.EncryptedTransferRequest;
import com.fidelity.moneytransfer.service.TransferService;
import com.fidelity.moneytransfer.security.EncryptionUtil;
import com.fidelity.moneytransfer.repository.AccountRepository;
import com.fidelity.moneytransfer.domain.Account;

import jakarta.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;
    private final EncryptionUtil encryptionUtil;
    private final AccountRepository accountRepository;

    public TransferController(TransferService transferService, EncryptionUtil encryptionUtil, AccountRepository accountRepository) {
        this.transferService = transferService;
        this.encryptionUtil = encryptionUtil;
        this.accountRepository = accountRepository;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Secure transfer endpoint with encrypted toAccountId and amount
     * This prevents intermediate proxies or network monitors from seeing sensitive transfer details
     */
    @PostMapping("/encrypted")
    public ResponseEntity<TransferResponse> transferEncrypted(
            @Valid @RequestBody EncryptedTransferRequest encryptedRequest) {

        try {
            // Decrypt the sensitive fields
            Long toAccountId = encryptionUtil.decryptLong(encryptedRequest.getEncryptedToAccountId());
            BigDecimal amount = new BigDecimal(encryptionUtil.decrypt(encryptedRequest.getEncryptedAmount()));

            // Get the current authenticated user's account ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account senderAccount = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Sender account not found"));
            Long fromAccountId = senderAccount.getId();

            // Create a normal transfer request with decrypted values
            TransferRequest request = new TransferRequest();
            request.setFromAccountId(fromAccountId);
            request.setToAccountId(toAccountId);
            request.setAmount(amount);
            request.setIdempotencyKey(encryptedRequest.getIdempotencyKey());

            // Process the transfer
            TransferResponse response = transferService.transfer(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Decryption or transfer failed: " + e.getMessage(), e);
        }
    }
}
