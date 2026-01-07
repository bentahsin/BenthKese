package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.data.InterestAccount;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

public class FaizToplamKazancPlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public FaizToplamKazancPlaceholder(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faiz_toplam_kazanc";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        double totalFinalAmount = storageService.getInterestAccounts(player.getUniqueId())
                .stream()
                .mapToDouble(InterestAccount::getFinalAmount)
                .sum();
        return numberFormat.format(totalFinalAmount) + " ⛁";
    }
}