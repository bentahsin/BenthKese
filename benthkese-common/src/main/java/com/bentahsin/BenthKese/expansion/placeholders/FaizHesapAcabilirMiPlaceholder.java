package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FaizHesapAcabilirMiPlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final ConfigurationManager configManager;

    public FaizHesapAcabilirMiPlaceholder(IStorageService storageService, ConfigurationManager configManager) {
        this.storageService = storageService;
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faiz_hesap_acabilir_mi";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        if (!configManager.isInterestEnabled()) {
            return "false";
        }
        int currentAccounts = storageService.getInterestAccounts(player.getUniqueId()).size();
        boolean canOpen = currentAccounts < configManager.getMaxInterestAccounts();
        return String.valueOf(canOpen);
    }
}