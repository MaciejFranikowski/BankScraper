package com.kontomatik.bankScraper.mbank;

import java.util.HashMap;
import java.util.Map;

public class Cookies {
    private final Map<String, String> cookies = new HashMap<>();

    public Cookies() {
    }

    public Map<String, String> getCookies() {
        return Map.copyOf(cookies);
    }

    public void addCookies(Map<String, String> newCookies) {
        cookies.putAll(newCookies);
    }
}
