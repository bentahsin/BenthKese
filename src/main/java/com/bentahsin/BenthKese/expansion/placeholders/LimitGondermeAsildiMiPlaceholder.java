package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LimitGondermeAsildiMiPlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final LimitManager limitManager;

    public LimitGondermeAsildiMiPlaceholder(IStorageService storageService, LimitManager limitManager) {
        this.storageService = storageService;
        this.limitManager = limitManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "limit_gonderme_asildi_mi";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        PlayerData playerData = storageService.getPlayerData(player.getUniqueId());
        LimitLevel limitLevel = limitManager.getLimitLevel(playerData.getLimitLevel());

        if (limitLevel == null || limitLevel.getSendLimit() == -1) {
            return "false"; // Limit yoksa aşılmamıştır.
        }
        boolean isExceeded = playerData.getDailySent() >= limitLevel.getSendLimit();
        return String.valueOf(isExceeded);
    }
}