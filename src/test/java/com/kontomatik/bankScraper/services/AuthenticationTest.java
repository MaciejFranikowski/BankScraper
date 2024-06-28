package com.kontomatik.bankScraper.services;

import com.google.gson.Gson;
import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.exceptions.AuthenticationException;
import com.kontomatik.bankScraper.models.*;
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Authentication.class})
class AuthenticationTest {

    @SpyBean
    private Gson gson;
    @MockBean
    private Cookies cookies;
    @MockBean
    private HttpService httpService;
    @SpyBean
    private ResponseHandler responseHandler;
    @SpyBean
    private UserInteraction userInteraction;
    @Autowired
    private Authentication authentication;

    @Test
    void shouldThrowExceptionWhenInitialLoginFails() throws IOException {
        // Given
        Credentials credentials = new Credentials("testuser", "testpassword");

        when(httpService.sendPostRequest(anyString(), any(Credentials.class))).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
    }

    @Test
    void shouldThrowExceptionWhenFetchCsrfTokenFails() throws IOException {
        // Given
        Credentials credentials = new Credentials("testuser", "testpassword");

        Connection.Response response = mock(Connection.Response.class);
        when(response.cookies()).thenReturn(new HashMap<>());

        when(httpService.sendPostRequest(anyString(), any(Credentials.class))).thenReturn(response);
        when(httpService.sendGetRequest(anyString(), anyMap())).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
    }


