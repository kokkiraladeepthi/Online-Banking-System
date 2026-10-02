package com.bank.mvp.dto;

import java.math.BigDecimal;

public class AccountResponse {

    private Long id;
    private String accountNumber;
    private String name;
    private String email;
    private BigDecimal balance;
    private Long userId;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String accountNumber, String name, String email, BigDecimal balance) {
        this(id, accountNumber, name, email, balance, null);
    }

    public AccountResponse(Long id, String accountNumber, String name, String email, BigDecimal balance, Long userId) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.name = name;
        this.email = email;
        this.balance = balance;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
