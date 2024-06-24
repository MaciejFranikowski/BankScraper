package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

public class InitTwoFactorData {
    @SerializedName("ScaAuthorizationId")
    private final String scaAuthorizationId;

    public InitTwoFactorData(String scaAuthorizationId) {
        this.scaAuthorizationId = scaAuthorizationId;
    }

    @Override
    public String toString() {
        return "{\"ScaAuthorizationId\":\""+scaAuthorizationId+"\"}";
    }
}
