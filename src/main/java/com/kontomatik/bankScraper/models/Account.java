package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;
import lombok.ToString;

@ToString
public class Account {
    @SerializedName("accountNumber")
    public String accountNumber;

    @SerializedName("balance")
    public double balance;

    @SerializedName("currency")
    public String currency;

    @SerializedName("name")
    public String name;

    @SerializedName("customName")
    public String customName;

}
