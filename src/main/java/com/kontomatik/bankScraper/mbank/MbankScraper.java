package com.kontomatik.bankScraper.mbank;

import com.kontomatik.bankScraper.exceptions.ResponseHandlingException;
import com.kontomatik.bankScraper.exceptions.ScrapingException;
import com.kontomatik.bankScraper.models.Account;
import com.kontomatik.bankScraper.services.JsoupClient;
import com.kontomatik.bankScraper.services.ResponseHandler;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
class MbankScraper {
    private final ResponseHandler responseHandler;
    private final JsoupClient jsoupClient;

    @Value("${mbank.accounts.url}")
    private String mbankScraperUrl;

    @Value("${mbank.base.url}")
    private String baseUrl;

    MbankScraper(ResponseHandler responseHandler, JsoupClient jsoupClient) {
        this.responseHandler = responseHandler;
        this.jsoupClient = jsoupClient;
    }

    List<Account> scrape(Cookies cookies) {
        try {
            RequestParams requestParams = new RequestParams.Builder()
                    .cookies(cookies.getCookies())
                    .ignoreContentType(true)
                    .build();
            Connection.Response response = jsoupClient.sendRequest(
                    baseUrl + mbankScraperUrl,
                    Connection.Method.GET,
                    requestParams);
            AccountGroups groups = responseHandler.handleResponse(response.body(), AccountGroups.class);
            validateScrapedGroups(groups);
            return flattenAccountGroups(groups);

        } catch (ScrapingException | IOException | ResponseHandlingException e) {
            throw new ScrapingException("An error has occurred during scraping: " + e.getMessage());
        }
    }

    private List<Account> flattenAccountGroups(AccountGroups accountGroups) {
        return Optional.ofNullable(accountGroups)
                .map(AccountGroups::accountGroups)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(group -> group.accounts().stream())
                .collect(Collectors.toList());
    }

    private void validateScrapedGroups(AccountGroups groups) {
        if (groups == null) {
            throw new ScrapingException("AccountGroups is null");
        }
    }
}
