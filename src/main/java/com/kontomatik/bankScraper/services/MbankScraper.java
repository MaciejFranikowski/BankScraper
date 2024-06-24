package com.kontomatik.bankScraper.services;

import com.google.gson.Gson;
import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.models.AccountGroups;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class MbankScraper {
    private final UserInteraction userInteraction;
    private final Cookies cookies;
    private final Gson gson;
    public MbankScraper(UserInteraction userInteraction, Cookies cookies, Gson gson) {
        this.userInteraction = userInteraction;
        this.cookies = cookies;
        this.gson = gson;
    }
    public void scrape() throws IOException {
        Connection.Response response = Jsoup.connect("https://online.mbank.pl/pl/Accounts/Accounts/AccountsGroups")
                .cookies(cookies.getCookies())
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        AccountGroups groups = ResponseHandler.handleResponse(response.body(), AccountGroups.class, gson);
        userInteraction.displayAccountGroups(groups);
    }
}
