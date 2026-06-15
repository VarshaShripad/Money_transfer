package com.fidelity.moneytransfer.domain;

import com.fidelity.moneytransfer.enums.AccountStatus;
import com.fidelity.moneytransfer.exception.AccountNotActiveException;
import com.fidelity.moneytransfer.exception.InsufficientBalanceException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AccountTest {

    @Test
    void testDebit_Success() {
        Account account = new Account();
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        account.debit(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("800.00"), account.getBalance());
    }

    @Test
    void testDebit_InsufficientBalance() {
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(AccountStatus.ACTIVE);

        assertThrows(InsufficientBalanceException.class, () -> {
            account.debit(new BigDecimal("200.00"));
        });
    }

    @Test
    void testCredit_Success() {
        Account account = new Account();
        account.setBalance(new BigDecimal("500.00"));
        account.setStatus(AccountStatus.ACTIVE);

        account.credit(new BigDecimal("300.00"));

        assertEquals(new BigDecimal("800.00"), account.getBalance());
    }

    @Test
    void testDebit_WhenAccountNotActive() {
        Account account = new Account();
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.LOCKED);

        assertThrows(AccountNotActiveException.class, () -> {
            account.debit(new BigDecimal("100.00"));
        });
    }

    @Test
    void testCredit_WhenAccountNotActive() {
        Account account = new Account();
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.CLOSED);

        assertThrows(AccountNotActiveException.class, () -> {
            account.credit(new BigDecimal("100.00"));
        });
    }
}
