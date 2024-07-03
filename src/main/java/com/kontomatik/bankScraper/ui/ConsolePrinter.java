package com.kontomatik.bankScraper.ui;

import com.kontomatik.bankScraper.mbank.models.Account;
import com.kontomatik.bankScraper.mbank.models.AccountGroup;
import com.kontomatik.bankScraper.mbank.models.AccountGroups;
import org.springframework.stereotype.Component;

@Component
public class ConsolePrinter {

    public void printAccountGroups(AccountGroups accountGroups) {
        System.out.println(formatAccountGroups(accountGroups));
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

}
