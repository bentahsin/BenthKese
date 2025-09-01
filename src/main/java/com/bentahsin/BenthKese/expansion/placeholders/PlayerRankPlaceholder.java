package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerRankPlaceholder implements IPlaceholder {
    private final IStorageService storageService;

    public PlayerRankPlaceholder(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "siralama_bakiye";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        int rank = storageService.getPlayerBalanceRank(player.getUniqueId());
        return rank > 0 ? "#" + rank : "Sıralamada değil";
    }
}