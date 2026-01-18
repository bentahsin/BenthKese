package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.BenthConfig;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FaizHesapDurumPlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final BenthConfig config;

    public FaizHesapDurumPlaceholder(IStorageService storageService, BenthConfig config) {
        this.storageService = storageService;
        this.config = config;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faiz_hesap_durum";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        int current = storageService.getInterestAccounts(player.getUniqueId()).size();
        int max = config.interest.maxAccounts;
        return current + " / " + max;
    }
}