package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

public record CsrfResponse(@SerializedName("antiForgeryToken") String csrfToken) {
}
