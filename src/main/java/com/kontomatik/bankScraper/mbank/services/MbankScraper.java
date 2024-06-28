package com.kontomatik.bankScraper.mbank.services;

import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.exceptions.ScrapingException;
import com.kontomatik.bankScraper.mbank.models.AccountGroups;
import com.kontomatik.bankScraper.mbank.models.RequestParams;
import com.kontomatik.bankScraper.services.Cookies;
import com.kontomatik.bankScraper.services.JsoupClient;
import com.kontomatik.bankScraper.services.ResponseHandler;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class MbankScraper {
    private final UserInteraction userInteraction;
    private final ResponseHandler responseHandler;
    private final JsoupClient jsoupClient;

    @Value("${mbank.accounts.url}")
    private String mbankScraperUrl;

    public MbankScraper(UserInteraction userInteraction, ResponseHandler responseHandler, JsoupClient jsoupClient) {
        this.userInteraction = userInteraction;
        this.responseHandler = responseHandler;
        this.jsoupClient = jsoupClient;
    }

    public void scrape(Cookies cookies) {
        try {
            RequestParams requestParams = new RequestParams.Builder()
                    .cookies(cookies.getCookies())
                    .ignoreContentType(true)
                    .build();
            Connection.Response response = jsoupClient.sendRequest(
                    mbankScraperUrl,
                    Connection.Method.GET,
                    requestParams);
            AccountGroups groups = responseHandler.handleResponse(response.body(), AccountGroups.class);
            System.out.println(userInteraction.formatAccountGroups(groups));
        } catch (Exception e) {
            throw new ScrapingException("An error has occurred during scraping: " + e.getMessage());
        }
    }
}
