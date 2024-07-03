package com.kontomatik.bankScraper.ui;

import com.kontomatik.bankScraper.models.Account;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsolePrinter {

    public void printAccountGroups(List<Account> accounts) {
        System.out.println(formatAccountGroups(accounts));
    }

    public String formatAccountGroups(List<Account> accounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Account account : accounts) {
            sb.append("Account Name: ").append(account.getName()).append("\n");
            sb.append("Account Number: ").append(account.getAccountNumber()).append("\n");
            sb.append("Balance: ").append(account.getBalance()).append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    public void notifyTwoFactorAuthStart() {
        System.out.println("Two-factor authentication process has started. Please check your device.");
    }

}
