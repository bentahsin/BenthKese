package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.BenthConfig;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class VergiOranPlaceholder implements IPlaceholder {

    private final String identifier;
    private final BenthConfig config;

    public VergiOranPlaceholder(String identifier, BenthConfig config) {
        this.identifier = identifier;
        this.config = config;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public String getValue(OfflinePlayer player) {
        double rate = switch (identifier) {
            case "vergi_yatirma_oran_yuzde" ->
                    config.taxes.deposit.enabled ? config.taxes.deposit.rate : 0.0;
            case "vergi_cekme_oran_yuzde" ->
                    config.taxes.withdraw.enabled ? config.taxes.withdraw.rate : 0.0;
            case "vergi_gonderme_oran_yuzde" -> config.taxes.send.enabled ? config.taxes.send.rate : 0.0;
            default -> 0.0;
        };
        return String.format("%%%s", rate * 100);
    }
}