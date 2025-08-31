/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.impl;

import com.bentahsin.BenthKese.commands.ISubCommand;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class KeseLimitGorCommand implements ISubCommand {

    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseLimitGorCommand(MessageManager messageManager, IStorageService storageService, LimitManager limitManager) {
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
    }

    @Override
    public String getName() {
        return "gor";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.limit.gor";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return;
        }
        Player player = (Player) sender;
        PlayerData playerData = storageService.getPlayerData(player.getUniqueId());

        // Limitin sıfırlanma zamanı gelmiş mi diye kontrol et
        if (System.currentTimeMillis() - playerData.getLastResetTime() > TimeUnit.DAYS.toMillis(1)) {
            playerData.resetDailyLimits();
            storageService.savePlayerData(playerData); // Değişikliği kaydet
            messageManager.sendMessage(player, "limit-status.resetted");
        }

        LimitLevel currentLevel = limitManager.getLimitLevel(playerData.getLimitLevel());
        if (currentLevel == null) {
            player.sendMessage("§cHata: Mevcut limit seviyeniz bulunamadı. Lütfen bir yetkiliye bildirin.");
            return;
        }

        String infiniteText = messageManager.getMessage("limit-info.infinite-text");
        String maxSend = currentLevel.getSendLimit() == -1 ? infiniteText : numberFormat.format(currentLevel.getSendLimit());
        String maxReceive = currentLevel.getReceiveLimit() == -1 ? infiniteText : numberFormat.format(currentLevel.getReceiveLimit());

        List<String> messageList = messageManager.getMessageList("limit-status.info");
        for (String line : messageList) {
            sender.sendMessage(line
                    .replace("{seviye_adi}", currentLevel.getName())
                    .replace("{gonderme_kullanilan}", numberFormat.format(playerData.getDailySent()))
                    .replace("{gonderme_max}", maxSend)
                    .replace("{alma_kullanilan}", numberFormat.format(playerData.getDailyReceived()))
                    .replace("{alma_max}", maxReceive)
            );
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}