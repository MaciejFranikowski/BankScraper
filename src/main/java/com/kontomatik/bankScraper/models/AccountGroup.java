package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;
import lombok.ToString;

import java.util.List;
@ToString
public class AccountGroup {
    @SerializedName("accounts")
    public List<Account> accounts;
}
