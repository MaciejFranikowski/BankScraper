package com.kontomatik.bankScraper.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InitTwoFactorData {
    private final String scaAuthorizationId;

    @Override
    public String toString() {
        return "{\"ScaAuthorizationId\":\""+scaAuthorizationId+"\"}";
    }
}
