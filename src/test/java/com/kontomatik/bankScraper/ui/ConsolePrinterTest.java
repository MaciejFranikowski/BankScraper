package com.kontomatik.bankScraper.ui;

import com.kontomatik.bankScraper.mbank.models.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsolePrinterTest {

    private final ConsolePrinter consolePrinter = new ConsolePrinter();

    @Test
    void shouldProperlyFormatAccounts() {
        // given
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account(
                "123456",
                new BigDecimal("1000.0"),
                "USD",
                "Account1",
                "CustomAccount1")
        );
        accounts.add(new Account(
                "654321",
                new BigDecimal("2000.0"),
                "EUR",
                "Account2",
                "CustomAccount2")
        );

        String expectedOutput = """

                Account Name: Account1
                Account Number: 123456
                Balance: 1000.0 USD
                Custom Name: CustomAccount1

                Account Name: Account2
                Account Number: 654321
                Balance: 2000.0 EUR
                Custom Name: CustomAccount2

                """;

        // when
        var result = consolePrinter.formatAccountGroups(accounts);

        // then
        assertEquals(expectedOutput, result);
    }
}
