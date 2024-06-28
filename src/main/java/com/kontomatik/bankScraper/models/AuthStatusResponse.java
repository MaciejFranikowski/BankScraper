package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

public record AuthStatusResponse(@SerializedName("Status") String status) {
}
