package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

public class InitTwoFactorResponse {
    @SerializedName("TranId")
    public final String tranId;

    public InitTwoFactorResponse(String tranId) {
        this.tranId = tranId;
    }

    public String getTranId() {
        return tranId;
    }

    @Override
    public String toString() {
        return "InitPrepareResponseBody{" +
                "tranId='" + tranId + '\'' +
                '}';
    }
}
