package com.kontomatik.bankScraper;

import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.models.Cookies;
import com.kontomatik.bankScraper.mbank.services.MbankScraper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.kontomatik.bankScraper.mbank.services.MbankAuthentication;

@SpringBootApplication
public class BankScraperApplication implements CommandLineRunner {

    private final MbankAuthentication authentication;
    private final UserInteraction userInteraction;
    private final MbankScraper mbankScraper;

    public static void main(String[] args) {
        SpringApplication.run(BankScraperApplication.class, args);
    }

    public BankScraperApplication(MbankAuthentication authentication, UserInteraction userInteraction, MbankScraper mbankScraper) {
        this.authentication = authentication;
        this.userInteraction = userInteraction;
        this.mbankScraper = mbankScraper;
    }

    @Override
    public void run(String... args) {
        Cookies authenticatedCookies = authentication.authenticate(userInteraction.getCredentials());
        mbankScraper.scrape(authenticatedCookies);
    }

}
