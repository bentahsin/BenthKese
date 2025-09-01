package com.bentahsin.BenthKese.commands.impl;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.commands.ISubCommand;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.data.TransactionData;
import com.bentahsin.BenthKese.data.TransactionType;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class KeseLimitYukseltCommand implements ISubCommand {

    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseLimitYukseltCommand(MessageManager messageManager, IStorageService storageService, LimitManager limitManager) {
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
    }

    @Override
    public String getName() {
        return "yükselt";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.limit.yukselt";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return;
        }

        Player player = (Player) sender;
        PlayerData playerData = storageService.getPlayerData(player.getUniqueId());
        LimitLevel nextLevel = limitManager.getNextLevel(playerData.getLimitLevel());

        if (nextLevel == null) {
            messageManager.sendMessage(player, "level-up.max-level-reached");
            return;
        }

        double cost = nextLevel.getCost();
        if (!economy.has(player, cost)) {
            String message = messageManager.getMessage("level-up.not-enough-money")
                    .replace("{gereken}", numberFormat.format(cost))
                    .replace("{mevcut}", numberFormat.format(economy.getBalance(player)));
            player.sendMessage(message);
            return;
        }

        EconomyResponse response = economy.withdrawPlayer(player, cost);
        if (response.transactionSuccess()) {
            playerData.setLimitLevel(nextLevel.getLevel());

            // --- İSTATİSTİK GÜNCELLEME ---
            playerData.incrementTotalTransactions();
            // İsteğe bağlı: Seviye yükseltme maliyetini ödenen vergiye ekleyebilirsiniz.
            // playerData.addTotalTaxPaid(cost);
            storageService.savePlayerData(playerData);
            // --- BİTİŞ ---

            // Loglama
            storageService.logTransaction(new TransactionData(player.getUniqueId(), TransactionType.LEVEL_UP, cost, nextLevel.getName(), System.currentTimeMillis()));

            String message = messageManager.getMessage("level-up.success")
                    .replace("{yeni_seviye}", nextLevel.getName());
            player.sendMessage(message);
        } else {
            String message = messageManager.getMessage("level-up.error")
                    .replace("{hata}", response.errorMessage);
            player.sendMessage(message);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}