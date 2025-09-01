package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

public class BakiyeFormattedPlaceholder implements IPlaceholder {
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    @Override
    public @NotNull String getIdentifier() {
        return "bakiye_formatted";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        // NumberFormat.getCurrencyInstance() da kullanılabilir, ancak para birimi simgesi ekler.
        return numberFormat.format(economy.getBalance(player)) + " ⛁";
    }
}