    @Test
    void shouldThrowExceptionWhenFetchScaAuthorizationDataFails() throws IOException {
        // Given
        Credentials credentials = new Credentials("testuser", "testpassword");
        Connection.Response response = mock(Connection.Response.class);
        when(response.cookies()).thenReturn(new HashMap<>());

        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendPostRequest(anyString(), any(Credentials.class))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"csrfToken\":\"testCsrfToken\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(CsrfResponse.class));
        when(httpService.sendGetRequest(anyString(),
                anyMap())).thenReturn(csrfResponse);

        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap())).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
    }

    @Test
    void shouldThrowExceptionWhenInitTwoFactorAuthFails() throws IOException {
        // Given
        Credentials credentials = new Credentials("testuser", "testpassword");

        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendPostRequest(anyString(), any(Credentials.class))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"csrfToken\":\"testCsrfToken\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(CsrfResponse.class));
        when(httpService.sendGetRequest(anyString(),
                anyMap())).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(ScaResponse.class));
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap())).thenReturn(scaResponse);

        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap(), anyString())).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
    }

    @Test
    void shouldThrowExceptionWhenWaitForUserAuthenticationFails() throws IOException {
        // Given
        Credentials credentials = new Credentials("testuser", "testpassword");

        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendPostRequest(anyString(), any(Credentials.class))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"csrfToken\":\"testCsrfToken\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(CsrfResponse.class));
        when(httpService.sendGetRequest(anyString(),
                anyMap())).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(ScaResponse.class));
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap())).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(InitTwoFactorResponse.class));
        when(initTwoFactorResponse.getTranId()).thenReturn("0");
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap(), anyString())).thenReturn(twoFactorAuthResponse);

        when(httpService.sendPostRequest(anyString(), eq(Map.of("TranId", "testTranId")), anyMap())).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
    }

    @Test
    void shouldThrowExceptionWhenFinalizeAuthorizationFails() throws IOException {
        // Given
        Credentials credentials = new Credentials("testuser", "testpassword");

        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendPostRequest(anyString(), any(Credentials.class))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"csrfToken\":\"testCsrfToken\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(CsrfResponse.class));
        when(httpService.sendGetRequest(anyString(),
                anyMap())).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(ScaResponse.class));
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap())).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(InitTwoFactorResponse.class));
        when(initTwoFactorResponse.getTranId()).thenReturn("0");
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap(), anyString())).thenReturn(twoFactorAuthResponse);


        // waitForUserAuthentication when
        Connection.Response authStatusResponse = mock(Connection.Response.class);
        when(authStatusResponse.cookies()).thenReturn(new HashMap<>());
        when(authStatusResponse.body()).thenReturn("{\"Status\":\"Authorized\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(AuthStatusResponse.class));
        when(httpService.sendPostRequest(anyString(), eq(Map.of("TranId", "testTranId")), anyMap())).thenReturn(authStatusResponse);

        Connection.Response executeResponse = mock(Connection.Response.class);
        when(executeResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendExecutionRequest(anyString(), anyString(), anyMap(), anyString())).thenReturn(executeResponse);
        when(httpService.sendPostRequest(anyString(),
                eq(Map.of("scaAuthorizationId", "testScaId")),
                anyMap(), eq("testCsrfToken"))).thenThrow(IOException.class);


        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
    }

    @Test
    void shouldThrowExceptionWhenVerifyCorrectLoginFails() throws IOException {
        // Given
        Credentials credentials = new Credentials("testuser", "testpassword");

        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendPostRequest(anyString(), any(Credentials.class))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"csrfToken\":\"testCsrfToken\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(CsrfResponse.class));
        when(httpService.sendGetRequest(anyString(),
                anyMap())).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(ScaResponse.class));
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap())).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(InitTwoFactorResponse.class));
        when(initTwoFactorResponse.getTranId()).thenReturn("0");
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap(), anyString())).thenReturn(twoFactorAuthResponse);


        // waitForUserAuthentication when
        Connection.Response authStatusResponse = mock(Connection.Response.class);
        when(authStatusResponse.cookies()).thenReturn(new HashMap<>());
        when(authStatusResponse.body()).thenReturn("{\"Status\":\"Authorized\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(AuthStatusResponse.class));
        when(httpService.sendPostRequest(anyString(), eq(Map.of("TranId", "testTranId")), anyMap())).thenReturn(authStatusResponse);

        // finalizeAuthorization when
        Connection.Response executeResponse = mock(Connection.Response.class);
        when(executeResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendExecutionRequest(anyString(), anyString(), anyMap(), anyString())).thenReturn(executeResponse);
        when(httpService.sendPostRequest(anyString(),
                eq(Map.of("scaAuthorizationId", "testScaId")),
                anyMap(), eq("testCsrfToken"))).thenReturn(authStatusResponse);

        // verifyCorrectLogin when
        Connection.Response verifyResponse = mock(Connection.Response.class);
        when(verifyResponse.statusCode()).thenReturn(200);
        ArgumentMatcher<String> urlMatcher = url -> url.startsWith(
                "${mbank.init.url}");
        when(httpService.sendGetRequest(argThat(urlMatcher), anyMap())).thenThrow(IOException.class);

        // Then
        assertThrows(AuthenticationException.class, () -> authentication.authenticate(credentials));
    }

    @Test
    void shouldAuthenticate() throws IOException {
        // Given
        Credentials credentials = new Credentials("testuser", "testpassword");

        // initialLogin when
        Connection.Response loginResponse = mock(Connection.Response.class);
        when(loginResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendPostRequest(anyString(), any(Credentials.class))).thenReturn(loginResponse);

        // fetchCsrfToken when
        Connection.Response csrfResponse = mock(Connection.Response.class);
        when(csrfResponse.cookies()).thenReturn(new HashMap<>());
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        when(csrfResponse.body()).thenReturn("{\"csrfToken\":\"testCsrfToken\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(CsrfResponse.class));
        when(httpService.sendGetRequest(anyString(),
                anyMap())).thenReturn(csrfResponse);

        // fetchScaAuthorizationData when
        Connection.Response scaResponse = mock(Connection.Response.class);
        when(scaResponse.cookies()).thenReturn(new HashMap<>());
        when(scaResponse.body()).thenReturn("{\"ScaAuthorizationId\":\"testScaId\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(ScaResponse.class));
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap())).thenReturn(scaResponse);

        // initTwoFactorAuth when
        Connection.Response twoFactorAuthResponse = mock(Connection.Response.class);
        when(twoFactorAuthResponse.cookies()).thenReturn(new HashMap<>());
        InitTwoFactorResponse initTwoFactorResponse = mock(InitTwoFactorResponse.class);
        when(twoFactorAuthResponse.body()).thenReturn("{\"TranId\":\"testTranId\"}");
        when(cookies.getCookies()).thenReturn(new HashMap<>());
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(InitTwoFactorResponse.class));
        when(initTwoFactorResponse.getTranId()).thenReturn("0");
        when(httpService.sendPostRequest(anyString(), anyMap(), anyMap(), anyString())).thenReturn(twoFactorAuthResponse);


        // waitForUserAuthentication when
        Connection.Response authStatusResponse = mock(Connection.Response.class);
        when(authStatusResponse.cookies()).thenReturn(new HashMap<>());
        when(authStatusResponse.body()).thenReturn("{\"Status\":\"Authorized\"}");
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(AuthStatusResponse.class));
        when(httpService.sendPostRequest(anyString(), eq(Map.of("TranId", "testTranId")), anyMap())).thenReturn(authStatusResponse);

        // finalizeAuthorization when
        Connection.Response executeResponse = mock(Connection.Response.class);
        when(executeResponse.cookies()).thenReturn(new HashMap<>());
        when(httpService.sendExecutionRequest(anyString(), anyString(), anyMap(), anyString())).thenReturn(executeResponse);
        when(httpService.sendPostRequest(anyString(),
                eq(Map.of("scaAuthorizationId", "testScaId")),
                anyMap(), eq("testCsrfToken"))).thenReturn(authStatusResponse);

        // verifyCorrectLogin when
        Connection.Response verifyResponse = mock(Connection.Response.class);
        when(verifyResponse.statusCode()).thenReturn(200);
        ArgumentMatcher<String> urlMatcher = url -> url.startsWith(
                "${mbank.init.url}");
        when(httpService.sendGetRequest(argThat(urlMatcher), anyMap())).thenReturn(verifyResponse);


        // Then
        assertDoesNotThrow(() -> authentication.authenticate(credentials));

        verify(httpService).sendPostRequest(anyString(), any(Credentials.class)); // initialLogin
        verify(httpService, times(2)).sendGetRequest(anyString(), anyMap()); // fetchCsrfToken and verifyCorrectLogin
        verify(httpService, times(2)).sendPostRequest(anyString(), anyMap(), anyMap()); // fetchScaAuthorizationData and initTwoFactorAuth
        verify(httpService, times(2)).sendPostRequest(anyString(), anyMap(), anyMap(), anyString()); // initTwoFactorAuth and finalizeAuthorization
        verify(httpService).sendExecutionRequest(anyString(), anyString(), anyMap(), anyString()); // finalizeAuthorization
    }
}
