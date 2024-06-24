package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AccountGroup {
    @SerializedName("accounts")
    public List<Account> accounts;


    @Override
    public String toString() {
        return "AccountGroup{" +
                "accounts=" + accounts +
                '}';
    }
}
