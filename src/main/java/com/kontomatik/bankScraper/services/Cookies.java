package com.kontomatik.bankScraper.services;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Cookies {
    private Map<String, String> cookies = new HashMap<>();

    public Cookies() {
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
}
