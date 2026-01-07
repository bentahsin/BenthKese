/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.admin.impl;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.commands.admin.IAdminSubCommand;
import com.bentahsin.BenthKese.configuration.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdminBakiyeCommand implements IAdminSubCommand {

    private final Economy economy = BenthKeseCore.getEconomy();
    private final MessageManager messageManager;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public AdminBakiyeCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public String getName() {
        return "bakiye";
    }

    @Override
    public String getPermission() {
        return "benthkese.admin.bakiye";
    }

    @Override
    @SuppressWarnings("deprecation")
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            messageManager.sendMessage(sender, "admin.bakiye.usage");
            return;
        }

        String action = args[0].toLowerCase();
        OfflinePlayer target;
        Player onlineTarget = Bukkit.getPlayer(args[1]);
        target = Objects.requireNonNullElseGet(onlineTarget, () -> Bukkit.getOfflinePlayer(args[1]));

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(messageManager.getMessage("send-money.player-not-found").replace("{oyuncu}", args[1]));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0) {
                messageManager.sendMessage(sender, "invalid-amount");
                return;
            }
        } catch (NumberFormatException e) {
            messageManager.sendMessage(sender, "invalid-amount");
            return;
        }

        String targetName = target.getName() != null ? target.getName() : args[1];
        String formattedAmount = numberFormat.format(amount);

        switch (action) {
            case "ekle":
                economy.depositPlayer(target, amount);
                sender.sendMessage(messageManager.getMessage("admin.bakiye.ekle-success")
                        .replace("{oyuncu}", targetName).replace("{miktar}", formattedAmount));
                break;
            case "cikar":
                economy.withdrawPlayer(target, amount);
                sender.sendMessage(messageManager.getMessage("admin.bakiye.cikar-success")
                        .replace("{oyuncu}", targetName).replace("{miktar}", formattedAmount));
                break;
            case "ayarla":
                double currentBalance = economy.getBalance(target);
                economy.withdrawPlayer(target, currentBalance);
                economy.depositPlayer(target, amount);
                sender.sendMessage(messageManager.getMessage("admin.bakiye.ayarla-success")
                        .replace("{oyuncu}", targetName).replace("{miktar}", formattedAmount));
                break;
            default:
                messageManager.sendMessage(sender, "admin.bakiye.usage");
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("ekle", "cikar", "ayarla"), new ArrayList<>());
        }
        if (args.length == 2) {
            List<String> onlinePlayerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], onlinePlayerNames, new ArrayList<>());
        }
        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], Arrays.asList("100", "1000", "10000"), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}