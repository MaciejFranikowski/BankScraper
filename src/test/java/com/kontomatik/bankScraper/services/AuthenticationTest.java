package com.kontomatik.bankScraper.services;

import com.google.gson.Gson;
import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.kontomatik.bankScraper.mbank.models.InitTwoFactorResponse;
import com.kontomatik.bankScraper.mbank.models.RequestParams;
import com.kontomatik.bankScraper.mbank.services.MbankAuthentication;
import com.kontomatik.bankScraper.models.Cookies;
import com.kontomatik.bankScraper.models.Credentials;
import org.jsoup.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MbankAuthentication.class})
class AuthenticationTest {

    @SpyBean
    private Gson gson;
    @MockBean
    private JsoupClient jsoupClient;
    @SpyBean
    private ResponseHandler responseHandler;
    @SpyBean
    private UserInteraction userInteraction;
    @Autowired
    private MbankAuthentication authentication;

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

    private RequestParams loginParams;
    private RequestParams csrfParams;
    private RequestParams scaAuthParams;
    private RequestParams twoFactorAuthParams;
    private RequestParams twoFactorAuthStatusParams;
    private RequestParams executeAuthParams;
    private RequestParams finalizeParams;
    private RequestParams verifyLoginParams;
    private Credentials credentials;
    private Cookies cookies;


    @BeforeEach
    void prepareTestData() {
        cookies = mock(Cookies.class);
        gson = new Gson();
        credentials = new Credentials("testuser", "testpassword");
        loginParams = new RequestParams.Builder()
                .data(Map.of("username", credentials.username(), "password", credentials.password()))
                .ignoreContentType(true)
                .build();
        csrfParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
        scaAuthParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
        twoFactorAuthParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .data(Map.of("Data",
                        "{\"scaAuthorizationId\": \"testScaId\"}", "Url",
                        "sca/authorization/disposable", "Method", "POST"))
                .headers(Map.of(
                        "User-Agent", "${userAgent}",
                        "X-Request-Verification-Token", "testCsrfToken"))
                .build();
        twoFactorAuthStatusParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .data(Map.of("TranId", "testTranId"))
                .build();
        executeAuthParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .headers(Map.of("User-Agent",
                        "${userAgent}",
                        "X-Request-Verification-Token", "testCsrfToken"))
                .ignoreContentType(true)
                .requestBody(gson.toJson(new Object()))
                .build();
        finalizeParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .data(Map.of("scaAuthorizationId", "testScaId"))
                .build();
        verifyLoginParams = new RequestParams.Builder()
                .cookies(cookies.getCookies())
                .ignoreContentType(true)
                .build();
    }

    @Test
    void shouldThrowExceptionWhenInitialLoginFails() throws IOException {
        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenThrow(IOException.class);
        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
    }

