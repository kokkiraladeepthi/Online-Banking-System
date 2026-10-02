package com.bank.mvp.soap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "accountNumber",
    "accountHolder",
    "currentBalance",
    "transactionDetails"
})
@XmlRootElement(name = "getStatementResponse", namespace = "http://com.bank.mvp/soap/statement")
public class GetStatementResponse {

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement", required = true)
    private String accountNumber;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement", required = true)
    private String accountHolder;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement", required = true)
    private BigDecimal currentBalance;

    @XmlElement(name = "transactionDetails", namespace = "http://com.bank.mvp/soap/statement")
    private List<SoapTransaction> transactionDetails = new ArrayList<>();

    public GetStatementResponse() {
    }

    public GetStatementResponse(String accountNumber, String accountHolder, BigDecimal currentBalance, List<SoapTransaction> transactionDetails) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.currentBalance = currentBalance;
        this.transactionDetails = transactionDetails != null ? transactionDetails : new ArrayList<>();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public List<SoapTransaction> getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(List<SoapTransaction> transactionDetails) {
        this.transactionDetails = transactionDetails;
    }
}

