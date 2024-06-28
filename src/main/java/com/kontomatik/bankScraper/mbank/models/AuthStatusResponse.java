package com.kontomatik.bankScraper.mbank.models;

import com.google.gson.annotations.SerializedName;

public record AuthStatusResponse(@SerializedName("Status") String status) {
}
