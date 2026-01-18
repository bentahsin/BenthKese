package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.BenthConfig;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FaizHesapMaxPlaceholder implements IPlaceholder {
    private final BenthConfig config;

    public FaizHesapMaxPlaceholder(BenthConfig config) {
        this.config = config;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faiz_hesap_max";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        return String.valueOf(config.interest.maxAccounts);
    }
}