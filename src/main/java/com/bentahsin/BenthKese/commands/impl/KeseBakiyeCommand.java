/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.impl;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.commands.ISubCommand;
import com.bentahsin.BenthKese.configuration.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class KeseBakiyeCommand implements ISubCommand {

    private final MessageManager messageManager;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseBakiyeCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public String getName() {
        return "bakiye";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.bakiye";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Durum 1: /kese bakiye (Kendi bakiyesine bakar)
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Konsolun bakiyesi yoktur. Lütfen bir oyuncu adı belirtin.");
                return;
            }
            Player player = (Player) sender;
            double balance = economy.getBalance(player);
            player.sendMessage(messageManager.getMessage("balance.self")
                    .replace("{bakiye}", numberFormat.format(balance)));
            return;
        }

        // Durum 2: /kese bakiye <oyuncu> (Başkasının bakiyesine bakar)
        if (!sender.hasPermission("benthkese.command.bakiye.others")) {
            messageManager.sendMessage(sender, "no-permission");
            return;
        }

        OfflinePlayer target = getTargetPlayer(args[0]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(messageManager.getMessage("send-money.player-not-found").replace("{oyuncu}", args[0]));
            return;
        }

        double balance = economy.getBalance(target);
        String targetName = target.getName() != null ? target.getName() : args[0];

        sender.sendMessage(messageManager.getMessage("balance.other")
                .replace("{oyuncu}", targetName)
                .replace("{bakiye}", numberFormat.format(balance)));
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer getTargetPlayer(String name) {
        Player onlineTarget = Bukkit.getPlayerExact(name);
        if (onlineTarget != null) {
            return onlineTarget;
        }
        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("benthkese.command.bakiye.others")) {
            return null; // Sunucunun varsayılan oyuncu adı tamamlama mekanizmasını kullan
        }
        return Collections.emptyList();
    }
}