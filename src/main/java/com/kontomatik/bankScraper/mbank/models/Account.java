package com.kontomatik.bankScraper.mbank.models;

import java.math.BigDecimal;

public record Account(String accountNumber, BigDecimal balance, String currency, String name, String customName) {
}
