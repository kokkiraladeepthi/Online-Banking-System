package com.bank.mvp.controller;

import com.bank.mvp.dto.AccountResponse;
import com.bank.mvp.dto.CreateAccountRequest;
import com.bank.mvp.dto.MoneyRequest;
import com.bank.mvp.dto.TransactionResponse;
import com.bank.mvp.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<AccountResponse> deposit(@PathVariable Long id, @Valid @RequestBody MoneyRequest request) {
        return ResponseEntity.ok(accountService.deposit(id, request));
    }

    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable Long id, @Valid @RequestBody MoneyRequest request) {
        return ResponseEntity.ok(accountService.withdraw(id, request));
    }

    @GetMapping("/transactions/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getTransactionHistory(accountId));
    }
}
