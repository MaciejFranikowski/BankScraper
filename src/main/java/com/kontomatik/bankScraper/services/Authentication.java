package com.kontomatik.bankScraper.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

@Service
public class Authentication {

    public void authenticate(HashMap<String, String> credentials) throws IOException {
        initalLogin(credentials);

    }
    public void initalLogin(HashMap<String, String> credentials) throws IOException {

        Connection.Response loginPageResponse = Jsoup.connect("https://online.mbank.pl/pl/Login")
                .method(Connection.Method.GET)
                .execute();
        Connection.Response response = Jsoup.connect("https://online.mbank.pl/pl/LoginMain/Account/JsonLogin")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                .ignoreContentType(true)
                .cookies(loginPageResponse.cookies())
                .data(credentials)
                .method(Connection.Method.POST)
                .execute();
    }

}