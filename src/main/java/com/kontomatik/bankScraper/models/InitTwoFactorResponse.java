package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

public record InitTwoFactorResponse(@SerializedName("TranId") String tranId) {
}
