package com.bank.mvp.soap;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bank.mvp.exception.AccountNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.Transaction;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.TransactionRepository;

@DataJpaTest
@Import(StatementEndpoint.class)
@ActiveProfiles("test")
class StatementEndpointTest {

    @Autowired
    private StatementEndpoint statementEndpoint;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account account;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        account = accountRepository.save(new Account("ACC-SOAP-101", "Emma Watson", "emma@example.com", new BigDecimal("7500.00")));

        Transaction dep = new Transaction(account, "DEPOSIT", new BigDecimal("2000.00"), "SUCCESS");
        dep.setReceiverAccount(account.getAccountNumber());
        dep.setDescription("Salary Deposit");
        transactionRepository.save(dep);

        Transaction wth = new Transaction(account, "WITHDRAW", new BigDecimal("500.00"), "SUCCESS");
        wth.setSenderAccount(account.getAccountNumber());
        wth.setDescription("ATM Cash Withdrawal");
        transactionRepository.save(wth);
    }

    @Test
    void shouldGenerateStatementByAccountId() {
        GetStatementRequest request = new GetStatementRequest();
        request.setAccountId(account.getId());

        GetStatementResponse response = statementEndpoint.getStatement(request);

        assertNotNull(response);
        assertEquals("ACC-SOAP-101", response.getAccountNumber());
        assertEquals("Emma Watson", response.getAccountHolder());
        assertEquals(new BigDecimal("7500.00"), response.getCurrentBalance());
        assertEquals(2, response.getTransactionDetails().size());

        SoapTransaction tx1 = response.getTransactionDetails().get(0);
        assertNotNull(tx1.getId());
        assertNotNull(tx1.getType());
        assertNotNull(tx1.getAmount());
        assertNotNull(tx1.getStatus());
        assertFalse(tx1.getDate().isEmpty());
    }

    @Test
    void shouldGenerateStatementByAccountNumber() {
        GetStatementRequest request = new GetStatementRequest();
        request.setAccountNumber("ACC-SOAP-101");

        GetStatementResponse response = statementEndpoint.getStatement(request);

        assertNotNull(response);
        assertEquals("ACC-SOAP-101", response.getAccountNumber());
        assertEquals("Emma Watson", response.getAccountHolder());
        assertEquals(2, response.getTransactionDetails().size());
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        GetStatementRequest request = new GetStatementRequest();
        request.setAccountId(999999L);

        assertThrows(AccountNotFoundException.class, () -> statementEndpoint.getStatement(request));
    }
}

