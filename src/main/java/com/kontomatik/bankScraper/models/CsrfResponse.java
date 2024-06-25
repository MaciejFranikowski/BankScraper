package com.kontomatik.bankScraper.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CsrfResponse {
    public final String csrfToken;

}
