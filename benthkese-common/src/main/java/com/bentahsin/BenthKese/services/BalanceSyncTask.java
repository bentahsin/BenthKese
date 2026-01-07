package com.bentahsin.BenthKese.services;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periyodik olarak online oyuncuların Vault bakiyelerini
 * liderlik tabloları için veritabanına senkronize eder.
 */
public class BalanceSyncTask extends BukkitRunnable {

    private final IStorageService storageService;
    private final Economy economy = BenthKeseCore.getEconomy();

    public BalanceSyncTask(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            storageService.updatePlayerBalance(player.getUniqueId(), economy.getBalance(player));
        }
    }
}