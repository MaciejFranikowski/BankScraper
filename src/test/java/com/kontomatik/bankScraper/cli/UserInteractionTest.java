package com.kontomatik.bankScraper.cli;

import com.kontomatik.bankScraper.exceptions.InvalidCredentials;
import com.kontomatik.bankScraper.mbank.models.Account;
import com.kontomatik.bankScraper.mbank.models.AccountGroup;
import com.kontomatik.bankScraper.mbank.models.AccountGroups;
import com.kontomatik.bankScraper.models.Credentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Console;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserInteractionTest {
    private Scanner scanner;
    private Console console;

    private UserInteraction userInteraction;

    @BeforeEach
    void setUp() {
        scanner = mock(Scanner.class);
        console = mock(Console.class);
        userInteraction = spy(new UserInteraction(scanner, console));
    }

    @Test
    public void shouldThrowInvalidCredentialsWhenConsoleInputsAreEmpty() {
        when(console.readLine("Enter your username: ")).thenReturn("");
        when(console.readPassword("Enter your password: ")).thenReturn("".toCharArray());

        assertThrows(InvalidCredentials.class, () -> userInteraction.getCredentials());
    }

    @Test
    public void shouldThrowInvalidCredentialsWhenScannerInputsAreEmpty() {
        // given
        when(userInteraction.isConsoleAvailable()).thenReturn(false);
        when(scanner.nextLine()).thenReturn("");
        // when & then
        assertThrows(InvalidCredentials.class, () -> userInteraction.getCredentials());
    }

    @Test
    void shouldInvokeScannerAndProperlyReadCredentials() throws InvalidCredentials {
        // given
        when(userInteraction.isConsoleAvailable()).thenReturn(false);
        when(scanner.nextLine()).thenReturn("testUser", "testPass");

        // when
        Credentials credentials = userInteraction.getCredentials();

        // then
        assertEquals("testUser", credentials.username());
        assertEquals("testPass", credentials.password());
        verify(scanner, times(2)).nextLine();
    }

    @Test
    public void shouldReturnValidCredentialsWhenConsoleInputsAreValid() throws InvalidCredentials {
        when(console.readLine("Enter your username: ")).thenReturn("validUser");
        when(console.readPassword("Enter your password: ")).thenReturn("validPass".toCharArray());

        Credentials credentials = userInteraction.getCredentials();
        assertEquals("validUser", credentials.username());
        assertEquals("validPass", credentials.password());
    }

    @Test
    public void shouldThrowInvalidCredentialsWhenUsernameIsEmptyAndPasswordIsValid() {
        when(console.readLine("Enter your username: ")).thenReturn("");
        when(console.readPassword("Enter your password: ")).thenReturn("validPass".toCharArray());

        InvalidCredentials exception = assertThrows(InvalidCredentials.class, () -> userInteraction.getCredentials());
        assertEquals("Username or password cannot be empty.", exception.getMessage());
    }

    @Test
    public void shouldThrowInvalidCredentialsWhenPasswordIsEmptyAndUsernameIsValid() {
        when(console.readLine("Enter your username: ")).thenReturn("validUser");
        when(console.readPassword("Enter your password: ")).thenReturn("".toCharArray());

        InvalidCredentials exception = assertThrows(InvalidCredentials.class, () -> userInteraction.getCredentials());
        assertEquals("Username or password cannot be empty.", exception.getMessage());
    }

    @Test
    void shouldProperlyFormatAccounts() {
        // given
        List<AccountGroup> accountGroupsList = prepareAccountGroups();

        AccountGroups accountGroups = new AccountGroups(accountGroupsList);

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
        var result = userInteraction.formatAccountGroups(accountGroups);

        assertEquals(expectedOutput, result);
    }

    private static List<AccountGroup> prepareAccountGroups() {
        Account account1 = new Account(
                "123456",
                new BigDecimal("1000.0"),
                "USD",
                "Account1",
                "CustomAccount1"
        );

        Account account2 = new Account(
                "654321",
                new BigDecimal("2000.0"),
                "EUR",
                "Account2",
                "CustomAccount2"
        );
        List<Account> accounts1 = new ArrayList<>();
        accounts1.add(account1);
        List<Account> accounts2 = new ArrayList<>();
        accounts2.add(account2);

        AccountGroup group1 = new AccountGroup(accounts1);
        AccountGroup group2 = new AccountGroup(accounts2);

        List<AccountGroup> accountGroupsList = new ArrayList<>();
        accountGroupsList.add(group1);
        accountGroupsList.add(group2);
        return accountGroupsList;
    }
}
