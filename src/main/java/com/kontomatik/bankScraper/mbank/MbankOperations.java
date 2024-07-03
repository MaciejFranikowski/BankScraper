package com.kontomatik.bankScraper.mbank;

import com.kontomatik.bankScraper.models.Account;
import com.kontomatik.bankScraper.models.Credentials;
import com.kontomatik.bankScraper.services.BankOperationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MbankOperations implements BankOperationsService {
    final MbankAuthentication authentication;
    final MbankScraper mbankScraper;

    @Autowired
    MbankOperations(MbankAuthentication authentication, MbankScraper mbankScraper) {
        this.authentication = authentication;
        this.mbankScraper = mbankScraper;
    }

    @Override
    public List<Account> fetchAccountData(Credentials credentials) {
        Cookies authenticatedCookies = authentication.authenticate(credentials);
        return mbankScraper.scrape(authenticatedCookies);
    }
}
