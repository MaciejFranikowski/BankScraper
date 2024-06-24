package com.kontomatik.bankScraper.models;

public class CsrfResponse {
    public final String csrfToken;

    public CsrfResponse(String antiForgeryToken) {
        this.csrfToken = antiForgeryToken;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    @Override
    public String toString() {
        return "SetupDataResponseBody{" +
                "antiForgeryToken='" + csrfToken + '\'' +
                '}';
    }
}
