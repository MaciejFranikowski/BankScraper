package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

public record ScaResponse(@SerializedName("ScaAuthorizationId") String scaAuthorizationId) {
}
