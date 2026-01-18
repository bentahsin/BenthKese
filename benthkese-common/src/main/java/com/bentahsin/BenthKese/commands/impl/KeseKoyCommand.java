/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.impl;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.commands.ISubCommand;
import com.bentahsin.BenthKese.configuration.BenthConfig;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.data.TransactionData;
import com.bentahsin.BenthKese.data.TransactionType;
import com.bentahsin.BenthKese.eventbridge.BenthBus;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class KeseKoyCommand implements ISubCommand {

    private final BenthKeseCore core;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final BenthConfig config;
    private final IStorageService storageService;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));


    public KeseKoyCommand(BenthKeseCore core, MessageManager messageManager, EconomyService economyService, BenthConfig config, IStorageService storageService) {
        this.core = core;
        this.messageManager = messageManager;
        this.economyService = economyService;
        this.config = config;
        this.storageService = storageService;
    }

    @Override
    public String getName() {
        return "koy";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.koy";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return;
        }

        String itemBirim = config.economyItem.name().toLowerCase().replace("_", " ");

        if (args.length == 0) {
            handleDepositAmount(player, 1, itemBirim);
            return;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("el") || arg.equals("hand")) {
            handleDepositHand(player, itemBirim);
            return;
        }

        if (arg.equals("envanter")) {
            handleDepositInventory(player, itemBirim);
            return;
        }

        try {
            int amount = Integer.parseInt(arg);
            if (amount <= 0) {
                messageManager.sendMessage(player, "invalid-amount");
                return;
            }
            handleDepositAmount(player, amount, itemBirim);
        } catch (NumberFormatException e) {
            messageManager.sendMessage(player, "invalid-command-usage");
        }
    }

    private void handleDepositAmount(Player player, int amount, String itemBirim) {
        double netAmount = economyService.deposit(player, amount);
        if (netAmount != -1) {
            double taxAmount = amount - netAmount;

            PlayerData playerData = storageService.getPlayerData(player.getUniqueId());
            playerData.incrementTotalTransactions();
            if (taxAmount > 0) {
                playerData.addTotalTaxPaid(taxAmount);
            }
            storageService.savePlayerData(playerData);

            TransactionData transaction = new TransactionData(
                    player.getUniqueId(),
                    TransactionType.DEPOSIT,
                    (double) amount,
                    itemBirim,
                    System.currentTimeMillis()
            );

            BenthBus.publish(core.getPlugin(), "transaction-log", transaction);

            String successMessage = messageManager.getMessage("deposit-success")
                    .replace("{miktar}", numberFormat.format(amount))
                    .replace("{net_miktar}", numberFormat.format(netAmount))
                    .replace("{vergi}", numberFormat.format(taxAmount))
                    .replace("{birim}", itemBirim);
            player.sendMessage(successMessage);
        } else {
            messageManager.sendMessage(player, "not-enough-items");
        }
    }

    private void handleDepositHand(Player player, String itemBirim) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Material economyItem = config.economyItem;

        if (itemInHand.getType() != economyItem) {
            messageManager.sendMessage(player, "deposit.not-holding-economy-item");
            return;
        }

        int amount = itemInHand.getAmount();
        handleDepositAmount(player, amount, itemBirim);
    }

    private void handleDepositInventory(Player player, String itemBirim) {
        int totalAmount = economyService.depositInventory(player);

        if (totalAmount > 0) {
            player.sendMessage(messageManager.getMessage("deposit.inventory-success")
                    .replace("{miktar}", numberFormat.format(totalAmount))
                    .replace("{birim}", itemBirim));
        } else {
            messageManager.sendMessage(player, "deposit.no-economy-item-in-inventory");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("el", "envanter", "1", "16", "64"), new ArrayList<>());
        }
        return new ArrayList<>();
    }
}