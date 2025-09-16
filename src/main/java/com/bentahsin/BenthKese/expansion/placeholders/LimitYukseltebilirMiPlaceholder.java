/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LimitYukseltebilirMiPlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final Economy economy = BenthKese.getEconomy();
    private final String yesText;
    private final String noText;

    public LimitYukseltebilirMiPlaceholder(IStorageService storageService, LimitManager limitManager, String yesText, String noText) {
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.yesText = yesText;
        this.noText = noText;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "limit_yukseltebilir_mi";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        int currentLevelId = storageService.getPlayerData(player.getUniqueId()).getLimitLevel();
        LimitLevel nextLevel = limitManager.getNextLevel(currentLevelId);
        if (nextLevel == null) {
            return noText;
        }
        return economy.has(player, nextLevel.getCost()) ? yesText : noText;
    }
}