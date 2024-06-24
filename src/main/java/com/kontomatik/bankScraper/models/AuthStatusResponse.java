package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class AuthStatusResponse {
    @SerializedName("Status")
    public final String status;

}
