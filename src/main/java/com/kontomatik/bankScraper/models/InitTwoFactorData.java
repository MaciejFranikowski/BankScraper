package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InitTwoFactorData {
    @SerializedName("ScaAuthorizationId")
    private final String scaAuthorizationId;

    @Override
    public String toString() {
        return "{\"ScaAuthorizationId\":\""+scaAuthorizationId+"\"}";
    }
}
