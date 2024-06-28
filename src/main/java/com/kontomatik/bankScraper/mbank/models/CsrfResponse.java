package com.kontomatik.bankScraper.mbank.models;

import com.google.gson.annotations.SerializedName;

public record CsrfResponse(@SerializedName("antiForgeryToken") String csrfToken) {
}
