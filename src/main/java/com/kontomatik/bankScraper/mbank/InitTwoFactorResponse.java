package com.kontomatik.bankScraper.mbank;

import com.google.gson.annotations.SerializedName;

record InitTwoFactorResponse(@SerializedName("TranId") String tranId) {
}
