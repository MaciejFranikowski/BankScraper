package com.kontomatik.bankScraper.ui;

import java.util.Scanner;

public class ScannerUserInput implements UserInput {
    private final Scanner scanner;

    public ScannerUserInput(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String fetchUserName() {
        System.out.print("Enter your username: ");
        return scanner.nextLine();
    }

    @Override
    public String fetchPassword() {
        System.out.print("Enter your password: ");
        return scanner.nextLine();
    }
}
