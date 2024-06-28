package com.kontomatik.bankScraper.models;

import java.util.HashMap;
import java.util.Map;

public class Cookies {
    private final Map<String, String>  cookies = new HashMap<>();

    public Cookies() {
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

}
