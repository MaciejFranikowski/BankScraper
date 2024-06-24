package com.kontomatik.bankScraper.cli;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Scanner;
@Component
public class UserInteraction {
    private final Scanner scanner;

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
}
