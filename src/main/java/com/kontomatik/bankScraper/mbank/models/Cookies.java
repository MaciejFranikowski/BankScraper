package com.kontomatik.bankScraper.mbank.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Cookies {
    private final Map<String, String> cookies = new HashMap<>();

    public Cookies() {
    }

    public Map<String, String> getCookies() {
        return Collections.unmodifiableMap(cookies);
    }

    public void addCookies(Map<String, String> newCookies) {
        cookies.putAll(newCookies);
    }
}
