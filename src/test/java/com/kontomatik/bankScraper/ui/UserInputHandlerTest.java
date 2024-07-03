package com.kontomatik.bankScraper.ui;

import com.kontomatik.bankScraper.exceptions.InvalidCredentials;
import com.kontomatik.bankScraper.models.Credentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserInputHandlerTest {

    private UserInput userInput;
    private UserInputHandler userInputHandler;

    @BeforeEach
    void setUp() {
        userInput = mock(UserInput.class);
        userInputHandler = new UserInputHandler(userInput);
    }

    @Test
    public void shouldThrowInvalidCredentialsWhenUsernameAndPasswordAreEmpty() {
        // given
        when(userInput.fetchUserName()).thenReturn("");
        when(userInput.fetchPassword()).thenReturn("");

        // when & then
        assertThrows(InvalidCredentials.class, () -> userInputHandler.getCredentials());
    }

    @Test
    public void shouldThrowInvalidCredentialsWhenUsernameIsEmpty() {
        // given
        when(userInput.fetchUserName()).thenReturn("");
        when(userInput.fetchPassword()).thenReturn("validPass");

        // when & then
        assertThrows(InvalidCredentials.class, () -> userInputHandler.getCredentials());
    }

    @Test
    public void shouldThrowInvalidCredentialsWhenPasswordIsEmpty() {
        // given
        when(userInput.fetchUserName()).thenReturn("validUser");
        when(userInput.fetchPassword()).thenReturn("");

        // when & then
        assertThrows(InvalidCredentials.class, () -> userInputHandler.getCredentials());
    }

    @Test
    void shouldReturnValidCredentialsWhenInputsAreValid() throws InvalidCredentials {
        // given
        when(userInput.fetchUserName()).thenReturn("validUser");
        when(userInput.fetchPassword()).thenReturn("validPass");

        // when
        Credentials credentials = userInputHandler.getCredentials();

        // then
        assertEquals("validUser", credentials.username());
        assertEquals("validPass", credentials.password());
        verify(userInput, times(1)).fetchUserName();
        verify(userInput, times(1)).fetchPassword();
    }
}
