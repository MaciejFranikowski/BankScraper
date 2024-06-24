package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AccountGroups {
    @SerializedName("accountsGroups")
    public List<AccountGroup> accountGroups;

    @Override
    public String toString() {
        return "AccountGroups{" +
                "accountGroups=" + accountGroups +
                '}';
    }
}
