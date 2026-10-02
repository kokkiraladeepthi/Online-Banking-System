package com.bank.mvp.soap;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "soapTransaction", namespace = "http://com.bank.mvp/soap/statement", propOrder = {
    "id",
    "type",
    "amount",
    "date",
    "status",
    "senderAccount",
    "receiverAccount",
    "description"
})
public class SoapTransaction {

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private Long id;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private String type;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private BigDecimal amount;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private String date;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private String status;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private String senderAccount;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private String receiverAccount;

    @XmlElement(namespace = "http://com.bank.mvp/soap/statement")
    private String description;

    public SoapTransaction() {
    }

    public SoapTransaction(Long id, String type, BigDecimal amount, String date, String status,
                           String senderAccount, String receiverAccount, String description) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

