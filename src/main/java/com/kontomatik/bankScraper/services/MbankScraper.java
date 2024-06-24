package com.kontomatik.bankScraper.services;

import com.google.gson.Gson;
import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.exceptions.ScrapingException;
import com.kontomatik.bankScraper.models.AccountGroups;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class MbankScraper {
    private final UserInteraction userInteraction;
    private final Cookies cookies;
    private final Gson gson;
    private final ResponseHandler responseHandler;
    private final HttpService httpService;
    @Value("${mbank.accounts.url}")
    private String mbankScraperUrl;
    public MbankScraper(UserInteraction userInteraction, Cookies cookies, Gson gson, ResponseHandler responseHandler, HttpService httpService) {
        this.userInteraction = userInteraction;
        this.cookies = cookies;
        this.gson = gson;
        this.responseHandler = responseHandler;
        this.httpService = httpService;
    }
    public void scrape() {
        try {
            Connection.Response response = httpService.sendGetRequest(mbankScraperUrl, cookies.getCookies());
            AccountGroups groups = responseHandler.handleResponse(response.body(), AccountGroups.class, gson);
            userInteraction.displayAccountGroups(groups);
        } catch (Exception e) {
            throw new ScrapingException("An error has occurred during scraping: " + e.getMessage());
        }
    }
}
