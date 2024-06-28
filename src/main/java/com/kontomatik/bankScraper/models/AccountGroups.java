package com.kontomatik.bankScraper.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record AccountGroups(@SerializedName("accountsGroups") List<AccountGroup> accountGroups) {
}
