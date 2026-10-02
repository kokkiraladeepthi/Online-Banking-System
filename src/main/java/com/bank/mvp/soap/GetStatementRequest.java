package com.bank.mvp.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "accountId",
    "accountNumber"
})
@XmlRootElement(name = "getStatementRequest", namespace = "http://com.bank.mvp/soap/statement")
public class GetStatementRequest {

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private Long accountId;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private String accountNumber;

    public GetStatementRequest() {
    }

    public GetStatementRequest(Long accountId, String accountNumber) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}

