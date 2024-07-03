package com.kontomatik.bankScraper;

import com.kontomatik.bankScraper.exceptions.InvalidCredentials;
import com.kontomatik.bankScraper.mbank.models.AccountGroups;
import com.kontomatik.bankScraper.mbank.services.MbankAuthentication;
import com.kontomatik.bankScraper.mbank.services.MbankScraper;
import com.kontomatik.bankScraper.mbank.models.Cookies;
import com.kontomatik.bankScraper.models.Credentials;
import com.kontomatik.bankScraper.ui.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.Console;
import java.util.Scanner;

@SpringBootApplication
public class BankScraperApplication implements CommandLineRunner {

    private final MbankAuthentication authentication;
    private final MbankScraper mbankScraper;
    private final ConsolePrinter consolePrinter;
    private final UserInputHandler userInputHandler;

    public static void main(String[] args) {
        SpringApplication.run(BankScraperApplication.class, args);
    }

    public BankScraperApplication(MbankAuthentication authentication, MbankScraper mbankScraper, ConsolePrinter consolePrinter, UserInputHandler userInputHandler) {
        this.authentication = authentication;
        this.mbankScraper = mbankScraper;
        this.consolePrinter = consolePrinter;
        this.userInputHandler = userInputHandler;
    }

    @Override
    public void run(String... args) throws InvalidCredentials {
        Credentials credentials = userInputHandler.getCredentials();
        Cookies authenticatedCookies = authentication.authenticate(credentials);
        AccountGroups accountGroups = mbankScraper.scrape(authenticatedCookies);
        consolePrinter.printAccountGroups(accountGroups);
    }

    @Bean
    public static UserInput createUserInput() {
        Console console = System.console();
        if (console != null) {
            return new ConsoleUserInput(console);
        } else {
            return new ScannerUserInput(new Scanner(System.in));
        }
    }
}
