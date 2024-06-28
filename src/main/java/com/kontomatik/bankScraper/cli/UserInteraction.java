package com.kontomatik.bankScraper.cli;

import com.kontomatik.bankScraper.models.Account;
import com.kontomatik.bankScraper.models.AccountGroup;
import com.kontomatik.bankScraper.models.AccountGroups;
import com.kontomatik.bankScraper.models.Credentials;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Scanner;

@Setter
@Component
public class UserInteraction {
    private Scanner scanner;
    private final Console console;

    public UserInteraction() {
        this.scanner = new Scanner(System.in);
        this.console = System.console();
    }

    public Credentials getCredentials() {
        if (console != null) {
            return getCredentialsFromConsole(console);
        } else {
            return getCredentialsFromScanner();
        }
    }

    private Credentials getCredentialsFromConsole(Console console) {
        String username = console.readLine("Enter your username: ");
        String password = new String(console.readPassword("Enter your password: "));
        return new Credentials(username, password);
    }

    private Credentials getCredentialsFromScanner() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        return new Credentials(username, password);
    }

    public String formatAccountGroups(AccountGroups accountGroups) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (AccountGroup group : accountGroups.accountGroups) {
            for (Account account : group.accounts) {
                sb.append("Account Name: ").append(account.name).append("\n");
                sb.append("Account Number: ").append(account.accountNumber).append("\n");
                sb.append("Balance: ").append(account.balance).append(" ").append(account.currency).append("\n");
                sb.append("Custom Name: ").append(account.customName).append("\n");
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
