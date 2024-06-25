package com.kontomatik.bankScraper.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
@Getter
public class AccountGroup {
    public List<Account> accounts;
}
