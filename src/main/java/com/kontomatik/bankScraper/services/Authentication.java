package com.kontomatik.bankScraper.services;

import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.kontomatik.bankScraper.models.*;
import com.google.gson.Gson;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final HttpService httpService;
    private final ResponseHandler responseHandler;

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

    @Autowired
    public Authentication(Gson gson, Cookies cookies, HttpService httpService, ResponseHandler responseHandler) {
        this.gson = gson;
        this.cookies = cookies;
        this.httpService = httpService;
        this.responseHandler = responseHandler;
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
        Connection.Response response = httpService.sendPostRequest(loginUrl, credentials);
        cookies.setCookies(new HashMap<>(response.cookies()));
    }

    private CsrfResponse fetchCsrfToken() throws IOException {
        Connection.Response response = httpService.sendGetRequest(fetchCsrfTokenUrl, cookies.getCookies());
        updateCookies(response.cookies());
        return responseHandler.handleResponse(response.body(), CsrfResponse.class, gson);
    }

    private ScaResponse fetchScaAuthorizationData() throws IOException {
        Connection.Response response = httpService.sendPostRequest(fetchScaIdUrl, new HashMap<>(), cookies.getCookies());
        updateCookies(response.cookies());
        return responseHandler.handleResponse(response.body(), ScaResponse.class, gson);
    }

    private String initTwoFactorAuth(String scaId, String csrfToken) throws IOException {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("Data", new InitTwoFactorData(scaId).toString());
        requestData.put("Url", "sca/authorization/disposable");
        requestData.put("Method", "POST");

        Connection.Response response = httpService.sendPostRequest(beginTwoFactorAuthUrl, requestData, cookies.getCookies(),csrfToken);
        InitTwoFactorResponse initResponse = responseHandler.handleResponse(response.body(), InitTwoFactorResponse.class, gson);
        updateCookies(response.cookies());
        return initResponse.getTranId();
    }

    private void waitForUserAuthentication(String twoFactorAuthToken) throws InterruptedException, IOException {
        String status;
        do {
            Connection.Response response = httpService.sendPostRequest(statusTwoFactorAuthUrl, Map.of("TranId", twoFactorAuthToken));
            updateCookies(response.cookies());
            AuthStatusResponse statusResponseBody = responseHandler.handleResponse(response.body(), AuthStatusResponse.class, gson);
            status = statusResponseBody.getStatus();
            Thread.sleep(1000);
            if ("Canceled".equals(status)) {
                throw new AuthenticationException("2FA cancelled by user");
            }
        } while (!"Authorized".equals(status));
    }

    private void finalizeAuthorization(String scaId, String csrfToken) throws IOException {
        Connection.Response response = httpService.sendExecutionRequest(executeTwoFactoAuthUrl, gson.toJson(new Object()), cookies.getCookies(),csrfToken);
        updateCookies(response.cookies());
        response = httpService.sendPostRequest(
                scaFinalizeUrl, Map.of("scaAuthorizationId", scaId), cookies.getCookies(),csrfToken);
        updateCookies(response.cookies());
        verifyCorrectLogin();
    }

    private void verifyCorrectLogin() throws IOException {
        Connection.Response response = httpService.sendGetRequest(initUrl + "?_=" + LocalDateTime.now(), cookies.getCookies());
        if (response.statusCode() != 200) {
            throw new AuthenticationException("Login failed");
        }
    }

    private void updateCookies(Map<String, String> newCookies) {
        cookies.getCookies().putAll(newCookies);
    }

}
