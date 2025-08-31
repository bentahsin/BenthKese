/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LimitSeviyeAdiPlaceholder implements IPlaceholder {

    private final IStorageService storageService;
    private final LimitManager limitManager;

    public LimitSeviyeAdiPlaceholder(IStorageService storageService, LimitManager limitManager) {
        this.storageService = storageService;
        this.limitManager = limitManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "limit_seviye_adi";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        int levelId = storageService.getPlayerData(player.getUniqueId()).getLimitLevel();
        LimitLevel level = limitManager.getLimitLevel(levelId);
        return level != null ? level.getName() : "Bilinmeyen Seviye";
    }
}