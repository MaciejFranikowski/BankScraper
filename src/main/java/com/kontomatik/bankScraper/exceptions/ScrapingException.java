package com.kontomatik.bankScraper.exceptions;

public class ScrapingException extends RuntimeException {
    public ScrapingException(String message) {
        super(message);
    }
}
