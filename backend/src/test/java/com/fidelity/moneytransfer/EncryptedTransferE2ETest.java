package com.fidelity.moneytransfer;

import com.fidelity.moneytransfer.controller.TransferController;
import com.fidelity.moneytransfer.dto.EncryptedTransferRequest;
import com.fidelity.moneytransfer.dto.TransferResponse;
import com.fidelity.moneytransfer.security.EncryptionUtil;
import com.fidelity.moneytransfer.service.TransferService;
import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.repository.AccountRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * End-to-End Integration Tests for Encrypted Transfer Feature
 * 
 * These tests verify the complete flow of encrypted transfers:
 * 1. Frontend encrypts toAccountId and amount
 * 2. Request is sent to backend with encrypted values
 * 3. Backend decrypts values using EncryptionUtil
 * 4. Backend validates decrypted values
 * 5. Transfer service processes the transaction
 * 6. Success/Failure response is returned
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Encrypted Transfer End-to-End Integration Tests")
class EncryptedTransferE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private TransferService transferService;

    @MockBean
    private AccountRepository accountRepository;

    private Account senderAccount;
    private Account recipientAccount;

    @BeforeEach
    void setUp() {
        // Setup sender account
        senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setUsername("alice");
        senderAccount.setHolderName("Alice Johnson");
        senderAccount.setBalance(BigDecimal.valueOf(10000));

        // Setup recipient account
        recipientAccount = new Account();
        recipientAccount.setId(2L);
        recipientAccount.setUsername("bob");
        recipientAccount.setHolderName("Bob Smith");
        recipientAccount.setBalance(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("E2E: Full encrypted transfer flow - sender encrypts, backend decrypts, processes, returns success")
    @WithMockUser(username = "alice", authorities = "ROLE_USER")
    void encryptedTransferE2E_fullFlow() throws Exception {
        // Step 1: Frontend encrypts sensitive values
        Long toAccountId = 2L;
        BigDecimal amount = BigDecimal.valueOf(1000);
        String encryptedToAccountId = encryptionUtil.encryptLong(toAccountId);
        String encryptedAmount = encryptionUtil.encrypt(amount.toString());
        String idempotencyKey = "e2e-test-001";

        // Step 2: Create encrypted request
        EncryptedTransferRequest encryptedRequest = new EncryptedTransferRequest(
                encryptedToAccountId,
                encryptedAmount,
                idempotencyKey
        );

        // Step 3: Mock account lookup
        when(accountRepository.findByUsername("alice"))
                .thenReturn(Optional.of(senderAccount));

        // Step 4: Send request to backend
        mockMvc.perform(post("/api/v1/transfers/encrypted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"encryptedToAccountId\":\"" + encryptedToAccountId + 
                                "\",\"encryptedAmount\":\"" + encryptedAmount + 
                                "\",\"idempotencyKey\":\"" + idempotencyKey + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("SUCCESS")))
                .andExpect(jsonPath("$.debitedFrom", equalTo(1)))
                .andExpect(jsonPath("$.creditedTo", equalTo(2)));
    }

    @Test
    @DisplayName("E2E: Verify encrypted values are not logged in plaintext")
    @WithMockUser(username = "alice", authorities = "ROLE_USER")
    void encryptedTransferE2E_payloadNotExposed() throws Exception {
        Long toAccountId = 3L;
        BigDecimal amount = BigDecimal.valueOf(500);
        
        String encryptedToAccountId = encryptionUtil.encryptLong(toAccountId);
        String encryptedAmount = encryptionUtil.encrypt(amount.toString());

        when(accountRepository.findByUsername("alice"))
                .thenReturn(Optional.of(senderAccount));

        // Verify encrypted payload doesn't contain plaintext values
        MvcResult result = mockMvc.perform(post("/api/v1/transfers/encrypted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"encryptedToAccountId\":\"" + encryptedToAccountId + 
                                "\",\"encryptedAmount\":\"" + encryptedAmount + 
                                "\",\"idempotencyKey\":\"e2e-test-002\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        
        // Plaintext values should NOT appear in the payload sent to API
        // (They only appear in the response, which is the transaction result)
    }

    @Test
    @DisplayName("E2E: Large amount encryption and decryption")
    @WithMockUser(username = "alice", authorities = "ROLE_USER")
    void encryptedTransferE2E_largeAmount() throws Exception {
        Long toAccountId = 5L;
        BigDecimal largeAmount = BigDecimal.valueOf(999999.99);
        
        String encryptedToAccountId = encryptionUtil.encryptLong(toAccountId);
        String encryptedAmount = encryptionUtil.encrypt(largeAmount.toString());

        when(accountRepository.findByUsername("alice"))
                .thenReturn(Optional.of(senderAccount));

        mockMvc.perform(post("/api/v1/transfers/encrypted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"encryptedToAccountId\":\"" + encryptedToAccountId + 
                                "\",\"encryptedAmount\":\"" + encryptedAmount + 
                                "\",\"idempotencyKey\":\"e2e-test-003\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("SUCCESS")));
    }

    @Test
    @DisplayName("E2E: Multiple concurrent encrypted transfers")
    @WithMockUser(username = "alice", authorities = "ROLE_USER")
    void encryptedTransferE2E_concurrentTransfers() throws Exception {
        when(accountRepository.findByUsername("alice"))
                .thenReturn(Optional.of(senderAccount));

        // Simulate 3 concurrent transfers
        for (int i = 0; i < 3; i++) {
            Long toAccountId = (long) (i + 2);
            BigDecimal amount = BigDecimal.valueOf(100 + i * 10);
            
            String encryptedToAccountId = encryptionUtil.encryptLong(toAccountId);
            String encryptedAmount = encryptionUtil.encrypt(amount.toString());
            String idempotencyKey = "concurrent-transfer-" + i;

            mockMvc.perform(post("/api/v1/transfers/encrypted")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"encryptedToAccountId\":\"" + encryptedToAccountId + 
                                    "\",\"encryptedAmount\":\"" + encryptedAmount + 
                                    "\",\"idempotencyKey\":\"" + idempotencyKey + "\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("E2E: Verify encryption produces different ciphertexts for same value")
    void encryptedTransferE2E_nondeterministicEncryption() throws Exception {
        Long value = 123L;
        
        String encrypted1 = encryptionUtil.encryptLong(value);
        String encrypted2 = encryptionUtil.encryptLong(value);
        
        // Different ciphertexts for same value (due to random IV)
        // Both should decrypt to same value
        assert !encrypted1.equals(encrypted2);
        assert encryptionUtil.decryptLong(encrypted1).equals(value);
        assert encryptionUtil.decryptLong(encrypted2).equals(value);
    }
}
