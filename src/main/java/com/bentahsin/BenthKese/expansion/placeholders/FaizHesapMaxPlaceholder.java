package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FaizHesapMaxPlaceholder implements IPlaceholder {
    private final ConfigurationManager configManager;

    public FaizHesapMaxPlaceholder(ConfigurationManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faiz_hesap_max";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        return String.valueOf(configManager.getMaxInterestAccounts());
    }
}