package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
@Getter
public class AccountGroups {
    @SerializedName("accountsGroups")
    public List<AccountGroup> accountGroups;
}
