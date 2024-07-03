package com.kontomatik.bankScraper.mbank;

import com.google.gson.annotations.SerializedName;

record ScaResponse(@SerializedName("ScaAuthorizationId") String scaAuthorizationId) {
}
