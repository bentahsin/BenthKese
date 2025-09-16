/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.InterestAccount;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.TimeUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class InterestService {

    private final IStorageService storageService;
    private final ConfigurationManager configManager;
    private final MessageManager messageManager;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public InterestService(IStorageService storageService, ConfigurationManager configManager, MessageManager messageManager) {
        this.storageService = storageService;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    /**
     * Oyuncu için yeni bir vadeli hesap oluşturur.
     */
    public void createAccount(Player player, double amount, String durationString) {
        if (!configManager.isInterestEnabled()) {
            messageManager.sendMessage(player, "interest.error-disabled");
            return;
        }

        if (amount < configManager.getMinInterestDeposit() || amount > configManager.getMaxInterestDeposit()) {
            player.sendMessage(messageManager.getMessage("interest.error-amount-range")
                    .replace("{min}", numberFormat.format(configManager.getMinInterestDeposit()))
                    .replace("{max}", numberFormat.format(configManager.getMaxInterestDeposit())));
            return;
        }

        if (!economy.has(player, amount)) {
            messageManager.sendMessage(player, "not-enough-money");
            return;
        }

        List<InterestAccount> currentAccounts = storageService.getInterestAccounts(player.getUniqueId());
        if (currentAccounts.size() >= configManager.getMaxInterestAccounts()) {
            player.sendMessage(messageManager.getMessage("interest.error-max-accounts")
                    .replace("{max}", String.valueOf(configManager.getMaxInterestAccounts())));
            return;
        }

        long durationMillis = TimeUtil.parseTime(durationString);
        if (durationMillis <= 0) {
            messageManager.sendMessage(player, "interest.error-invalid-duration");
            return;
        }

        double applicableRate = -1;
        for (Map<?, ?> rateMap : configManager.getInterestRates()) {
            long rateTimeMillis = TimeUtil.parseTime(String.valueOf(rateMap.get("time")));
            if (durationMillis >= rateTimeMillis) {
                applicableRate = (Double) rateMap.get("rate");
                break;
            }
        }

        if (applicableRate < 0) {
            messageManager.sendMessage(player, "interest.error-no-rate-found");
            return;
        }

        economy.withdrawPlayer(player, amount);

        int nextAccountId = currentAccounts.stream().mapToInt(InterestAccount::getAccountId).max().orElse(0) + 1;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + durationMillis;

        InterestAccount newAccount = new InterestAccount();
        newAccount.setPlayerUuid(player.getUniqueId());
        newAccount.setAccountId(nextAccountId);
        newAccount.setPrincipal(amount);
        newAccount.setInterestRate(applicableRate);
        newAccount.setStartTime(startTime);
        newAccount.setEndTime(endTime);

        storageService.saveInterestAccount(newAccount);

        player.sendMessage(messageManager.getMessage("interest.create-success")
                .replace("{sure}", TimeUtil.formatDuration(durationMillis))
                .replace("{miktar}", numberFormat.format(amount))
                .replace("{kazanc}", numberFormat.format(newAccount.getFinalAmount()))
        );
    }

    /**
     * Vadesi dolmuş bir hesabı çeker veya vadesi gelmemiş bir hesabı bozar.
     */
    public void processAccountAction(Player player, int accountId) {
        Optional<InterestAccount> accountOpt = storageService.getInterestAccounts(player.getUniqueId())
                .stream().filter(acc -> acc.getAccountId() == accountId).findFirst();

        if (!accountOpt.isPresent()) {
            player.sendMessage(messageManager.getMessage("interest.error-account-not-found")
                    .replace("{id}", String.valueOf(accountId)));
            return;
        }

        InterestAccount account = accountOpt.get();
        boolean isMature = System.currentTimeMillis() >= account.getEndTime();

        if (isMature) {
            double finalAmount = account.getFinalAmount();
            economy.depositPlayer(player, finalAmount);
            storageService.deleteInterestAccount(player.getUniqueId(), accountId);
            player.sendMessage(messageManager.getMessage("interest.claim-success")
                    .replace("{id}", String.valueOf(accountId))
                    .replace("{toplam_miktar}", numberFormat.format(finalAmount)));
        } else {
            double principal = account.getPrincipal();
            economy.depositPlayer(player, principal);
            storageService.deleteInterestAccount(player.getUniqueId(), accountId);
            player.sendMessage(messageManager.getMessage("interest.break-success")
                    .replace("{id}", String.valueOf(accountId))
                    .replace("{miktar}", numberFormat.format(principal)));
        }
    }
}