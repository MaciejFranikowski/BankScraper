package com.kontomatik.bankScraper.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class CsrfResponse {
    public final String csrfToken;
}
