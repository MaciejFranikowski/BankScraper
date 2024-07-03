package com.kontomatik.bankScraper.models;

import java.math.BigDecimal;

public class Account {
    private final String accountNumber;
    private final BigDecimal balance;
    private final String name;

    public Account(String accountNumber, BigDecimal balance, String name) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getName() {
        return name;
    }

}
