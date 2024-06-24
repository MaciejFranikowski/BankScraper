package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

public class AuthStatusResponse {
    @SerializedName("Status")
    public final String status;

    public AuthStatusResponse(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StatusResponseBody{" +
                "status='" + status + '\'' +
                '}';
    }

    public String getStatus() {
        return status;
    }
}
