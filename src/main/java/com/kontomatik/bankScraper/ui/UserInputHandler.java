package com.kontomatik.bankScraper.ui;

import com.kontomatik.bankScraper.exceptions.InvalidCredentials;
import com.kontomatik.bankScraper.models.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserInputHandler {
    private final UserInput userInput;

    @Autowired
    public UserInputHandler(UserInput userInput) {
        this.userInput = userInput;
    }

    public Credentials getCredentials() throws InvalidCredentials {
        String username = userInput.fetchUserName();
        String password = userInput.fetchPassword();
        if (validateCredentials(username, password)) {
            throw new InvalidCredentials("Username or password cannot be empty.");
        }
        return new Credentials(username, password);
    }

    private boolean validateCredentials(String username, String password) {
        return username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty();
    }
}
