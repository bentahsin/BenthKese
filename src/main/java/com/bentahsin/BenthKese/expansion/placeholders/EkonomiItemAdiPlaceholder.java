package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EkonomiItemAdiPlaceholder implements IPlaceholder {

    private final ConfigurationManager configManager;

    public EkonomiItemAdiPlaceholder(ConfigurationManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ekonomi_item_adi";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        String itemName = configManager.getEconomyItemMaterial().name().replace('_', ' ');
        return WordUtils.capitalizeFully(itemName);
    }
}