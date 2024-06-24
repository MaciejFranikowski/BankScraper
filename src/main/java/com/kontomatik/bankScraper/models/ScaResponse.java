package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

public class ScaResponse {
    @SerializedName("ScaAuthorizationId")
    public final String scaAuthorizationId;

    public ScaResponse(String scaAuthorizationId) {
        this.scaAuthorizationId = scaAuthorizationId;
    }

    public String getScaAuthorizationId() {
        return scaAuthorizationId;
    }
}
