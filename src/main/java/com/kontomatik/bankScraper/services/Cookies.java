package com.kontomatik.bankScraper.services;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
@NoArgsConstructor
public class Cookies {
    private Map<String, String> cookies = new HashMap<>();

    public void setCookies(HashMap<String, String> cookies) {
        this.cookies = cookies;
    }
}
