package com.kontomatik.bankScraper.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ResponseHandler {
    private final Gson gson;

    public <T> T handleResponse(String responseBody, Class<T> responseClass) {
        try {
            return gson.fromJson(responseBody, responseClass);
        } catch (JsonSyntaxException e) {
            throw new AuthenticationException("Wrong credentials");
        }
    }
}
