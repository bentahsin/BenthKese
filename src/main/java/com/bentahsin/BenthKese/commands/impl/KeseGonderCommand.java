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
import com.bentahsin.BenthKese.utils.ActionBarUtil; // YENİ IMPORT
import com.bentahsin.BenthKese.utils.TextUtil; // YENİ IMPORT
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.text.NumberFormat;
import java.util.*;
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

        PlayerData targetData = storageService.getPlayerData(targetPlayer.getUniqueId());
        if (System.currentTimeMillis() - targetData.getLastResetTime() > TimeUnit.DAYS.toMillis(1)) {
            targetData.resetDailyLimits();
        }
        LimitLevel targetLevel = limitManager.getLimitLevel(targetData.getLimitLevel());
        if (targetLevel.getReceiveLimit() != -1 && (targetData.getDailyReceived() + amount > targetLevel.getReceiveLimit())) {
            sender.sendMessage(messageManager.getMessage("send-money.receiver-limit-exceeded").replace("{oyuncu}", targetPlayer.getName()));
            return;
        }

        EconomyResponse response = economy.withdrawPlayer(senderPlayer, totalCost);
        if (response.transactionSuccess()) {
            economy.depositPlayer(targetPlayer, amount);

            senderData.addDailySent(amount);
            targetData.addDailyReceived(amount);

            // İstatistik Güncelleme
            senderData.incrementTotalTransactions();
            senderData.addTotalSent(amount);
            if(taxAmount > 0) senderData.addTotalTaxPaid(taxAmount);
            storageService.savePlayerData(senderData);

            targetData.incrementTotalTransactions();
            storageService.savePlayerData(targetData);

            // Loglama
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

            // --- YENİ: ACTION BAR BİLDİRİMLERİ ---
            sendLimitActionBars(senderPlayer, senderData, senderLevel, targetPlayer, targetData, targetLevel);

        } else {
            senderPlayer.sendMessage(messageManager.getMessage("level-up.error").replace("{hata}", response.errorMessage));
        }
    }

    /**
     * Göndericiye ve alıcıya güncel limit durumlarını action bar ile bildirir.
     */
    private void sendLimitActionBars(Player sender, PlayerData senderData, LimitLevel senderLevel, Player receiver, PlayerData receiverData, LimitLevel receiverLevel) {
        String messageTemplate = messageManager.getMessage("actionbar-limit-status");

        // Gönderici için Action Bar
        if (senderLevel.getSendLimit() != -1) {
            Map<String, String> senderPlaceholders = new HashMap<>();
            double kalan = senderLevel.getSendLimit() - senderData.getDailySent();
            senderPlaceholders.put("{kalan}", numberFormat.format(Math.max(0, kalan)));
            senderPlaceholders.put("{toplam}", numberFormat.format(senderLevel.getSendLimit()));
            ActionBarUtil.sendActionBar(sender, TextUtil.replacePlaceholders(messageTemplate, senderPlaceholders));
        }

        // Alıcı için Action Bar
        if (receiverLevel.getReceiveLimit() != -1) {
            Map<String, String> receiverPlaceholders = new HashMap<>();
            double kalan = receiverLevel.getReceiveLimit() - receiverData.getDailyReceived();
            receiverPlaceholders.put("{kalan}", numberFormat.format(Math.max(0, kalan)));
            receiverPlaceholders.put("{toplam}", numberFormat.format(receiverLevel.getReceiveLimit()));
            ActionBarUtil.sendActionBar(receiver, TextUtil.replacePlaceholders(messageTemplate, receiverPlaceholders));
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