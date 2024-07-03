package com.kontomatik.bankScraper;

import com.kontomatik.bankScraper.exceptions.InvalidCredentials;
import com.kontomatik.bankScraper.models.Account;
import com.kontomatik.bankScraper.models.Credentials;
import com.kontomatik.bankScraper.services.BankOperationsService;
import com.kontomatik.bankScraper.ui.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.Console;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class BankScraperApplication implements CommandLineRunner {

    private final ConsolePrinter consolePrinter;
    private final UserInputHandler userInputHandler;
    private final BankOperationsService bankOperationsService;

    public static void main(String[] args) {
        SpringApplication.run(BankScraperApplication.class, args);
    }

    public BankScraperApplication(@Qualifier("mbankOperations") BankOperationsService bankOperationsService,
                                  ConsolePrinter consolePrinter,
                                  UserInputHandler userInputHandler) {
        this.bankOperationsService = bankOperationsService;
        this.consolePrinter = consolePrinter;
        this.userInputHandler = userInputHandler;
    }

    @Override
    public void run(String... args) throws InvalidCredentials {
        Credentials credentials = userInputHandler.getCredentials();
        List<Account> accounts = bankOperationsService.fetchAccountData(credentials);
        consolePrinter.printAccountGroups(accounts);
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
