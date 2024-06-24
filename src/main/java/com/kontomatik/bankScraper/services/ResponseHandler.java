package com.kontomatik.bankScraper.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class ResponseHandler {
    public <T> T handleResponse(String responseBody, Class<T> responseClass, Gson gson) {
        try {
            return gson.fromJson(responseBody, responseClass);
        } catch (JsonSyntaxException e){
            throw new AuthenticationException("Wrong credentials");
        }
    }
}
