package com.kontomatik.bankScraper.mbank;

import com.kontomatik.bankScraper.models.Account;

import java.util.List;

record AccountGroup(List<Account> accounts) {
}
