/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.admin.impl;

import com.bentahsin.BenthKese.commands.admin.IAdminSubCommand;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdminLimitCommand implements IAdminSubCommand {

    private final IStorageService storageService;
    private final MessageManager messageManager;
    private final LimitManager limitManager;

    public AdminLimitCommand(IStorageService storageService, MessageManager messageManager, LimitManager limitManager) {
        this.storageService = storageService;
        this.messageManager = messageManager;
        this.limitManager = limitManager;
    }

    @Override
    public String getName() {
        return "limit";
    }

    @Override
    public String getPermission() {
        return "benthkese.admin.limit";
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 3 || !args[0].equalsIgnoreCase("set")) {
            messageManager.sendMessage(sender, "admin.limit.usage");
            return;
        }

        OfflinePlayer target;
        Player onlineTarget = Bukkit.getPlayer(args[1]);
        if (onlineTarget != null) {
            target = onlineTarget;
        } else {
            target = Bukkit.getOfflinePlayer(args[1]);
        }

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(messageManager.getMessage("send-money.player-not-found").replace("{oyuncu}", args[1]));
            return;
        }

        int newLevel;
        try {
            newLevel = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            messageManager.sendMessage(sender, "invalid-amount");
            return;
        }

        if (limitManager.getLimitLevel(newLevel) == null) {
            sender.sendMessage(messageManager.getMessage("admin.limit.level-not-exist").replace("{seviye}", String.valueOf(newLevel)));
            return;
        }

        PlayerData playerData = storageService.getPlayerData(target.getUniqueId());
        playerData.setLimitLevel(newLevel);
        storageService.savePlayerData(playerData);

        String targetName = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage(messageManager.getMessage("admin.limit.set-success")
                .replace("{oyuncu}", targetName)
                .replace("{seviye}", String.valueOf(newLevel)));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Collections.singletonList("set"), new ArrayList<>());
        }
        if (args.length == 2) {
            List<String> onlinePlayerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], onlinePlayerNames, new ArrayList<>());
        }
        if (args.length == 3) {
            List<String> levelSuggestions = limitManager.getAllLevels().keySet().stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[2], levelSuggestions, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}