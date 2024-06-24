package com.kontomatik.bankScraper.services;

import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.kontomatik.bankScraper.models.*;
import com.google.gson.Gson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class Authentication {

    private final Gson gson;
    private final Cookies cookies;

    @Value("${mbank.login.url}")
    private String loginUrl;

    @Value("${mbank.fetch.csrf.url}")
    private String fetchCsrfTokenUrl;

    @Value("${mbank.fetch.scaId.url}")
    private String fetchScaIdUrl;

    @Value("${mbank.begin.twoFactorAuth.url}")
    private String beginTwoFactorAuthUrl;

    @Value("${mbank.status.twoFactorAuth.url}")
    private String statusTwoFactorAuthUrl;

    @Value("${mbank.execute.twoFactorAuth.url}")
    private String executeTwoFactoAuthUrl;

    @Value("${mbank.sca.finalize.url}")
    private String scaFinalizeUrl;

    @Value("${mbank.init.url}")
    private String initUrl;

    public Authentication(Gson gson, Cookies cookies) {
        this.gson = gson;
        this.cookies = cookies;
    }

    public void authenticate(HashMap<String, String> credentials) {
        try {
            initialLogin(credentials);
            String csrfToken = fetchCsrfToken().getCsrfToken();
            String scaId = fetchScaAuthorizationData().getScaAuthorizationId();
            String twoFactorAuthToken = initTwoFactorAuth(scaId, csrfToken);
            waitForUserAuthentication(twoFactorAuthToken);
            finalizeAuthorization(scaId, csrfToken);
        } catch (IOException | InterruptedException e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private void initialLogin(HashMap<String, String> credentials) throws IOException {
        Connection.Response response = Jsoup.connect(loginUrl)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                .ignoreContentType(true)
                .data(credentials)
                .method(Connection.Method.POST)
                .execute();
        cookies.setCookies(new HashMap<>(response.cookies()));
    }

    private CsrfResponse fetchCsrfToken() throws IOException {
        Connection.Response response = Jsoup.connect(fetchCsrfTokenUrl)
                .ignoreContentType(true)
                .cookies(cookies.getCookies())
                .method(Connection.Method.GET)
                .execute();
        updateCookies(response.cookies());
        return ResponseHandler.handleResponse(response.body(), CsrfResponse.class, gson);
    }

    private ScaResponse fetchScaAuthorizationData() throws IOException {
        Connection.Response response = Jsoup.connect(fetchScaIdUrl)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                .ignoreContentType(true)
                .cookies(cookies.getCookies())
                .method(Connection.Method.POST)
                .execute();
        updateCookies(response.cookies());
        return ResponseHandler.handleResponse(response.body(), ScaResponse.class, gson);
    }

    private String initTwoFactorAuth(String scaId, String csrfToken) throws IOException {
        Connection.Response response = Jsoup.connect(beginTwoFactorAuthUrl)
                .header("X-Request-Verification-Token", csrfToken)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .cookies(cookies.getCookies())
                .data("Data", new InitTwoFactorData(scaId).toString())
                .data("Url", "sca/authorization/disposable")
                .data("Method", "POST")
                .method(Connection.Method.POST)
                .execute();
        InitTwoFactorResponse initResponse = ResponseHandler.handleResponse(response.body(), InitTwoFactorResponse.class, gson);
        updateCookies(response.cookies());
        return initResponse.getTranId();
    }

    private void waitForUserAuthentication(String twoFactorAuthToken) throws InterruptedException, IOException {
        String status;
        do {
            Connection.Response response = sendStatusRequest(twoFactorAuthToken);
            updateCookies(response.cookies());
            AuthStatusResponse statusResponseBody = ResponseHandler.handleResponse(response.body(), AuthStatusResponse.class, gson);
            status = statusResponseBody.getStatus();
            Thread.sleep(1000);
            if ("Canceled".equals(status)) {
                throw new AuthenticationException("2FA cancelled by user");
            }
        } while (!"Authorized".equals(status));
    }
    private Connection.Response sendStatusRequest(String twoFactorAuthToken) throws IOException {
        return Jsoup.connect(statusTwoFactorAuthUrl)
                .method(Connection.Method.POST)
                .data("TranId", twoFactorAuthToken)
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .execute();
    }

    private void finalizeAuthorization(String scaId, String csrfToken) throws IOException {
        Connection.Response response = Jsoup.connect(executeTwoFactoAuthUrl)
                .header("X-Request-Verification-Token", csrfToken)
                .method(Connection.Method.POST)
                .cookies(cookies.getCookies())
                .requestBody(gson.toJson(new Object()))
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        updateCookies(response.cookies());

        response = Jsoup.connect(scaFinalizeUrl)
                .method(Connection.Method.POST)
                .data("scaAuthorizationId", scaId)
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        updateCookies(response.cookies());

        verifyCorrectLogin();
    }

    private void verifyCorrectLogin() throws IOException {
        Connection.Response response = Jsoup.connect(initUrl + "?_=" + LocalDateTime.now())
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .execute();
        if (response.statusCode() != 200) {
            throw new AuthenticationException("Login failed");
        }
    }
    private void updateCookies(Map<String, String> newCookies) {
        cookies.getCookies().putAll(newCookies);
    }
}
