package com.kontomatik.bankScraper.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@AllArgsConstructor
@Getter
public class Account {
    public String accountNumber;

    public BigDecimal balance;

    public String currency;

    public String name;

    public String customName;

}
