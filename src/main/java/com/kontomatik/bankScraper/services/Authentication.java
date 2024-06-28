package com.kontomatik.bankScraper.services;

import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.kontomatik.bankScraper.models.*;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Authentication {

    private final Gson gson;
    private final HttpService httpService;
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

    public Cookies authenticate(Credentials credentials) {
        try {
            Cookies cookies = new Cookies();
            initialLogin(credentials, cookies);
            String csrfToken = fetchCsrfToken(cookies).getCsrfToken();
            String scaId = fetchScaAuthorizationData(cookies).getScaAuthorizationId();
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
        Connection.Response response = httpService.sendPostRequest(loginUrl, credentials);
        cookies.setCookies(new HashMap<>(response.cookies()));
    }

    private CsrfResponse fetchCsrfToken(Cookies cookies) throws IOException {
        Connection.Response response = httpService.sendGetRequest(fetchCsrfTokenUrl, cookies.getCookies());
        updateCookies(cookies, response.cookies());
        return responseHandler.handleResponse(response.body(), CsrfResponse.class);
    }

    private ScaResponse fetchScaAuthorizationData(Cookies cookies) throws IOException {
        Connection.Response response = httpService.sendPostRequest(fetchScaIdUrl, new HashMap<>(), cookies.getCookies());
        updateCookies(cookies, response.cookies());
        return responseHandler.handleResponse(response.body(), ScaResponse.class);
    }

    private String initTwoFactorAuth(String scaId, String csrfToken, Cookies cookies) throws IOException {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("Data", new InitTwoFactorData(scaId).toString());
        requestData.put("Url", "sca/authorization/disposable");
        requestData.put("Method", "POST");

        Connection.Response response = httpService.sendPostRequest(beginTwoFactorAuthUrl, requestData, cookies.getCookies(), csrfToken);
        InitTwoFactorResponse initResponse = responseHandler.handleResponse(response.body(), InitTwoFactorResponse.class);
        updateCookies(cookies, response.cookies());
        return initResponse.getTranId();
    }

    private void waitForUserAuthentication(String twoFactorAuthToken, Cookies cookies) throws InterruptedException, IOException {
        String status;
        do {
            Connection.Response response = httpService.sendPostRequest(
                    statusTwoFactorAuthUrl,
                    Map.of("TranId", twoFactorAuthToken),
                    cookies.getCookies());
            updateCookies(cookies, response.cookies());
            AuthStatusResponse statusResponseBody = responseHandler.handleResponse(response.body(), AuthStatusResponse.class);
            status = statusResponseBody.getStatus();
            Thread.sleep(1000);
            if ("Canceled".equals(status)) {
                throw new AuthenticationException("2FA cancelled by user");
            }
        } while (!"Authorized".equals(status));
    }

    private void finalizeAuthorization(String scaId, String csrfToken, Cookies cookies) throws IOException {
        Connection.Response response = httpService.sendExecutionRequest(executeTwoFactoAuthUrl, gson.toJson(new Object()), cookies.getCookies(), csrfToken);
        updateCookies(cookies, response.cookies());
        response = httpService.sendPostRequest(
                scaFinalizeUrl, Map.of("scaAuthorizationId", scaId), cookies.getCookies(), csrfToken);
        updateCookies(cookies, response.cookies());
        verifyCorrectLogin(cookies);
    }

    private void verifyCorrectLogin(Cookies cookies) throws IOException {
        Connection.Response response = httpService.sendGetRequest(initUrl + "?_=" + LocalDateTime.now(), cookies.getCookies());
        if (response.statusCode() != 200) {
            throw new AuthenticationException("Login failed");
        }
    }

    private void updateCookies(Cookies cookies ,Map<String, String> newCookies) {
        cookies.getCookies().putAll(newCookies);
    }

}
