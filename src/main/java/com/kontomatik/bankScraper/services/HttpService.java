package com.kontomatik.bankScraper.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class HttpService {
    @Value("${userAgent}")
    private String userAgent;

    public Connection.Response sendPostRequest(String url, Map<String, String> data) throws IOException {
        return Jsoup.connect(url)
                .header("User-Agent", userAgent)
                .ignoreContentType(true)
                .data(data)
                .method(Connection.Method.POST)
                .execute();
    }
    public Connection.Response sendPostRequest(String url, Map<String, String> data, Map<String, String> cookies) throws IOException {
        return Jsoup.connect(url)
                .header("User-Agent", userAgent)
                .ignoreContentType(true)
                .data(data)
                .cookies(cookies)
                .method(Connection.Method.POST)
                .execute();
    }
    public Connection.Response sendPostRequest(String url, Map<String, String> data, Map<String, String> cookies, String csrfToken) throws IOException {
        return Jsoup.connect(url)
                .header("User-Agent", userAgent)
                .header("X-Request-Verification-Token", csrfToken)
                .ignoreContentType(true)
                .data(data)
                .cookies(cookies)
                .method(Connection.Method.POST)
                .execute();
    }
    public Connection.Response sendExecutionRequest(String url, String emptyObject , Map<String, String> cookies, String csrfToken) throws IOException {
        return Jsoup.connect(url)
                .header("User-Agent", userAgent)
                .header("X-Request-Verification-Token", csrfToken)
                .ignoreContentType(true)
                .requestBody(emptyObject)
                .cookies(cookies)
                .method(Connection.Method.POST)
                .execute();
    }
    public Connection.Response sendGetRequest(String url, Map<String, String> cookies) throws IOException {
        return Jsoup.connect(url)
                .ignoreContentType(true)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .execute();
    }
}
