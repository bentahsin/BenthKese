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

public class LimitSonrakiSeviyeIlerlemePlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final Economy economy = BenthKese.getEconomy();

    public LimitSonrakiSeviyeIlerlemePlaceholder(IStorageService storageService, LimitManager limitManager) {
        this.storageService = storageService;
        this.limitManager = limitManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "limit_sonraki_seviye_ilerleme";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        int currentLevelId = storageService.getPlayerData(player.getUniqueId()).getLimitLevel();
        LimitLevel nextLevel = limitManager.getNextLevel(currentLevelId);
        if (nextLevel == null || nextLevel.getCost() <= 0) {
            return "100";
        }
        double balance = economy.getBalance(player);
        double cost = nextLevel.getCost();
        double percentage = (balance / cost) * 100.0;
        return String.format("%.0f", Math.min(100.0, percentage));
    }
}