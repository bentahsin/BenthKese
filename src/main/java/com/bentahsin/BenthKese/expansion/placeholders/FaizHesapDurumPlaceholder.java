package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FaizHesapDurumPlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final ConfigurationManager configManager;

    public FaizHesapDurumPlaceholder(IStorageService storageService, ConfigurationManager configManager) {
        this.storageService = storageService;
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faiz_hesap_durum";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        int current = storageService.getInterestAccounts(player.getUniqueId()).size();
        int max = configManager.getMaxInterestAccounts();
        return current + " / " + max;
    }
}