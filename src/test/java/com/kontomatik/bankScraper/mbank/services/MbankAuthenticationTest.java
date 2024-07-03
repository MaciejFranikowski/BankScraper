package com.kontomatik.bankScraper.mbank.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.gson.Gson;
import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.kontomatik.bankScraper.mbank.MbankAuthentication;
import com.kontomatik.bankScraper.models.Credentials;
import com.kontomatik.bankScraper.services.JsoupClient;
import com.kontomatik.bankScraper.services.ResponseHandler;
import com.kontomatik.bankScraper.ui.ConsolePrinter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        MbankAuthentication.class,
        JsoupClient.class,
        ConsolePrinter.class,
        ResponseHandler.class,
        ResponseHandler.class
})
@TestPropertySource("classpath:application-test.properties")
class MbankAuthenticationTest {

    @SpyBean
    private Gson gson;
    @Autowired
    private MbankAuthentication authentication;

    @Value("${mbank.wiremock.port}")
    private Integer wireMockPort;

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
    private String executeTwoFactorAuthUrl;

    @Value("${mbank.finalize.twoFactorAuth.url}")
    private String finalizeTwoFactorAuthUrl;

    @Value("${mbank.accounts.url}")
    private String verifyLoginUrl;

    private Credentials credentials;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(wireMockPort);
        wireMockServer.start();
        configureFor("localhost", wireMockPort);
        credentials = new Credentials("testuser", "testpassword");
    }

    @AfterEach
    void cleanup() {
        wireMockServer.stop();
    }


    @Test
    void shouldThrowExceptionWhenInitialLoginFails() {
        var expectedStatus = 200;
        var responseBody = "{\"successful\":false,\"errorMessageTitle\":\"Nieprawidłowy identyfikator lub hasło.\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                        .withBody(responseBody)
                ));
        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Authentication failed: Passed credentials are invalid.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowExceptionWhenFetchCsrfTokenFails() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));

        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Failed to fetch CSRF token";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowExceptionWhenFetchScaAuthorizationDataFails() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));

        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"testCsrfToken\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        var expectedStatusSca = 200;
        var responseBodySca = "{\"ScaAuthorizationId\":\"\"}";
        stubFor(post(urlEqualTo(fetchScaIdUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusSca)
                        .withBody(responseBodySca)
                ));

        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Failed to fetch SCA authorization data";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowExceptionWhenInitTwoFactoAuthFails() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));

        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"testCsrfToken\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        var expectedStatusSca = 200;
        var responseBodySca = "{\"ScaAuthorizationId\":\"testScaId\"}";
        stubFor(post(urlEqualTo(fetchScaIdUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusSca)
                        .withBody(responseBodySca)
                ));

        var expectedStatusTwoFactorAuth = 200;
        var responseBodyTwoFactorAuth = "{\"TranId\":\"\"}";
        stubFor(post(urlEqualTo(beginTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuth)
                        .withBody(responseBodyTwoFactorAuth)
                ));

        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Failed to initialize 2FA";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowExceptionWhenWaitForUserAuthenticationFails() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));


        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"testCsrfToken\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        var expectedStatusSca = 200;
        var responseBodySca = "{\"ScaAuthorizationId\":\"testScaId\"}";
        stubFor(post(urlEqualTo(fetchScaIdUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusSca)
                        .withBody(responseBodySca)
                ));

        var expectedStatusTwoFactorAuth = 200;
        var responseBodyTwoFactorAuth = "{\"TranId\":\"testTranId\"}";
        stubFor(post(urlEqualTo(beginTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuth)
                        .withBody(responseBodyTwoFactorAuth)
                ));

        var expectedStatusTwoFactorAuthStatus = 200;
        var responseBodyTwoFactorAuthStatus = "{\"Status\":\"\"}";
        stubFor(post(urlEqualTo(statusTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuthStatus)
                        .withBody(responseBodyTwoFactorAuthStatus)
                ));

        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Failed to fetch 2FA status";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowExceptionWhenWaitForUserAuthenticationTimesOut() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));

        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"testCsrfToken\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        var expectedStatusSca = 200;
        var responseBodySca = "{\"ScaAuthorizationId\":\"testScaId\"}";
        stubFor(post(urlEqualTo(fetchScaIdUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusSca)
                        .withBody(responseBodySca)
                ));

        var expectedStatusTwoFactorAuth = 200;
        var responseBodyTwoFactorAuth = "{\"TranId\":\"testTranId\"}";
        stubFor(post(urlEqualTo(beginTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuth)
                        .withBody(responseBodyTwoFactorAuth)
                ));

        var expectedStatusTwoFactorAuthStatus = 200;
        var responseBodyTwoFactorAuthStatus = "{\"Status\":\"PreAuthorized\"}";
        stubFor(post(urlEqualTo(statusTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuthStatus)
                        .withBody(responseBodyTwoFactorAuthStatus)
                ));

        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Timeout (30s) reached for 2FA verification";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowExceptionWhenFinalizeAuthorizationExecuteFails() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));

        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"testCsrfToken\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        var expectedStatusSca = 200;
        var responseBodySca = "{\"ScaAuthorizationId\":\"testScaId\"}";
        stubFor(post(urlEqualTo(fetchScaIdUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusSca)
                        .withBody(responseBodySca)
                ));

        var expectedStatusTwoFactorAuth = 200;
        var responseBodyTwoFactorAuth = "{\"TranId\":\"testTranId\"}";
        stubFor(post(urlEqualTo(beginTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuth)
                        .withBody(responseBodyTwoFactorAuth)
                ));

        var expectedStatusTwoFactorAuthStatus = 200;
        var responseBodyTwoFactorAuthStatus = "{\"Status\":\"Authorized\"}";
        stubFor(post(urlEqualTo(statusTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuthStatus)
                        .withBody(responseBodyTwoFactorAuthStatus)
                ));

        var expectedStatusExecute = 500;
        stubFor(post(urlEqualTo(executeTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusExecute)
                ));

        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Failed to execute 2FA";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowExceptionWhenFinalizeAuthorizationFinalizeFails() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));

        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"testCsrfToken\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        var expectedStatusSca = 200;
        var responseBodySca = "{\"ScaAuthorizationId\":\"testScaId\"}";
        stubFor(post(urlEqualTo(fetchScaIdUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusSca)
                        .withBody(responseBodySca)
                ));

        var expectedStatusTwoFactorAuth = 200;
        var responseBodyTwoFactorAuth = "{\"TranId\":\"testTranId\"}";
        stubFor(post(urlEqualTo(beginTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuth)
                        .withBody(responseBodyTwoFactorAuth)
                ));

        var expectedStatusTwoFactorAuthStatus = 200;
        var responseBodyTwoFactorAuthStatus = "{\"Status\":\"Authorized\"}";
        stubFor(post(urlEqualTo(statusTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuthStatus)
                        .withBody(responseBodyTwoFactorAuthStatus)
                ));

        var expectedStatusExecute = 200;
        stubFor(post(urlEqualTo(executeTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusExecute)
                ));

        var finalizeStatusExecute = 500;
        stubFor(post(urlEqualTo(finalizeTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(finalizeStatusExecute)
                ));

        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Failed to finalize 2FA";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowExceptionWhenVerifyCorrectLoginFails() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));

        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"testCsrfToken\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        var expectedStatusSca = 200;
        var responseBodySca = "{\"ScaAuthorizationId\":\"testScaId\"}";
        stubFor(post(urlEqualTo(fetchScaIdUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusSca)
                        .withBody(responseBodySca)
                ));

        var expectedStatusTwoFactorAuth = 200;
        var responseBodyTwoFactorAuth = "{\"TranId\":\"testTranId\"}";
        stubFor(post(urlEqualTo(beginTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuth)
                        .withBody(responseBodyTwoFactorAuth)
                ));

        var expectedStatusTwoFactorAuthStatus = 200;
        var responseBodyTwoFactorAuthStatus = "{\"Status\":\"Authorized\"}";
        stubFor(post(urlEqualTo(statusTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuthStatus)
                        .withBody(responseBodyTwoFactorAuthStatus)
                ));

        var expectedStatusExecute = 200;
        stubFor(post(urlEqualTo(executeTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusExecute)
                ));

        var finalizeStatusExecute = 200;
        stubFor(post(urlEqualTo(finalizeTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(finalizeStatusExecute)
                ));

        var expectedStatusVerifyLogin = 500;
        stubFor(get(urlEqualTo(verifyLoginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusVerifyLogin)
                ));

        Exception exception = assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        String expectedMessage = "Login verification failed";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldAuthenticate() {
        var expectedStatusLogin = 200;
        var responseBodyLogin = "{\"successful\":true,\"errorMessageTitle\":\"\"}";
        stubFor(post(urlEqualTo(loginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusLogin)
                        .withBody(responseBodyLogin)
                ));

        var expectedStatusCsrf = 200;
        var responseBodyCsrf = "{\"antiForgeryToken\":\"testCsrfToken\"}";
        stubFor(get(urlEqualTo(fetchCsrfTokenUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCsrf)
                        .withBody(responseBodyCsrf)
                ));

        var expectedStatusSca = 200;
        var responseBodySca = "{\"ScaAuthorizationId\":\"testScaId\"}";
        stubFor(post(urlEqualTo(fetchScaIdUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusSca)
                        .withBody(responseBodySca)
                ));


        var expectedStatusTwoFactorAuth = 200;
        var responseBodyTwoFactorAuth = "{\"TranId\":\"testTranId\"}";
        stubFor(post(urlEqualTo(beginTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuth)
                        .withBody(responseBodyTwoFactorAuth)
                ));

        var expectedStatusTwoFactorAuthStatus = 200;
        var responseBodyTwoFactorAuthStatus = "{\"Status\":\"Authorized\"}";
        stubFor(post(urlEqualTo(statusTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusTwoFactorAuthStatus)
                        .withBody(responseBodyTwoFactorAuthStatus)
                ));

        var expectedStatusExecute = 200;
        stubFor(post(urlEqualTo(executeTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusExecute)
                ));

        var finalizeStatusExecute = 200;
        stubFor(post(urlEqualTo(finalizeTwoFactorAuthUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(finalizeStatusExecute)
                ));

        var expectedStatusVerifyLogin = 200;
        stubFor(get(urlEqualTo(verifyLoginUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusVerifyLogin)
                ));

        assertDoesNotThrow(() -> authentication.authenticate(credentials));
    }
}
