package com.kontomatik.bankScraper.mbank;

import com.google.gson.annotations.SerializedName;

import java.util.List;

record AccountGroups(@SerializedName("accountsGroups") List<AccountGroup> accountGroups) {
}
