/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.configuration;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.utils.TimeUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigurationManager {

    private final BenthKese plugin;
    private Material economyItemMaterial;
    private boolean depositTaxEnabled;
    private double depositTaxRate;
    private boolean withdrawTaxEnabled;
    private double withdrawTaxRate;
    private boolean sendTaxEnabled;
    private double sendTaxRate;
    private boolean interestEnabled;
    private int maxInterestAccounts;
    private double minInterestDeposit;
    private double maxInterestDeposit;
    private List<Map<?, ?>> interestRates;

    public ConfigurationManager(BenthKese plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        String materialName = config.getString("economy-item", "GOLD_INGOT");
        try {
            assert materialName != null;
            this.economyItemMaterial = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("config.yml dosyasındaki 'economy-item' geçerli bir materyal değil: " + materialName);
            plugin.getLogger().severe("Varsayılan olarak GOLD_INGOT kullanılacak.");
            this.economyItemMaterial = Material.GOLD_INGOT;
        }

        this.depositTaxEnabled = config.getBoolean("taxes.deposit.enabled", false);
        this.depositTaxRate = config.getDouble("taxes.deposit.rate", 0.0);
        this.withdrawTaxEnabled = config.getBoolean("taxes.withdraw.enabled", false);
        this.withdrawTaxRate = config.getDouble("taxes.withdraw.rate", 0.0);
        this.sendTaxEnabled = config.getBoolean("taxes.send.enabled", false);
        this.sendTaxRate = config.getDouble("taxes.send.rate", 0.0);
        this.interestEnabled = config.getBoolean("interest.enabled", false);
        this.maxInterestAccounts = config.getInt("interest.max-accounts-per-player", 5);
        this.minInterestDeposit = config.getDouble("interest.min-deposit", 1000.0);
        this.maxInterestDeposit = config.getDouble("interest.max-deposit", 1000000.0);
        this.interestRates = config.getMapList("interest.rates");
        this.interestRates.sort((m1, m2) -> {
            long time1 = TimeUtil.parseTime(String.valueOf(m1.get("time")));
            long time2 = TimeUtil.parseTime(String.valueOf(m2.get("time")));
            return Long.compare(time2, time1);
        });
    }

    public Material getEconomyItemMaterial() { return economyItemMaterial; }
    public boolean isDepositTaxEnabled() { return depositTaxEnabled; }
    public double getDepositTaxRate() { return depositTaxRate; }
    public boolean isWithdrawTaxEnabled() { return withdrawTaxEnabled; }
    public double getWithdrawTaxRate() { return withdrawTaxRate; }
    public boolean isSendTaxEnabled() { return sendTaxEnabled; }
    public double getSendTaxRate() { return sendTaxRate; }
    public boolean isInterestEnabled() { return interestEnabled; }
    public int getMaxInterestAccounts() { return maxInterestAccounts; }
    public double getMinInterestDeposit() { return minInterestDeposit; }
    public double getMaxInterestDeposit() { return maxInterestDeposit; }
    public List<Map<?, ?>> getInterestRates() { return interestRates; }
}