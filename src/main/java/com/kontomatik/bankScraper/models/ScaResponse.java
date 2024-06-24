package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScaResponse {
    @SerializedName("ScaAuthorizationId")
    public final String scaAuthorizationId;
}
