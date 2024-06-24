package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class InitTwoFactorResponse {
    @SerializedName("TranId")
    public final String tranId;
}
