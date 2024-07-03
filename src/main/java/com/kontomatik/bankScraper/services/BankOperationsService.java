package com.kontomatik.bankScraper.services;

import com.kontomatik.bankScraper.models.Account;
import com.kontomatik.bankScraper.models.Credentials;

import java.util.List;

public interface BankOperationsService {
    List<Account> fetchAccountData(Credentials credentials);
}