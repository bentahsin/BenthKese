/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.expansion;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.expansion.placeholders.*;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BenthKeseExpansion extends PlaceholderExpansion {

    private final BenthKese plugin;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final MessageManager messageManager;
    private final Economy economy = BenthKese.getEconomy();
    private final Map<String, IPlaceholder> placeholders = new HashMap<>();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public BenthKeseExpansion(BenthKese plugin, IStorageService storageService, LimitManager limitManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.messageManager = messageManager;
        registerPlaceholders(storageService, limitManager, messageManager);
    }

    private void registerPlaceholders(IStorageService storageService, LimitManager limitManager, MessageManager messageManager) {
        String infiniteText = messageManager.getMessage("limit-info.infinite-text");
        String maxLevelText = messageManager.getMessage("limit-info.max-level-text");
        String yesText = messageManager.getMessage("general.yes");
        String noText = messageManager.getMessage("general.no");

        // Her bir placeholder'ı oluştur ve kaydet
        addPlaceholder(new LimitSeviyeAdiPlaceholder(storageService, limitManager));
        addPlaceholder(new LimitGondermeKalanPlaceholder(storageService, limitManager, infiniteText));
        addPlaceholder(new LimitSonrakiSeviyeAdiPlaceholder(storageService, limitManager, maxLevelText));
        addPlaceholder(new LimitSonrakiSeviyeUcretPlaceholder(storageService, limitManager));
        addPlaceholder(new LimitSonrakiSeviyeIlerlemePlaceholder(storageService, limitManager));
        addPlaceholder(new LimitYukseltebilirMiPlaceholder(storageService, limitManager, yesText, noText));
        // Mevcut ve gelecekteki tüm placeholder'ları buraya ekle...
    }

    private void addPlaceholder(IPlaceholder placeholder) {
        this.placeholders.put(placeholder.getIdentifier().toLowerCase(), placeholder);
    }

    @Override
    public @NotNull String getIdentifier() { return "benthkese"; }
    @Override
    public @NotNull String getAuthor() { return plugin.getDescription().getAuthors().toString(); }
    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override
    public boolean persist() { return true; }

    @SuppressWarnings("deprecation")
    private OfflinePlayer getTargetPlayer(String name) {
        Player onlineTarget = Bukkit.getPlayerExact(name);
        if (onlineTarget != null) {
            return onlineTarget;
        }
        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // Günlük limit sıfırlama
        PlayerData playerData = storageService.getPlayerData(player.getUniqueId());
        if (System.currentTimeMillis() - playerData.getLastResetTime() > TimeUnit.DAYS.toMillis(1)) {
            playerData.resetDailyLimits();
            storageService.savePlayerData(playerData);
        }

        String lowerParams = params.toLowerCase();

        // Önce tam eşleşen, modüler placeholder'ları kontrol et
        IPlaceholder placeholder = placeholders.get(lowerParams);
        if (placeholder != null) {
            return placeholder.getValue(player);
        }

        // Sonra parametreli placeholder'ları kontrol et
        if (lowerParams.startsWith("bakiye_")) {
            return getBakiye(params);
        }
        if (lowerParams.startsWith("seviye_adi_from_id_")) {
            return getSeviyeAdiFromId(params);
        }
        if (lowerParams.startsWith("seviye_adi_")) { // Bu, "bakiye_"den sonra olmalı
            return getSeviyeAdi(params);
        }

        return null;
    }

    private String getBakiye(String params) {
        String playerName = params.substring("bakiye_".length());
        OfflinePlayer target = getTargetPlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            return messageManager.getMessage("general.player-not-found");
        }
        return numberFormat.format(economy.getBalance(target));
    }

    private String getSeviyeAdi(String params) {
        String playerName = params.substring("seviye_adi_".length());
        OfflinePlayer target = getTargetPlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            return messageManager.getMessage("general.player-not-found");
        }
        int levelId = storageService.getPlayerData(target.getUniqueId()).getLimitLevel();
        LimitLevel level = limitManager.getLimitLevel(levelId);
        return level != null ? level.getName() : "Bilinmeyen Seviye";
    }

    private String getSeviyeAdiFromId(String params) {
        String idStr = params.substring("seviye_adi_from_id_".length());
        try {
            int levelId = Integer.parseInt(idStr);
            LimitLevel level = limitManager.getLimitLevel(levelId);
            return level != null ? level.getName() : messageManager.getMessage("general.invalid-level");
        } catch (NumberFormatException e) {
            return messageManager.getMessage("general.invalid-level");
        }
    }
}