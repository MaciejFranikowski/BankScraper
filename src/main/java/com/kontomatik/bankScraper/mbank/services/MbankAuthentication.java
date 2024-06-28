package com.kontomatik.bankScraper.mbank.services;

import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.kontomatik.bankScraper.mbank.models.*;
import com.kontomatik.bankScraper.models.*;
import com.google.gson.Gson;
import com.kontomatik.bankScraper.models.Cookies;
import com.kontomatik.bankScraper.services.JsoupClient;
import com.kontomatik.bankScraper.services.ResponseHandler;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class MbankAuthentication {

    private final Gson gson;
    private final JsoupClient jsoupClient;
    private final ResponseHandler responseHandler;
    private final UserInteraction userInteraction;

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

    @Value("${userAgent}")
    private String userAgent;

    public MbankAuthentication(Gson gson, JsoupClient jsoupClient, ResponseHandler responseHandler, UserInteraction userInteraction) {
        this.gson = gson;
        this.jsoupClient = jsoupClient;
        this.responseHandler = responseHandler;
        this.userInteraction = userInteraction;
    }

    public Cookies authenticate(Credentials credentials) {
        try {
            Cookies cookies = new Cookies();
            initialLogin(credentials, cookies);
            String csrfToken = fetchCsrfToken(cookies).csrfToken();
            String scaId = fetchScaAuthorizationData(cookies).scaAuthorizationId();
            String twoFactorAuthToken = initTwoFactorAuth(scaId, csrfToken, cookies);
            userInteraction.notifyTwoFactorAuthStart();
            waitForUserAuthentication(twoFactorAuthToken, cookies);
            finalizeAuthorization(scaId, csrfToken, cookies);
            return cookies;
        } catch (IOException | InterruptedException e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private void initialLogin(Credentials credentials, Cookies cookies) throws IOException {
        RequestParams params = new RequestParams.Builder()
                .data(Map.of("username", credentials.username(), "password", credentials.password()))
                .ignoreContentType(true)
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                loginUrl,
                Connection.Method.POST,
                params);
        updateCookies(cookies, response.cookies());
    }

    private CsrfResponse fetchCsrfToken(Cookies cookies) throws IOException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                fetchCsrfTokenUrl,
                Connection.Method.GET,
                params);
        updateCookies(cookies, response.cookies());
        return responseHandler.handleResponse(response.body(), CsrfResponse.class);
    }

    private ScaResponse fetchScaAuthorizationData(Cookies cookies) throws IOException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                fetchScaIdUrl,
                Connection.Method.POST,
                params);
        updateCookies(cookies, response.cookies());
        return responseHandler.handleResponse(response.body(), ScaResponse.class);
    }

    private String initTwoFactorAuth(String scaId, String csrfToken, Cookies cookies) throws IOException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .data(Map.of("Data", wrapScaIdIntoJson(scaId), "Url", "sca/authorization/disposable", "Method", "POST"))
                .headers(Map.of("User-Agent", userAgent, "X-Request-Verification-Token", csrfToken))
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                beginTwoFactorAuthUrl,
                Connection.Method.POST,
                params);
        InitTwoFactorResponse initResponse = responseHandler.handleResponse(response.body(), InitTwoFactorResponse.class);
        updateCookies(cookies, response.cookies());
        return initResponse.tranId();
    }

    private void waitForUserAuthentication(String twoFactorAuthToken, Cookies cookies) throws InterruptedException, IOException {
        String status;
        int attempts = 0;
        do {
            RequestParams params = new RequestParams.Builder()
                    .cookies(cookies.getCookies())
                    .ignoreContentType(true)
                    .data(Map.of("TranId", twoFactorAuthToken))
                    .build();
            Connection.Response response = jsoupClient.sendRequest(
                    statusTwoFactorAuthUrl,
                    Connection.Method.POST,
                    params);
            updateCookies(cookies, response.cookies());
            AuthStatusResponse statusResponseBody = responseHandler.handleResponse(response.body(), AuthStatusResponse.class);
            status = statusResponseBody.status();
            Thread.sleep(1000);
            if ("Canceled".equals(status)) {
                throw new AuthenticationException("2FA cancelled by user");
            }
            attempts++;
            if (attempts >= 30) {
                throw new AuthenticationException("Timeout (30s) reached for 2FA verification");
            }
        } while (!"Authorized".equals(status));
    }

    private void finalizeAuthorization(String scaId, String csrfToken, Cookies cookies) throws IOException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .headers(Map.of("User-Agent", userAgent, "X-Request-Verification-Token", csrfToken))
                .ignoreContentType(true)
                .requestBody(gson.toJson(new Object()))
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                executeTwoFactoAuthUrl,
                Connection.Method.POST,
                params);
        updateCookies(cookies, response.cookies());
        RequestParams finalizeParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .data(Map.of("scaAuthorizationId", scaId))
                .build();
        response = jsoupClient.sendRequest(
                scaFinalizeUrl,
                Connection.Method.POST,
                finalizeParams);
        updateCookies(cookies, response.cookies());
        verifyCorrectLogin(cookies);
    }

    private void verifyCorrectLogin(Cookies cookies) throws IOException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                initUrl + "?_=" + LocalDateTime.now(),
                Connection.Method.GET,
                params);
        if (response.statusCode() != 200) {
            throw new AuthenticationException("Login failed");
        }
    }

    private void updateCookies(Cookies cookies, Map<String, String> newCookies) {
        cookies.getCookies().putAll(newCookies);
    }

    private String wrapScaIdIntoJson(String scaId) {
        return "{\"scaAuthorizationId\": \"" + scaId + "\"}";
    }

}
