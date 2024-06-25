package com.kontomatik.bankScraper.cli;


import com.kontomatik.bankScraper.models.Account;
import com.kontomatik.bankScraper.models.AccountGroup;
import com.kontomatik.bankScraper.models.AccountGroups;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Scanner;

@Setter
@Component
public class UserInteraction {
    private Scanner scanner;

    public UserInteraction() {
        this.scanner = new Scanner(System.in);
    }

    public HashMap<String, String> getCredentials() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);
        return credentials;
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
