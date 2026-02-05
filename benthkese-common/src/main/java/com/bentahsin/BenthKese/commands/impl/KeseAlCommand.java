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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class KeseAlCommand implements ISubCommand {

    private final BenthKeseCore core;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final BenthConfig config;
    private final IStorageService storageService;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseAlCommand(BenthKeseCore core, MessageManager messageManager, EconomyService economyService, BenthConfig config, IStorageService storageService) {
        this.core = core;
        this.messageManager = messageManager;
        this.economyService = economyService;
        this.config = config;
        this.storageService = storageService;
    }

    @Override
    public String getName() {
        return "al";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.al";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return;
        }

        int requestedAmount = 1;
        if (args.length > 0) {
            try {
                requestedAmount = Integer.parseInt(args[0]);
                if (requestedAmount <= 0) {
                    messageManager.sendMessage(player, "invalid-amount");
                    return;
                }
            } catch (NumberFormatException e) {
                messageManager.sendMessage(player, "invalid-amount");
                return;
            }
        }

        double taxRate = config.taxes.withdraw.enabled ? config.taxes.withdraw.rate : 0.0;
        double estimatedTotalCost = requestedAmount * (1 + taxRate);

        if (!BenthKeseCore.getEconomy().has(player, estimatedTotalCost)) {
            String message = messageManager.getMessage("withdraw.not-enough-money")
                    .replace("{istenen}", numberFormat.format(requestedAmount))
                    .replace("{gereken}", numberFormat.format(estimatedTotalCost));
            player.sendMessage(message);
            return;
        }

        int withdrawnAmount = economyService.withdraw(player, requestedAmount);
        String itemBirim = config.getEconomyMaterial().name().toLowerCase().replace("_", " ");

        if (withdrawnAmount == -1) {
            messageManager.sendMessage(player, "not-enough-money");
        } else if (withdrawnAmount == 0) {
            messageManager.sendMessage(player, "withdraw.inventory-full");
        } else {
            double finalTotalCost = withdrawnAmount * (1 + taxRate);
            double taxAmount = finalTotalCost - withdrawnAmount;

            PlayerData playerData = storageService.getPlayerData(player.getUniqueId());
            playerData.incrementTotalTransactions();
            if (taxAmount > 0) {
                playerData.addTotalTaxPaid(taxAmount);
            }
            storageService.savePlayerData(playerData);

            TransactionData transaction = new TransactionData(
                    player.getUniqueId(),
                    TransactionType.WITHDRAW,
                    withdrawnAmount,
                    itemBirim,
                    System.currentTimeMillis()
            );

            BenthBus.publish(core.getPlugin(), "transaction-log", transaction);

            if (withdrawnAmount < requestedAmount) {
                player.sendMessage(messageManager.getMessage("withdraw.partial-success")
                        .replace("{istenen}", numberFormat.format(requestedAmount))
                        .replace("{verilen}", numberFormat.format(withdrawnAmount))
                        .replace("{birim}", itemBirim)
                );
            } else {
                player.sendMessage(messageManager.getMessage("withdraw-success")
                        .replace("{miktar}", numberFormat.format(withdrawnAmount))
                        .replace("{birim}", itemBirim)
                        .replace("{vergi}", numberFormat.format(taxAmount))
                        .replace("{toplam_maliyet}", numberFormat.format(finalTotalCost))
                );
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}