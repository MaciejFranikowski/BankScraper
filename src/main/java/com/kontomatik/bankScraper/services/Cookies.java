package com.kontomatik.bankScraper.services;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
public class Cookies {
    private Map<String, String> cookies = new HashMap<>();
    public Cookies() {}

    public void setCookies(HashMap<String, String> cookies) {
        this.cookies = cookies;
    }
}
