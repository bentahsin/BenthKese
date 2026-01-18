package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.BenthConfig;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FaizHesapAcabilirMiPlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final BenthConfig config;

    public FaizHesapAcabilirMiPlaceholder(IStorageService storageService, BenthConfig config) {
        this.storageService = storageService;
        this.config = config;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faiz_hesap_acabilir_mi";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        if (!config.interest.enabled) {
            return "false";
        }
        int currentAccounts = storageService.getInterestAccounts(player.getUniqueId()).size();
        boolean canOpen = currentAccounts < config.interest.maxAccounts;
        return String.valueOf(canOpen);
    }
}