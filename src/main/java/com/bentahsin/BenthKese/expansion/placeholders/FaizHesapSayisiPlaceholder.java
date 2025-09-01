package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FaizHesapSayisiPlaceholder implements IPlaceholder {
    private final IStorageService storageService;

    public FaizHesapSayisiPlaceholder(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faiz_hesap_sayisi";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        return String.valueOf(storageService.getInterestAccounts(player.getUniqueId()).size());
    }
}