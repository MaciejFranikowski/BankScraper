package com.kontomatik.bankScraper.mbank;

import com.google.gson.Gson;
import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.kontomatik.bankScraper.exceptions.InvalidCredentials;
import com.kontomatik.bankScraper.exceptions.ResponseHandlingException;
import com.kontomatik.bankScraper.models.Credentials;
import com.kontomatik.bankScraper.services.JsoupClient;
import com.kontomatik.bankScraper.services.ResponseHandler;
import com.kontomatik.bankScraper.ui.ConsolePrinter;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
class MbankAuthentication {
    private final Gson gson;
    private final JsoupClient jsoupClient;
    private final ResponseHandler responseHandler;
    private final ConsolePrinter consolePrinter;


    @Value("${mbank.base.url}")
    private String baseUrl;

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

    @Value("${mbank.finalize.twoFactorAuth.url}")
    private String scaFinalizeUrl;

    @Value("${userAgent}")
    private String userAgent;

    @Value("${mbank.accounts.url}")
    private String mbankScraperUrl;

    MbankAuthentication(Gson gson, JsoupClient jsoupClient, ResponseHandler responseHandler, ConsolePrinter consolePrinter) {
        this.gson = gson;
        this.jsoupClient = jsoupClient;
        this.responseHandler = responseHandler;
        this.consolePrinter = consolePrinter;
    }

