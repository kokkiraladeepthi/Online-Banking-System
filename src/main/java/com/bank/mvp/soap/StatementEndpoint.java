package com.bank.mvp.soap;

import java.util.List;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.bank.mvp.exception.AccountNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.Transaction;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.TransactionRepository;

@Endpoint
public class StatementEndpoint {

    public static final String NAMESPACE_URI = "http://com.bank.mvp/soap/statement";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public StatementEndpoint(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getStatementRequest")
    @ResponsePayload
    public GetStatementResponse getStatement(@RequestPayload GetStatementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("SOAP statement request cannot be null");
        }

        Account account = null;

        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId()).orElse(null);
        }

        if (account == null && request.getAccountNumber() != null && !request.getAccountNumber().trim().isEmpty()) {
            account = accountRepository.findByAccountNumber(request.getAccountNumber().trim()).orElse(null);
        }

        if (account == null) {
            String identifier = request.getAccountId() != null ? "ID: " + request.getAccountId()
                    : "Number: " + request.getAccountNumber();
            throw new AccountNotFoundException("Account not found with provided " + identifier);
        }

        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());

        GetStatementResponse response = new GetStatementResponse();
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolder(account.getName());
        response.setCurrentBalance(account.getBalance());

        for (Transaction tx : transactions) {
            SoapTransaction st = new SoapTransaction();
            st.setId(tx.getId());
            st.setType(tx.getType());
            st.setAmount(tx.getAmount());
            st.setStatus(tx.getStatus());
            st.setDate(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : "");
            st.setSenderAccount(tx.getSenderAccount());
            st.setReceiverAccount(tx.getReceiverAccount());
            st.setDescription(tx.getDescription());
            response.getTransactionDetails().add(st);
        }

        return response;
    }
}

