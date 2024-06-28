package com.kontomatik.bankScraper.services;

import com.google.gson.Gson;
import com.kontomatik.bankScraper.cli.UserInteraction;
import com.kontomatik.bankScraper.mbank.models.AccountGroups;
import com.kontomatik.bankScraper.mbank.models.RequestParams;
import com.kontomatik.bankScraper.mbank.services.MbankScraper;
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MbankScraper.class})
class MbankScraperTest {
    @SpyBean
    private UserInteraction userInteraction;
    @SpyBean
    private Gson gson;
    @SpyBean
    private ResponseHandler responseHandler;
    @MockBean
    private JsoupClient jsoupClient;
    @Autowired
    private MbankScraper mbankScraper;

    @Test
    void shouldScrape() throws Exception {
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
        RequestParams requestParams = new RequestParams.Builder()
                .cookies(new HashMap<>())
                .ignoreContentType(true)
                .build();

        Connection.Response response = mock(Connection.Response.class);
        when(jsoupClient.sendRequest(eq("${mbank.accounts.url}"), eq(Connection.Method.GET), eq(requestParams))).thenReturn(response);
        when(response.body()).thenReturn(jsonString);
        doAnswer(invocation -> {
            String responseBody = invocation.getArgument(0);
            Class<?> responseClass = invocation.getArgument(1);
            return gson.fromJson(responseBody, responseClass);
        }).when(responseHandler).handleResponse(anyString(), eq(AccountGroups.class));

        // When
        mbankScraper.scrape(new Cookies());

        // Then
        verify(jsoupClient).sendRequest(eq("${mbank.accounts.url}"), eq(Connection.Method.GET), eq(requestParams));
        verify(responseHandler).handleResponse(anyString(), eq(AccountGroups.class));
        // Verify the UserInteraction formatting method was called with the correct object
        ArgumentCaptor<AccountGroups> captor = ArgumentCaptor.forClass(AccountGroups.class);
        verify(userInteraction).formatAccountGroups(captor.capture());

        // Verify the captured AccountGroups object matches the expected data
        AccountGroups capturedGroups = captor.getValue();
        assertEquals(2, capturedGroups.accountGroups().size());
        assertEquals("05 1120 2004 0000 3212 7715 8837",
                capturedGroups.accountGroups().getFirst().accounts().getFirst().accountNumber());
        assertEquals(new BigDecimal("1119.15"),
                capturedGroups.accountGroups().getFirst().accounts().getFirst().balance());
    }

    @Test
    void shouldHandleErrorResponse() throws Exception {
        // Given
        RequestParams requestParams = new RequestParams.Builder()
                .cookies(new HashMap<>())
                .ignoreContentType(true)
                .build();
        when(jsoupClient.sendRequest(eq("${mbank.accounts.url}"), eq(Connection.Method.GET), eq(requestParams))).thenThrow(IOException.class);

        // When / Then
        assertThrows(RuntimeException.class, () -> mbankScraper.scrape(new Cookies()));

        // Verify interactions
        verify(jsoupClient).sendRequest(eq("${mbank.accounts.url}"), eq(Connection.Method.GET), eq(requestParams));
        verifyNoInteractions(responseHandler);
        verifyNoInteractions(userInteraction);
    }
}