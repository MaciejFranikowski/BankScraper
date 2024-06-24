package com.kontomatik.bankScraper.services;

import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.google.gson.Gson;
import com.kontomatik.bankScraper.models.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class Authentication {
    private final Gson gson;
    private final Cookies cookies;
    public Authentication(Gson gson, Cookies cookies) {
        this.gson = gson;
        this.cookies = cookies;
    }
    public void authenticate(HashMap<String, String> credentials) {
        try{
            initalLogin(credentials);
            String csrfToken = fetchCsrfToken().getCsrfToken();
            String scaId = fetchScaAuthorizationData().getScaAuthorizationId();
            String twoFactorAuthToken = initTwoFactorAuth(scaId, csrfToken);
            waitForUserAuthentication(twoFactorAuthToken);
            finalizeAuthorization(scaId, csrfToken);
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }

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
        this.cookies.setCookies((HashMap<String, String>) response.cookies());
    }
    public CsrfResponse fetchCsrfToken() throws IOException {
        Connection.Response response = Jsoup.connect("https://online.mbank.pl/pl/setup/data")
                .ignoreContentType(true)
                .cookies(this.cookies.getCookies())
                .method(Connection.Method.GET)
                .execute();
        this.cookies.getCookies().putAll(response.cookies());
        return ResponseHandler.handleResponse(response.body(), CsrfResponse.class, gson);
    }
    public ScaResponse fetchScaAuthorizationData() throws IOException {
        Connection.Response response = Jsoup.connect("https://online.mbank.pl/pl/Sca/GetScaAuthorizationData")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                .ignoreContentType(true)
                .cookies(this.cookies.getCookies())
                .method(Connection.Method.POST)
                .execute();
        this.cookies.getCookies().putAll(response.cookies());
        return ResponseHandler.handleResponse(response.body(), ScaResponse.class, gson);
    }
    public String initTwoFactorAuth(String scaId, String csrfToken) throws IOException {
        Connection.Response response = Jsoup.connect("https://online.mbank.pl/api/auth/initprepare")
                .header("X-Request-Verification-Token", csrfToken)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .cookies(this.cookies.getCookies())
                .data("Data", new InitTwoFactorData(scaId).toString())
                .data("Url","sca/authorization/disposable")
                .data("Method","POST")
                .method(Connection.Method.POST)
                .execute();
        InitTwoFactorResponse initResponse = ResponseHandler.handleResponse(response.body(), InitTwoFactorResponse.class, gson);
        this.cookies.getCookies().putAll(response.cookies());
        return initResponse.getTranId();
    }
    private void waitForUserAuthentication(String twoFactorAuthToken) throws InterruptedException, IOException {
        String status;
        do {
            Connection.Response response  = Jsoup.connect("https://online.mbank.pl/api/auth/status")
                    .method(Connection.Method.POST)
                    .data("TranId",twoFactorAuthToken)
                    .cookies(this.cookies.getCookies())
                    .ignoreContentType(true)
                    .execute();
            this.cookies.getCookies().putAll(response.cookies());
            AuthStatusResponse statusResponseBody = ResponseHandler.handleResponse(response.body(), AuthStatusResponse.class, gson);
            status=statusResponseBody.getStatus();
            Thread.sleep(1000);
            if(status.equals("Canceled")) {
                throw new AuthenticationException("2FA cancelled by user");
            }
        } while (!status.equals("Authorized"));
    }
    private void finalizeAuthorization(String scaId, String csrfToken) throws IOException {
        this.cookies.getCookies().remove("fileDownload");
        Connection.Response response = Jsoup.connect("https://online.mbank.pl/api/auth/execute")
                .header("X-Request-Verification-Token", csrfToken)
                .method(Connection.Method.POST)
                .cookies(this.cookies.getCookies())
                .requestBody(gson.toJson(new Object()))
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        this.cookies.getCookies().putAll(response.cookies());
        response  = Jsoup.connect("https://online.mbank.pl/pl/Sca/FinalizeAuthorization")
                .method(Connection.Method.POST)
                .data("scaAuthorizationId",scaId)
                .cookies(this.cookies.getCookies())
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        this.cookies.getCookies().putAll(response.cookies());
        verifyCorrectLogin();
    }
    private void verifyCorrectLogin() throws IOException {
        Connection.Response response = Jsoup.connect("https://online.mbank.pl/api/chat/init?_=" + LocalDateTime.now())
                .cookies(this.cookies.getCookies())
                .ignoreContentType(true)
                .execute();
        if (response.statusCode()==200) {
            System.out.println("Login successful");
        } else {
            throw new AuthenticationException("\nLogin failed\n");
        }
    }
}