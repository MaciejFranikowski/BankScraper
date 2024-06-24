package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

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


    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", name='" + name + '\'' +
                ", customName='" + customName + '\'' +
                '}';
    }
}
