package com.kontomatik.bankScraper.mbank;

import java.util.HashMap;
import java.util.Map;

class Cookies {
    private final Map<String, String> cookies = new HashMap<>();

    Cookies() {
    }

    Map<String, String> getCookies() {
        return Map.copyOf(cookies);
    }

    void addCookies(Map<String, String> newCookies) {
        cookies.putAll(newCookies);
    }
}