    Cookies authenticate(Credentials credentials) {
        try {
            Cookies cookies = new Cookies();
            initialLogin(credentials, cookies);
            String csrfToken = fetchCsrfToken(cookies).csrfToken();
            String scaId = fetchScaAuthorizationData(cookies).scaAuthorizationId();
            String twoFactorAuthToken = initTwoFactorAuth(scaId, csrfToken, cookies);
            consolePrinter.notifyTwoFactorAuthStart();
            waitForUserAuthentication(twoFactorAuthToken, cookies);
            finalizeAuthorization(scaId, csrfToken, cookies);
            return cookies;
        } catch (IOException | InterruptedException | InvalidCredentials | ResponseHandlingException e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private void initialLogin(Credentials credentials, Cookies cookies) throws IOException, InvalidCredentials, ResponseHandlingException {
        RequestParams params = new RequestParams.Builder()
                .data(Map.of("username", credentials.username(), "password", credentials.password()))
                .ignoreContentType(true)
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                baseUrl + loginUrl,
                Connection.Method.POST,
                params);
        LoginResponse longinResponse = responseHandler.handleResponse(response.body(), LoginResponse.class);
        validateLoginResponse(longinResponse);
        cookies.addCookies(response.cookies());
    }

    private void validateLoginResponse(LoginResponse longinResponse) throws InvalidCredentials {
        String errorMessageTitle = "Nieprawidłowy identyfikator lub hasło.";
        if (longinResponse == null || (!longinResponse.successful() && longinResponse.errorMessageTitle().equals(errorMessageTitle))) {
            throw new InvalidCredentials("Passed credentials are invalid.");
        }
    }

    private CsrfResponse fetchCsrfToken(Cookies cookies) throws IOException, ResponseHandlingException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                baseUrl + fetchCsrfTokenUrl,
                Connection.Method.GET,
                params);
        CsrfResponse csrfResponse = responseHandler.handleResponse(response.body(), CsrfResponse.class);
        validateCsrfResponse(csrfResponse);
        cookies.addCookies(response.cookies());
        return csrfResponse;
    }

    private void validateCsrfResponse(CsrfResponse csrfResponse) throws AuthenticationException {
        if (csrfResponse == null || csrfResponse.csrfToken() == null || csrfResponse.csrfToken().isEmpty()) {
            throw new AuthenticationException("Failed to fetch CSRF token");
        }
    }

    private ScaResponse fetchScaAuthorizationData(Cookies cookies) throws IOException, ResponseHandlingException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                baseUrl + fetchScaIdUrl,
                Connection.Method.POST,
                params);
        ScaResponse scaResponse = responseHandler.handleResponse(response.body(), ScaResponse.class);
        validateScaResponse(scaResponse);
        cookies.addCookies(response.cookies());
        return responseHandler.handleResponse(response.body(), ScaResponse.class);
    }

    private void validateScaResponse(ScaResponse scaResponse) {
        if (scaResponse == null || scaResponse.scaAuthorizationId() == null || scaResponse.scaAuthorizationId().isEmpty()) {
            throw new AuthenticationException("Failed to fetch SCA authorization data");
        }
    }

    private String initTwoFactorAuth(String scaId, String csrfToken, Cookies cookies) throws IOException, ResponseHandlingException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .data(Map.of("Data", wrapScaIdIntoJson(scaId), "Url", "sca/authorization/disposable", "Method", "POST"))
                .headers(Map.of("User-Agent", userAgent, "X-Request-Verification-Token", csrfToken))
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                baseUrl + beginTwoFactorAuthUrl,
                Connection.Method.POST,
                params);
        InitTwoFactorResponse initResponse = responseHandler.handleResponse(response.body(), InitTwoFactorResponse.class);
        validateInitTwoFactorResponse(initResponse);
        cookies.addCookies(response.cookies());
        return initResponse.tranId();
    }

    private void validateInitTwoFactorResponse(InitTwoFactorResponse initResponse) {
        if (initResponse == null || initResponse.tranId() == null || initResponse.tranId().isEmpty()) {
            throw new AuthenticationException("Failed to initialize 2FA");
        }

    }

    private void waitForUserAuthentication(String twoFactorAuthToken, Cookies cookies) throws InterruptedException, IOException, ResponseHandlingException {
        String status;
        int attempts = 0;
        do {
            RequestParams params = new RequestParams.Builder()
                    .cookies(cookies.getCookies())
                    .ignoreContentType(true)
                    .data(Map.of("TranId", twoFactorAuthToken))
                    .build();
            Connection.Response response = jsoupClient.sendRequest(
                    baseUrl + statusTwoFactorAuthUrl,
                    Connection.Method.POST,
                    params);
            cookies.addCookies(response.cookies());
            AuthStatusResponse statusResponseBody = responseHandler.handleResponse(response.body(), AuthStatusResponse.class);
            status = statusResponseBody.status();
            validateStatusFetch(status);
            validateStatusNotCancelled(status);
            attempts++;
            if (attempts >= 30) {
                throw new AuthenticationException("Timeout (30s) reached for 2FA verification");
            }
            Thread.sleep(1000);
        } while (!"Authorized".equals(status));
    }

    private void validateStatusNotCancelled(String status) {
        if ("Canceled".equals(status)) {
            throw new AuthenticationException("2FA cancelled by user");
        }
    }

    private void validateStatusFetch(String status) {
        if (status == null || status.isEmpty()) {
            throw new AuthenticationException("Failed to fetch 2FA status");
        }
    }

    private void finalizeAuthorization(String scaId, String csrfToken, Cookies cookies) throws IOException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .headers(Map.of("User-Agent", userAgent, "X-Request-Verification-Token", csrfToken))
                .ignoreContentType(true)
                .requestBody(gson.toJson(new Object()))
                .build();
        Connection.Response executeAuthResponse = jsoupClient.sendRequest(
                baseUrl + executeTwoFactoAuthUrl,
                Connection.Method.POST,
                params);
        validateExecuteAuthResponse(executeAuthResponse);
        cookies.addCookies(executeAuthResponse.cookies());
        RequestParams finalizeParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .data(Map.of("scaAuthorizationId", scaId))
                .build();
        Connection.Response finalizeAuthResponse = jsoupClient.sendRequest(
                baseUrl + scaFinalizeUrl,
                Connection.Method.POST,
                finalizeParams);
        validateFinalizeAuthResponse(finalizeAuthResponse);
        cookies.addCookies(finalizeAuthResponse.cookies());
        verifyCorrectLogin(cookies);
    }

    private void validateFinalizeAuthResponse(Connection.Response finalizeAuthResponse) {
        if (finalizeAuthResponse.statusCode() != 200)
            throw new AuthenticationException("Failed to finalize 2FA");
    }

    private void validateExecuteAuthResponse(Connection.Response response) {
        if (response.statusCode() != 200) {
            throw new AuthenticationException("Failed to execute 2FA");
        }
    }

    private void verifyCorrectLogin(Cookies cookies) throws IOException {
        RequestParams params = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
        Connection.Response response = jsoupClient.sendRequest(
                baseUrl + mbankScraperUrl,
                Connection.Method.GET,
                params);
        if (response.statusCode() != 200) {
            throw new AuthenticationException("Login verification failed");
        }
    }

    private String wrapScaIdIntoJson(String scaId) {
        return "{\"scaAuthorizationId\": \"" + scaId + "\"}";
    }
}
