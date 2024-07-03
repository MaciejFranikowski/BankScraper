package com.kontomatik.bankScraper.mbank;

import com.google.gson.annotations.SerializedName;

record AuthStatusResponse(@SerializedName("Status") String status) {
}
