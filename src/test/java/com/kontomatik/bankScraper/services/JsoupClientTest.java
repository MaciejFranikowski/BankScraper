package com.kontomatik.bankScraper.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.kontomatik.bankScraper.mbank.models.RequestParams;
import org.jsoup.Connection;
import org.jsoup.UnsupportedMimeTypeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JsoupClient.class})
@TestPropertySource("classpath:application-test.properties")
class JsoupClientTest {

    @Autowired
    private JsoupClient jsoupClient;

    private WireMockServer wireMockServer;

    @Value("${mbank.wiremock.port}")
    private Integer wireMockPort;

    @Value("${mbank.base.url}")
    private String baseUrl;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(wireMockPort);
        wireMockServer.start();
        configureFor("localhost", wireMockPort);
    }

    @AfterEach
    void cleanup() {
        wireMockServer.stop();
    }

    @Test
    void shouldSendAndReturnCorrectStatus() throws IOException {
        var expectedStatus = 200;

        stubFor(get(urlEqualTo("/responseWithoutBody"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                ));

        RequestParams params = new RequestParams.
                Builder().
                ignoreContentType(true).
                build();
        var actual = jsoupClient.sendRequest(baseUrl + "/responseWithoutBody", Connection.Method.GET, params).statusCode();

        verify(getRequestedFor(urlEqualTo("/responseWithoutBody")));
        assertEquals(expectedStatus, actual);
    }

    @Test
    void shouldReturnBody() throws IOException {
        var expectedStatus = 200;
        var expectedBody = "{'key': 'value'}";

        stubFor(get(urlEqualTo("/responseWithBody"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                        .withBody(expectedBody)
                ));

        RequestParams params = new RequestParams.
                Builder().
                ignoreContentType(true).
                build();
        var actual = jsoupClient.sendRequest(baseUrl + "/responseWithBody", Connection.Method.GET, params).body();

        verify(getRequestedFor(urlEqualTo("/responseWithBody")));
        assertEquals(expectedBody, actual);
    }

    @Test
    void shouldReturnCookies() throws IOException {
        var expectedStatus = 200;
        var expectedBody = "{'key': 'value'}";
        var expectedCookies = "{cookie1=value1, cookie2=value2}";

        stubFor(get(urlEqualTo("/responseWithCookies"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                        .withBody(expectedBody)
                        .withHeader("Set-Cookie", "cookie1=value1")
                        .withHeader("Set-Cookie", "cookie2=value2")
                ));

        RequestParams params = new RequestParams.
                Builder().
                ignoreContentType(true).
                build();
        var actual = jsoupClient.sendRequest(baseUrl + "/responseWithCookies", Connection.Method.GET, params).cookies();

        verify(getRequestedFor(urlEqualTo("/responseWithCookies")));
        assertEquals(expectedCookies, actual.toString());
    }

    @Test
    void shouldReturnHeaders() throws IOException {
        var expectedStatus = 200;
        var expectedBody = "{'key': 'value'}";
        var expectedHeaders = Map.of("header1", "value1", "header2", "value2");

        stubFor(get(urlEqualTo("/responseWithHeaders"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                        .withBody(expectedBody)
                        .withHeader("header1", "value1")
                        .withHeader("header2", "value2")
                ));

        RequestParams params = new RequestParams.
                Builder().
                ignoreContentType(true).
                build();
        var actualHeaders = jsoupClient.sendRequest(baseUrl + "/responseWithHeaders", Connection.Method.GET, params).headers();

        verify(getRequestedFor(urlEqualTo("/responseWithHeaders")));
        expectedHeaders.forEach((key, value) -> assertEquals(value, actualHeaders.get(key)));
    }

    @Test
    void shouldSendHeadersAndBody() throws IOException {
        var expectedStatus = 200;
        var expectedBody = "{'key': 'value'}";
        var requestHeaders = Map.of("header1", "value1", "header2", "value2");
        var expectedRequestBody = "{\"key\": \"value\"}";

        stubFor(post(urlEqualTo("/responseWithHeadersAndBody"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                        .withBody(expectedBody)
                ));

        RequestParams params = new RequestParams.
                Builder().
                ignoreContentType(true).
                requestBody(expectedRequestBody).
                headers(requestHeaders).
                build();
        var actual = jsoupClient.sendRequest(baseUrl + "/responseWithHeadersAndBody", Connection.Method.POST, params).statusCode();

        verify(postRequestedFor(urlEqualTo("/responseWithHeadersAndBody"))
                .withHeader("header1", equalTo("value1"))
                .withHeader("header2", equalTo("value2"))
                .withRequestBody(equalToJson(expectedRequestBody)));
        assertEquals(expectedStatus, actual);
    }

    @Test
    void shouldSendCookies() throws IOException {
        var expectedStatus = 200;
        var expectedBody = "{'key': 'value'}";
        var requestCookies = Map.of("cookie1", "value1", "cookie2", "value2");

        stubFor(post(urlEqualTo("/responseWithCookies"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                        .withBody(expectedBody)
                ));

        RequestParams params = new RequestParams.
                Builder().
                ignoreContentType(true).
                cookies(requestCookies).
                build();
        jsoupClient.sendRequest(baseUrl + "/responseWithCookies", Connection.Method.POST, params).cookies();

        verify(postRequestedFor(urlEqualTo("/responseWithCookies"))
                .withCookie("cookie1", equalTo("value1"))
                .withCookie("cookie2", equalTo("value2")));
    }

    @Test
    void shouldHandle404StatusCode() throws IOException {
        var expectedStatus = 404;

        stubFor(get(urlEqualTo("/nonexistent"))
                .willReturn(aResponse().withStatus(expectedStatus)));

        RequestParams params = new RequestParams.
                Builder().
                ignoreContentType(true).
                build();

        var actual = jsoupClient.sendRequest(baseUrl + "/nonexistent", Connection.Method.GET, params).statusCode();

        verify(getRequestedFor(urlEqualTo("/nonexistent")));
        assertEquals(expectedStatus, actual);
    }

    @Test
    void shouldThrowUnsupportedMimeTypeException() throws IOException {
        var expectedStatus = 200;

        stubFor(get(urlEqualTo("/emptyJsonResponse"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                ));

        // without ignoring content type (Jsoup only works with text/* and application/xml)
        RequestParams params = new RequestParams.
                Builder().
                build();

        assertThrows(UnsupportedMimeTypeException.class,
                () -> jsoupClient.sendRequest(baseUrl + "/emptyJsonResponse", Connection.Method.GET, params));
    }
}
