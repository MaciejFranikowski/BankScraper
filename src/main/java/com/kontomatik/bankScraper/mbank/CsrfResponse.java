package com.kontomatik.bankScraper.mbank;

import com.google.gson.annotations.SerializedName;

record CsrfResponse(@SerializedName("antiForgeryToken") String csrfToken) {
}
