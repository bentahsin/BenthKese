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
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.data.TransactionData;
import com.bentahsin.BenthKese.data.TransactionType;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KeseGonderCommand implements ISubCommand {
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final ConfigurationManager configmanager;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));


    public KeseGonderCommand(MessageManager messageManager, IStorageService storageService, LimitManager limitManager, ConfigurationManager configManager) {
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.configmanager = configManager;
    }

    @Override
    public String getName() { return "gonder"; }

    @Override
    public String getPermission() { return "benthkese.command.gonder"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return;
        }
        if (args.length != 2) {
            messageManager.sendMessage(sender, "invalid-command-usage");
            return;
        }

        Player senderPlayer = (Player) sender;
        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null) {
            sender.sendMessage(messageManager.getMessage("send-money.player-not-found").replace("{oyuncu}", args[0]));
            return;
        }

        if (senderPlayer.equals(targetPlayer)) {
            messageManager.sendMessage(sender, "send-money.cannot-send-to-self");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            messageManager.sendMessage(sender, "invalid-amount");
            return;
        }

        if (amount <= 0) {
            messageManager.sendMessage(sender, "invalid-amount");
            return;
        }

        double totalCost = amount;
        double taxAmount = 0;
        if (configmanager.isSendTaxEnabled()) {
            taxAmount = amount * configmanager.getSendTaxRate();
            totalCost = amount + taxAmount;
        }

        // --- Gönderen Kontrolleri ---
        PlayerData senderData = storageService.getPlayerData(senderPlayer.getUniqueId());
        if (System.currentTimeMillis() - senderData.getLastResetTime() > TimeUnit.DAYS.toMillis(1)) {
            senderData.resetDailyLimits();
        }
        LimitLevel senderLevel = limitManager.getLimitLevel(senderData.getLimitLevel());
        if (senderLevel.getSendLimit() != -1 && (senderData.getDailySent() + amount > senderLevel.getSendLimit())) {
            sender.sendMessage(messageManager.getMessage("send-money.limit-exceeded")
                    .replace("{asacak_miktar}", numberFormat.format((senderData.getDailySent() + amount) - senderLevel.getSendLimit())));
            return;
        }
        if (economy.getBalance(senderPlayer) < totalCost) {
            messageManager.sendMessage(sender, "not-enough-money");
            return;
        }

        // --- Alıcı Kontrolleri ---
        PlayerData targetData = storageService.getPlayerData(targetPlayer.getUniqueId());
        if (System.currentTimeMillis() - targetData.getLastResetTime() > TimeUnit.DAYS.toMillis(1)) {
            targetData.resetDailyLimits();
        }
        LimitLevel targetLevel = limitManager.getLimitLevel(targetData.getLimitLevel());
        if (targetLevel.getReceiveLimit() != -1 && (targetData.getDailyReceived() + amount > targetLevel.getReceiveLimit())) {
            sender.sendMessage(messageManager.getMessage("send-money.receiver-limit-exceeded").replace("{oyuncu}", targetPlayer.getName()));
            return;
        }

        // --- Vault İşlemi ---
        EconomyResponse response = economy.withdrawPlayer(senderPlayer, totalCost);
        if (response.transactionSuccess()) {
            economy.depositPlayer(targetPlayer, amount);

            // Verileri güncelle
            senderData.addDailySent(amount);
            storageService.savePlayerData(senderData);
            targetData.addDailyReceived(amount);
            storageService.savePlayerData(targetData);

            // İşlem kayıtları
            long timestamp = System.currentTimeMillis();
            storageService.logTransaction(new TransactionData(senderPlayer.getUniqueId(), TransactionType.SEND, amount, targetPlayer.getName(), timestamp));
            storageService.logTransaction(new TransactionData(targetPlayer.getUniqueId(), TransactionType.RECEIVE, amount, senderPlayer.getName(), timestamp));

            // Başarı mesajları
            senderPlayer.sendMessage(messageManager.getMessage("send-money.success-sender")
                    .replace("{oyuncu}", targetPlayer.getName())
                    .replace("{miktar}", numberFormat.format(amount))
                    .replace("{vergi}", numberFormat.format(taxAmount))
                    .replace("{toplam_maliyet}", numberFormat.format(totalCost)));

            targetPlayer.sendMessage(messageManager.getMessage("send-money.success-receiver")
                    .replace("{gonderen}", senderPlayer.getName())
                    .replace("{miktar}", numberFormat.format(amount)));
        } else {
            senderPlayer.sendMessage(messageManager.getMessage("level-up.error").replace("{hata}", response.errorMessage));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String currentArg = args[0].toLowerCase();
            List<String> onlinePlayerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(currentArg, onlinePlayerNames, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}