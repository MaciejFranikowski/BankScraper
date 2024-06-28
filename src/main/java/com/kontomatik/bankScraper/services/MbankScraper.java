package com.kontomatik.bankScraper.services;

import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.exceptions.ScrapingException;
import com.kontomatik.bankScraper.models.AccountGroups;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class MbankScraper {
    private final UserInteraction userInteraction;
    private final ResponseHandler responseHandler;
    private final HttpService httpService;

    @Value("${mbank.accounts.url}")
    private String mbankScraperUrl;

    public MbankScraper(UserInteraction userInteraction, ResponseHandler responseHandler, HttpService httpService) {
        this.userInteraction = userInteraction;
        this.responseHandler = responseHandler;
        this.httpService = httpService;
    }

    public void scrape(Cookies cookies) {
        try {
            Connection.Response response = httpService.sendGetRequest(mbankScraperUrl, cookies.getCookies());
            AccountGroups groups = responseHandler.handleResponse(response.body(), AccountGroups.class);
            System.out.println(userInteraction.formatAccountGroups(groups));
        } catch (Exception e) {
            throw new ScrapingException("An error has occurred during scraping: " + e.getMessage());
        }
    }
}
