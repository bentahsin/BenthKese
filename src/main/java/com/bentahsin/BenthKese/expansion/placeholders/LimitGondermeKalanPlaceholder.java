/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

public class LimitGondermeKalanPlaceholder implements IPlaceholder {

    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
    private final String infiniteText;

    public LimitGondermeKalanPlaceholder(IStorageService storageService, LimitManager limitManager, String infiniteText) {
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.infiniteText = infiniteText;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "limit_gonderme_kalan";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        PlayerData playerData = storageService.getPlayerData(player.getUniqueId());
        LimitLevel currentLevel = limitManager.getLimitLevel(playerData.getLimitLevel());

        if (currentLevel == null) return "0";
        if (currentLevel.getSendLimit() == -1) return infiniteText;

        double kalan = currentLevel.getSendLimit() - playerData.getDailySent();
        return numberFormat.format(Math.max(0, kalan));
    }
}