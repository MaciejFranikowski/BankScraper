package com.kontomatik.bankScraper.mbank.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record AccountGroups(@SerializedName("accountsGroups") List<AccountGroup> accountGroups) {
}
