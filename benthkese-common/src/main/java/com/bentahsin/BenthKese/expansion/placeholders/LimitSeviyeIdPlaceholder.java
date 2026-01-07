package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LimitSeviyeIdPlaceholder implements IPlaceholder {
    private final IStorageService storageService;

    public LimitSeviyeIdPlaceholder(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "limit_seviye_id";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        return String.valueOf(storageService.getPlayerData(player.getUniqueId()).getLimitLevel());
    }
}