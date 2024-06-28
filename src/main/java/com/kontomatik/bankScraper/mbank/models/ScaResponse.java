package com.kontomatik.bankScraper.mbank.models;

import com.google.gson.annotations.SerializedName;

public record ScaResponse(@SerializedName("ScaAuthorizationId") String scaAuthorizationId) {
}