    @Test
    void shouldThrowExceptionWhenFetchCsrfTokenFails() throws IOException {
        // Given
        Connection.Response response = mock(Connection.Response.class);
        when(response.cookies()).thenReturn(new HashMap<>());

        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenReturn(response);
        when(jsoupClient.sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams))).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        verify(jsoupClient).sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams));
        verify(jsoupClient).sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams));
    }

    @Test
    void shouldThrowExceptionWhenFetchScaAuthorizationDataFails() throws IOException {
        // Given
        Connection.Response response = mock(Connection.Response.class);
        when(response.cookies()).thenReturn(new HashMap<>());

        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenReturn(response);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"antiForgeryToken\":\"testCsrfToken\"}");
        when(jsoupClient.sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams))).thenReturn(csrfResponse);
        // fetchScaAuthorizationData when
        when(jsoupClient.sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams))).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        verify(jsoupClient).sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams));
        verify(jsoupClient).sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams));
        verify(jsoupClient).sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams));
    }

    @Test
    void shouldThrowExceptionWhenInitTwoFactorAuthFails() throws IOException {
        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"antiForgeryToken\":\"testCsrfToken\"}");
        when(jsoupClient.sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams))).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        when(jsoupClient.sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams))).thenReturn(scaResponse);

        when(jsoupClient.sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams))).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        verify(jsoupClient).sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams));
        verify(jsoupClient).sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams));
        verify(jsoupClient).sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams));
        verify(jsoupClient).sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams));
    }

    @Test
    void shouldThrowExceptionWhenWaitForUserAuthenticationFails() throws IOException {
        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"antiForgeryToken\":\"testCsrfToken\"}");
        when(jsoupClient.sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams))).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        when(jsoupClient.sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams))).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams))).thenReturn(twoFactorAuthResponse);

        when(jsoupClient.sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams))).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        verify(jsoupClient).sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams));
        verify(jsoupClient).sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams));
        verify(jsoupClient).sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams));
        verify(jsoupClient).sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams));
        verify(jsoupClient).sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams));
    }

    @Test
    void shouldThrowExceptionWhenFinalizeAuthorizationFails() throws IOException {
        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"antiForgeryToken\":\"testCsrfToken\"}");
        when(jsoupClient.sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams))).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        when(jsoupClient.sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams))).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(initTwoFactorResponse.tranId()).thenReturn("0");
        when(jsoupClient.sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams))).thenReturn(twoFactorAuthResponse);

        // waitForUserAuthentication when
        Connection.Response authStatusResponse = mock(Connection.Response.class);
        when(authStatusResponse.cookies()).thenReturn(new HashMap<>());
        when(authStatusResponse.body()).thenReturn("{\"Status\":\"Authorized\"}");
        when(jsoupClient.sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams))).thenReturn(authStatusResponse);

        Connection.Response executeResponse = mock(Connection.Response.class);
        when(executeResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(executeTwoFactoAuthUrl), eq(Connection.Method.POST), eq(executeAuthParams))).thenReturn(executeResponse);
        when(jsoupClient.sendRequest(eq(scaFinalizeUrl), eq(Connection.Method.POST), eq(finalizeParams))).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        verify(jsoupClient).sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams));
        verify(jsoupClient).sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams));
        verify(jsoupClient).sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams));
        verify(jsoupClient).sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams));
        verify(jsoupClient).sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams));
        verify(jsoupClient).sendRequest(eq(executeTwoFactoAuthUrl), eq(Connection.Method.POST), eq(executeAuthParams));
    }

    @Test
    void shouldThrowExceptionWhenVerifyCorrectLoginFails() throws IOException {
        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"antiForgeryToken\":\"testCsrfToken\"}");
        when(jsoupClient.sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams))).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        when(jsoupClient.sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams))).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(initTwoFactorResponse.tranId()).thenReturn("0");
        when(jsoupClient.sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams))).thenReturn(twoFactorAuthResponse);


        // waitForUserAuthentication when
        Connection.Response authStatusResponse = mock(Connection.Response.class);
        when(authStatusResponse.cookies()).thenReturn(new HashMap<>());
        when(authStatusResponse.body()).thenReturn("{\"Status\":\"Authorized\"}");
        when(jsoupClient.sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams))).thenReturn(authStatusResponse);

        // finalizeAuthorization when
        Connection.Response executeResponse = mock(Connection.Response.class);
        when(executeResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(executeTwoFactoAuthUrl), eq(Connection.Method.POST), eq(executeAuthParams))).thenReturn(executeResponse);
        when(jsoupClient.sendRequest(eq(scaFinalizeUrl), eq(Connection.Method.POST), eq(finalizeParams))).thenReturn(authStatusResponse);

        // verifyCorrectLogin when
        Connection.Response verifyResponse = mock(Connection.Response.class);
        when(verifyResponse.statusCode()).thenReturn(200);
        ArgumentMatcher<String> urlMatcher = url -> url.startsWith("${mbank.init.url}");
        when(jsoupClient.sendRequest(argThat(urlMatcher), eq(Connection.Method.GET), eq(verifyLoginParams))).thenThrow((IOException.class));

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        verify(jsoupClient).sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams));
        verify(jsoupClient).sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams));
        verify(jsoupClient).sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams));
        verify(jsoupClient).sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams));
        verify(jsoupClient).sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams));
        verify(jsoupClient).sendRequest(eq(executeTwoFactoAuthUrl), eq(Connection.Method.POST), eq(executeAuthParams));
        verify(jsoupClient).sendRequest(eq(scaFinalizeUrl), eq(Connection.Method.POST), eq(finalizeParams));
    }

    @Test
    void shouldAuthenticate() throws IOException {
        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"antiForgeryToken\":\"testCsrfToken\"}");
        when(jsoupClient.sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams))).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        when(jsoupClient.sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams))).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(initTwoFactorResponse.tranId()).thenReturn("0");
        when(jsoupClient.sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams))).thenReturn(twoFactorAuthResponse);


        // waitForUserAuthentication when
        Connection.Response authStatusResponse = mock(Connection.Response.class);
        when(authStatusResponse.cookies()).thenReturn(new HashMap<>());
        when(authStatusResponse.body()).thenReturn("{\"Status\":\"Authorized\"}");
        when(jsoupClient.sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams))).thenReturn(authStatusResponse);

        // finalizeAuthorization when
        Connection.Response executeResponse = mock(Connection.Response.class);
        when(executeResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(executeTwoFactoAuthUrl), eq(Connection.Method.POST), eq(executeAuthParams))).thenReturn(executeResponse);
        when(jsoupClient.sendRequest(eq(scaFinalizeUrl), eq(Connection.Method.POST), eq(finalizeParams))).thenReturn(authStatusResponse);

        // verifyCorrectLogin when
        Connection.Response verifyResponse = mock(Connection.Response.class);
        when(verifyResponse.statusCode()).thenReturn(200);
        ArgumentMatcher<String> urlMatcher = url -> url.startsWith("${mbank.init.url}");
        when(jsoupClient.sendRequest(argThat(urlMatcher), eq(Connection.Method.GET), eq(verifyLoginParams))).thenReturn(verifyResponse);

        // Then
        assertDoesNotThrow(() -> authentication.authenticate(credentials));

        verify(jsoupClient).sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams));
        verify(jsoupClient).sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams));
        verify(jsoupClient).sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams));
        verify(jsoupClient).sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams));
        verify(jsoupClient).sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams));
        verify(jsoupClient).sendRequest(eq(executeTwoFactoAuthUrl), eq(Connection.Method.POST), eq(executeAuthParams));
        verify(jsoupClient).sendRequest(eq(scaFinalizeUrl), eq(Connection.Method.POST), eq(finalizeParams));
        verify(jsoupClient).sendRequest(argThat(urlMatcher), eq(Connection.Method.GET), eq(verifyLoginParams));
    }

    @Test
    void shouldThrowExceptionWhenTimeoutReached() throws IOException {
        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(jsoupClient.sendRequest(eq(loginUrl), eq(Connection.Method.POST), eq(loginParams))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"antiForgeryToken\":\"testCsrfToken\"}");
        when(jsoupClient.sendRequest(eq(fetchCsrfTokenUrl), eq(Connection.Method.GET), eq(csrfParams))).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        when(jsoupClient.sendRequest(eq(fetchScaIdUrl), eq(Connection.Method.POST), eq(scaAuthParams))).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(initTwoFactorResponse.tranId()).thenReturn("0");
        when(jsoupClient.sendRequest(eq(beginTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthParams))).thenReturn(twoFactorAuthResponse);

        // waitForUserAuthentication when
        Connection.Response authStatusResponse = mock(Connection.Response.class);
        when(authStatusResponse.cookies()).thenReturn(new HashMap<>());
        when(authStatusResponse.body()).thenReturn("{\"Status\":\"PreAuthorized\"}");
        when(jsoupClient.sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams))).thenReturn(authStatusResponse);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
        verify(jsoupClient, times(30)).sendRequest(eq(statusTwoFactorAuthUrl), eq(Connection.Method.POST), eq(twoFactorAuthStatusParams));
    }
}
