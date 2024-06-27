package com.kontomatik.bankScraper.cli;

import com.kontomatik.bankScraper.models.Account;
import com.kontomatik.bankScraper.models.AccountGroup;
import com.kontomatik.bankScraper.models.AccountGroups;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class UserInteractionTest {
    private Scanner scanner;
    private UserInteraction userInteraction;

    @BeforeEach
    void setUp() {
        scanner = mock(Scanner.class);
        userInteraction = new UserInteraction();
        userInteraction.setScanner(scanner);
    }

    @Test
    void shouldInvokeScannerAndProperlyReadCredentials() {
        // given
        when(scanner.nextLine()).thenReturn("testUser", "testPass");

        // when
        HashMap<String, String> credentials = userInteraction.getCredentials();

        // then
        assertEquals("testUser", credentials.get("username"));
        assertEquals("testPass", credentials.get("password"));
        verify(scanner, times(2)).nextLine();
    }

    @Test
    void shouldProperlyFormatAccounts() {
        // given
        Account account1 = Account.builder()
                .name("Account1")
                .accountNumber("123456")
                .balance(new BigDecimal("1000.0"))
                .currency("USD")
                .customName("CustomAccount1")
                .build();

        Account account2 = Account.builder()
                .name("Account2")
                .accountNumber("654321")
                .balance(new BigDecimal("2000.0"))
                .currency("EUR")
                .customName("CustomAccount2")
                .build();
        List<Account> accounts1 = new ArrayList<>();
        accounts1.add(account1);
        List<Account> accounts2 = new ArrayList<>();
        accounts2.add(account2);

        AccountGroup group1 = new AccountGroup(accounts1);
        AccountGroup group2 = new AccountGroup(accounts2);

        List<AccountGroup> accountGroupsList = new ArrayList<>();
        accountGroupsList.add(group1);
        accountGroupsList.add(group2);

        AccountGroups accountGroups = new AccountGroups(accountGroupsList);

        String expectedOutput = "\n" +
                "Account Name: Account1\n" +
                "Account Number: 123456\n" +
                "Balance: 1000.0 USD\n" +
                "Custom Name: CustomAccount1\n" +
                "\n" +
                "Account Name: Account2\n" +
                "Account Number: 654321\n" +
                "Balance: 2000.0 EUR\n" +
                "Custom Name: CustomAccount2\n" +
                "\n";

        // when
        var result = userInteraction.formatAccountGroups(accountGroups);

        // Redirect the standard output to capture the print statements
        assertEquals(expectedOutput, result);
    }
}