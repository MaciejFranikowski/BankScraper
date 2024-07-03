package com.kontomatik.bankScraper.ui;


import java.io.Console;

public class ConsoleUserInput implements UserInput {
    private final Console console;

    public ConsoleUserInput(Console console) {
        this.console = console;
    }

    @Override
    public String fetchUserName() {
        return console.readLine("Enter your username: ");
    }

    @Override
    public String fetchPassword() {
        return new String(console.readPassword("Enter your password: "));
    }
}