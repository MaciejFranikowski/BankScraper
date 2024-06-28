package com.kontomatik.bankScraper.services;

import com.kontomatik.bankScraper.mbank.models.RequestParams;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JsoupClient {

    public Connection.Response sendRequest(
            String url,
            Connection.Method method,
            RequestParams params
    ) throws IOException {
        Connection connection = Jsoup.connect(url)
                .headers(params.headers())
                .ignoreContentType(params.ignoreContentType())
                .cookies(params.cookies())
                .method(method);

        if (params.requestBody() != null && !params.requestBody().isEmpty()) {
            connection.requestBody(params.requestBody());
        } else {
            connection.data(params.data());
        }

        return connection.execute();
    }
}
