package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class LimitResetSuresiPlaceholder implements IPlaceholder {
    private final IStorageService storageService;

    public LimitResetSuresiPlaceholder(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "limit_reset_suresi";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        long lastReset = storageService.getPlayerData(player.getUniqueId()).getLastResetTime();
        long nextReset = lastReset + TimeUnit.DAYS.toMillis(1);
        long remaining = nextReset - System.currentTimeMillis();

        return remaining > 0 ? TimeUtil.formatDuration(remaining) : "Şimdi Sıfırlanıyor";
    }
}