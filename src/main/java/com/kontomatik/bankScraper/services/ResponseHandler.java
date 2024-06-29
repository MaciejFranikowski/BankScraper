package com.kontomatik.bankScraper.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kontomatik.bankScraper.exceptions.ResponseHandlingException;
import org.springframework.stereotype.Service;

@Service
public class ResponseHandler {
    private final Gson gson;

    public ResponseHandler(Gson gson) {
        this.gson = gson;
    }

    public <T> T handleResponse(String responseBody, Class<T> responseClass) throws ResponseHandlingException {
        try {
            return gson.fromJson(responseBody, responseClass);
        } catch (JsonSyntaxException e) {
            throw new ResponseHandlingException("Something went wrong while parsing the response body");
        }
    }
}
