package com.kontomatik.bankScraper.mbank.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.gson.Gson;
import com.kontomatik.bankScraper.exceptions.ScrapingException;
import com.kontomatik.bankScraper.mbank.MbankScraper;
import com.kontomatik.bankScraper.mbank.Cookies;
import com.kontomatik.bankScraper.services.JsoupClient;
import com.kontomatik.bankScraper.services.ResponseHandler;
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
        MbankScraper.class,
        JsoupClient.class,
        ResponseHandler.class,
        ResponseHandler.class
})
@TestPropertySource("classpath:application-test.properties")
class MbankScraperTest {
    @SpyBean
    private Gson gson;
    @Autowired
    private MbankScraper mbankScraper;

    private WireMockServer wireMockServer;

    @Value("${mbank.wiremock.port}")
    private Integer wireMockPort;

    @Value("${mbank.accounts.url}")
    private String accountsUrl;

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
    void shouldScrape() {
        // Given
        String jsonString = """
                {
                    "accountsGroups":[
                        {
                            "accounts":[
                                {
                                    "accountNumber":"05 1120 2004 0000 3212 7715 8837",
                                    "balance":1119.15,
                                    "currency":"PLN",
                                    "name":"eKonto",
                                    "customName":"eKonto WF"
                                }
                            ],
                            "header":"Personal",
                            "summary":{
                                "currency":"PLN",
                                "isRoundedToOneCurrency":false,
                                "balance":1119.15
                            }
                        },
                        {
                            "accounts":[
                                {
                                    "accountNumber":"49 1240 2004 0000 3312 2296 8860",
                                    "balance":0.00,
                                    "currency":"USD",
                                    "name":"eKonto walutowe USD",
                                    "customName":""
                                },
                                {
                                    "accountNumber":"00 1140 2234 0000 3912 1326 1146",
                                    "balance":0.00,
                                    "currency":"EUR",
                                    "name":"eKonto walutowe EUR",
                                    "customName":""
                                }
                            ],
                            "header":"Foreigns",
                            "summary":{
                                "currency":"PLN",
                                "isRoundedToOneCurrency":true,
                                "balance":0.000000
                            }
                        }
                    ],
                    "summary":{
                        "currency":"PLN",
                        "isRoundedToOneCurrency":true,
                        "balance":1119.150000
                    }
                }
                """;

        var expectedStatus = 200;
        stubFor(get(urlEqualTo(accountsUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                        .withBody(jsonString)
                ));

        assertDoesNotThrow(() -> mbankScraper.scrape(new Cookies()));
    }

    @Test
    void shouldThrowException() {
        // Given
        var expectedStatus = 500;
        stubFor(get(urlEqualTo(accountsUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                ));

        Exception exception = assertThrows(ScrapingException.class, () -> mbankScraper.scrape(new Cookies()));
        String expectedMessage = "An error has occurred during scraping: AccountGroups is null";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldHandleMalformedData() {
        // Given
        var expectedStatus = 200;
        stubFor(get(urlEqualTo(accountsUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus)
                        .withBody("malformed data")
                ));

        Exception exception = assertThrows(ScrapingException.class, () -> mbankScraper.scrape(new Cookies()));
        String expectedMessage = "An error has occurred during scraping: Something went wrong while parsing the response body";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

}