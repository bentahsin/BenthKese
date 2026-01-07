package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class BakiyeRawPlaceholder implements IPlaceholder {
    private final Economy economy = BenthKeseCore.getEconomy();

    @Override
    public @NotNull String getIdentifier() {
        return "bakiye_raw";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        return String.valueOf(economy.getBalance(player));
    }
}