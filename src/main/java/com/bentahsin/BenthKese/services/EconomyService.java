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
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EconomyService {

    private final Economy economy = BenthKese.getEconomy();
    private final ConfigurationManager configManager;

    public EconomyService(ConfigurationManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Oyuncunun envanterinden belirtilen miktarda ekonomi eşyasını alır, vergiyi hesaplar ve kalanını Vault'a yatırır.
     * @param player İşlem yapan oyuncu
     * @param amount Yatırılmak istenen miktar
     * @return İşlem sonrası oyuncunun kesesine geçen net miktar. Yetersiz eşya varsa -1 döner.
     */
    public double deposit(Player player, int amount) {
        Material itemType = configManager.getEconomyItemMaterial();
        if (!player.getInventory().containsAtLeast(new ItemStack(itemType), amount)) {
            return -1;
        }

        double netAmount = amount;
        if (configManager.isDepositTaxEnabled()) {
            double tax = amount * configManager.getDepositTaxRate();
            netAmount = amount - tax;
        }

        player.getInventory().removeItem(new ItemStack(itemType, amount));
        economy.depositPlayer(player, netAmount);
        return netAmount;
    }

    /**
     * Oyuncunun envanterindeki tüm ekonomi eşyalarını alır ve Vault hesabına yatırır.
     * @param player İşlem yapan oyuncu
     * @return Envanterden yatırılan toplam eşya miktarı. Hiç eşya yoksa 0 döner.
     */
    public int depositInventory(Player player) {
        Material itemType = configManager.getEconomyItemMaterial();
        int totalAmount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == itemType) {
                totalAmount += item.getAmount();
            }
        }

        if (totalAmount > 0) {
            player.getInventory().removeItem(new ItemStack(itemType, totalAmount));
            economy.depositPlayer(player, totalAmount);
        }
        return totalAmount;
    }

    /**
     * Oyuncunun Vault hesabından para çeker, vergiyi hesaplar ve envanterine ekonomi eşyası olarak verir.
     * @param player İşlem yapan oyuncu
     * @param requestedAmount Çekilmek istenen miktar
     * @return Çekilen gerçek miktar. -1 yetersiz bakiye, 0 envanter dolu, >0 başarılı.
     */
    public int withdraw(Player player, int requestedAmount) {
        double totalCost = requestedAmount;
        if (configManager.isWithdrawTaxEnabled()) {
            totalCost = requestedAmount * (1 + configManager.getWithdrawTaxRate());
        }

        if (!economy.has(player, totalCost)) {
            return -1;
        }

        int freeSpace = getFreeSpaceFor(player.getInventory(), configManager.getEconomyItemMaterial());
        if (freeSpace == 0) {
            return 0;
        }

        int amountToGive = Math.min(requestedAmount, freeSpace);

        if (amountToGive < requestedAmount) {
            totalCost = amountToGive * (1 + configManager.getWithdrawTaxRate());
        }

        economy.withdrawPlayer(player, totalCost);
        player.getInventory().addItem(new ItemStack(configManager.getEconomyItemMaterial(), amountToGive));

        return amountToGive;
    }

    /**
     * Bir envanterde belirli bir materyal için ne kadar boş yer olduğunu hesaplar.
     */
    private int getFreeSpaceFor(PlayerInventory inventory, Material material) {
        int freeSpace = 0;
        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                freeSpace += material.getMaxStackSize();
            } else if (item.getType() == material) {
                freeSpace += item.getMaxStackSize() - item.getAmount();
            }
        }
        return freeSpace;
    }
}