package com.fidelity.moneytransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fidelity.moneytransfer.dto.TransferRequest;
import com.fidelity.moneytransfer.dto.TransferResponse;
import com.fidelity.moneytransfer.dto.EncryptedTransferRequest;
import com.fidelity.moneytransfer.security.CustomUserDetailsService;
import com.fidelity.moneytransfer.security.JwtFilter;
import com.fidelity.moneytransfer.security.EncryptionUtil;
import com.fidelity.moneytransfer.service.TransferService;
import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.repository.AccountRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TransferController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @MockBean
    private EncryptionUtil encryptionUtil;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1", authorities = "ROLE_USER")
    void transfer_success() throws Exception {

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(500));
        request.setIdempotencyKey("idem-123");

        TransferResponse response = new TransferResponse(
                "trx-1",
                "SUCCESS",
                "Transfer completed successfully",
                1L,
                2L,
                BigDecimal.valueOf(500)
        );

        when(transferService.transfer(any(TransferRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("trx-1"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "user1", authorities = "ROLE_USER")
    void transfer_validationFailure_shouldReturn422() throws Exception {

        TransferRequest invalidRequest = new TransferRequest();
        invalidRequest.setFromAccountId(1L);
        invalidRequest.setToAccountId(2L);
        invalidRequest.setAmount(BigDecimal.valueOf(-100));
        invalidRequest.setIdempotencyKey("idem-123");

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Encrypted transfer: should decrypt and process successfully")
    @WithMockUser(username = "user1", authorities = "ROLE_USER")
    void transferEncrypted_success() throws Exception {
        // Setup
        Long toAccountId = 2L;
        BigDecimal amount = BigDecimal.valueOf(500);
        String idempotencyKey = "idem-456";

        EncryptedTransferRequest encryptedRequest = new EncryptedTransferRequest(
                "encryptedToAccountId123",
                "encryptedAmount456",
                idempotencyKey
        );

        // Mock Account lookup
        Account senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setUsername("user1");
        when(accountRepository.findByUsername("user1"))
                .thenReturn(Optional.of(senderAccount));

        // Mock decryption
        when(encryptionUtil.decryptLong("encryptedToAccountId123"))
                .thenReturn(toAccountId);
        when(encryptionUtil.decrypt("encryptedAmount456"))
                .thenReturn("500");

        // Mock transfer service
        TransferResponse response = new TransferResponse(
                "trx-2",
                "SUCCESS",
                "Secure transfer completed",
                1L,
                2L,
                amount
        );
        when(transferService.transfer(any(TransferRequest.class)))
                .thenReturn(response);

        // Test
        mockMvc.perform(post("/api/v1/transfers/encrypted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(encryptedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("trx-2"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Secure transfer completed"));
    }

    @Test
    @DisplayName("Encrypted transfer: should reject invalid request")
    @WithMockUser(username = "user1", authorities = "ROLE_USER")
    void transferEncrypted_invalidRequest_shouldReturn422() throws Exception {
        // Missing encryptedAmount
        EncryptedTransferRequest invalidRequest = new EncryptedTransferRequest(
                "encryptedToAccountId123",
                "",  // Empty encrypted amount
                "idem-789"
        );

        mockMvc.perform(post("/api/v1/transfers/encrypted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Encrypted transfer: should handle decryption failure gracefully")
    @WithMockUser(username = "user1", authorities = "ROLE_USER")
    void transferEncrypted_decryptionFailure() throws Exception {
        EncryptedTransferRequest encryptedRequest = new EncryptedTransferRequest(
                "invalid-encrypted-data",
                "invalid-encrypted-amount",
                "idem-999"
        );

        Account senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setUsername("user1");
        when(accountRepository.findByUsername("user1"))
                .thenReturn(Optional.of(senderAccount));

        when(encryptionUtil.decryptLong("invalid-encrypted-data"))
                .thenThrow(new RuntimeException("Decryption failed"));

        mockMvc.perform(post("/api/v1/transfers/encrypted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(encryptedRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Encrypted transfer: should handle missing sender account")
    @WithMockUser(username = "unknownUser", authorities = "ROLE_USER")
    void transferEncrypted_senderAccountNotFound() throws Exception {
        EncryptedTransferRequest encryptedRequest = new EncryptedTransferRequest(
                "encryptedToAccountId123",
                "encryptedAmount456",
                "idem-111"
        );

        when(accountRepository.findByUsername("unknownUser"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/transfers/encrypted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(encryptedRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Encrypted transfer: should handle large amounts")
    @WithMockUser(username = "user1", authorities = "ROLE_USER")
    void transferEncrypted_largeAmount() throws Exception {
        Long toAccountId = 999L;
        BigDecimal largeAmount = BigDecimal.valueOf(999999999.99);
        String idempotencyKey = "idem-large-amount";

        EncryptedTransferRequest encryptedRequest = new EncryptedTransferRequest(
                "encryptedToAccountId999",
                "encryptedAmount999999999",
                idempotencyKey
        );

        Account senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setUsername("user1");
        when(accountRepository.findByUsername("user1"))
                .thenReturn(Optional.of(senderAccount));

        when(encryptionUtil.decryptLong("encryptedToAccountId999"))
                .thenReturn(toAccountId);
        when(encryptionUtil.decrypt("encryptedAmount999999999"))
                .thenReturn("999999999.99");

        TransferResponse response = new TransferResponse(
                "trx-large",
                "SUCCESS",
                "Large transfer completed",
                1L,
                999L,
                largeAmount
        );
        when(transferService.transfer(any(TransferRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers/encrypted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(encryptedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

