package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class VergiOranPlaceholder implements IPlaceholder {

    private final String identifier;
    private final ConfigurationManager configManager;

    public VergiOranPlaceholder(String identifier, ConfigurationManager configManager) {
        this.identifier = identifier;
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public String getValue(OfflinePlayer player) {
        double rate = 0.0;
        switch (identifier) {
            case "vergi_yatirma_oran_yuzde":
                rate = configManager.isDepositTaxEnabled() ? configManager.getDepositTaxRate() : 0.0;
                break;
            case "vergi_cekme_oran_yuzde":
                rate = configManager.isWithdrawTaxEnabled() ? configManager.getWithdrawTaxRate() : 0.0;
                break;
            case "vergi_gonderme_oran_yuzde":
                rate = configManager.isSendTaxEnabled() ? configManager.getSendTaxRate() : 0.0;
                break;
        }
        // Yüzde olarak formatla (örn: 0.05 -> %5.0)
        return String.format("%%%s", rate * 100);
    }
}