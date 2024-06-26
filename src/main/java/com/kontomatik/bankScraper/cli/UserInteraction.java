package com.kontomatik.bankScraper.cli;

import com.kontomatik.bankScraper.exceptions.InvalidCredentials;
import com.kontomatik.bankScraper.mbank.models.Account;
import com.kontomatik.bankScraper.mbank.models.AccountGroup;
import com.kontomatik.bankScraper.mbank.models.AccountGroups;
import com.kontomatik.bankScraper.models.Credentials;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Scanner;

@Component
public class UserInteraction {
    private final Scanner scanner;
    private final Console console;

    public UserInteraction() {
        this(new Scanner(System.in), System.console());
    }

    public UserInteraction(Scanner scanner) {
        this(scanner, System.console());
    }

    public UserInteraction(Scanner scanner, Console console) {
        this.scanner = scanner;
        this.console = console;
    }

    public Credentials getCredentials() throws InvalidCredentials {
        if (isConsoleAvailable()) {
            return getCredentialsFromConsole(console);
        } else {
            return getCredentialsFromScanner();
        }
    }


    private Credentials getCredentialsFromConsole(Console console) throws InvalidCredentials {
        String username = console.readLine("Enter your username: ");
        String password = new String(console.readPassword("Enter your password: "));
        if (validateCredentials(username, password)) {
            throw new InvalidCredentials("Username or password cannot be empty.");
        }
        return new Credentials(username, password);
    }

    private Credentials getCredentialsFromScanner() throws InvalidCredentials {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        if (validateCredentials(username, password)) {
            throw new InvalidCredentials("Username or password cannot be empty.");
        }
        return new Credentials(username, password);
    }

    private boolean validateCredentials(String username, String password) {
        return username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty();
    }

    public String formatAccountGroups(AccountGroups accountGroups) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (AccountGroup group : accountGroups.accountGroups()) {
            for (Account account : group.accounts()) {
                sb.append("Account Name: ").append(account.name()).append("\n");
                sb.append("Account Number: ").append(account.accountNumber()).append("\n");
                sb.append("Balance: ").append(account.balance()).append(" ").append(account.currency()).append("\n");
                sb.append("Custom Name: ").append(account.customName()).append("\n");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void notifyTwoFactorAuthStart() {
        System.out.println("Two-factor authentication process has started. Please check your device.");
    }

    public boolean isConsoleAvailable() {
        return console != null;
    }
}
