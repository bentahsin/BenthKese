package com.bentahsin.BenthKese.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BenthConfig extends AbstractYamlConfig {

    public String storageType = "SQLITE";
    public MySQLSettings mysql = new MySQLSettings();
    public String economyItem = "GOLD_INGOT";
    public Taxes taxes = new Taxes();
    public Interest interest = new Interest();

    public BenthConfig(JavaPlugin plugin) {
        super(plugin, "config.yml");
    }

    @Override
    protected void onLoad(YamlDocument document) {
        storageType = document.getString("storage.type", storageType);

        mysql.host = document.getString("storage.mysql.host", mysql.host);
        mysql.port = document.getInt("storage.mysql.port", mysql.port);
        mysql.database = document.getString("storage.mysql.database", mysql.database);
        mysql.username = document.getString("storage.mysql.username", mysql.username);
        mysql.password = document.getString("storage.mysql.password", mysql.password);

        economyItem = document.getString("economy-item", economyItem);

        taxes.deposit.enabled = document.getBoolean("taxes.deposit.enabled", taxes.deposit.enabled);
        taxes.deposit.rate = document.getDouble("taxes.deposit.rate", taxes.deposit.rate);
        taxes.withdraw.enabled = document.getBoolean("taxes.withdraw.enabled", taxes.withdraw.enabled);
        taxes.withdraw.rate = document.getDouble("taxes.withdraw.rate", taxes.withdraw.rate);
        taxes.send.enabled = document.getBoolean("taxes.send.enabled", taxes.send.enabled);
        taxes.send.rate = document.getDouble("taxes.send.rate", taxes.send.rate);

        interest.enabled = document.getBoolean("interest.enabled", interest.enabled);
        interest.maxAccounts = document.getInt("interest.max-accounts-per-player", interest.maxAccounts);
        interest.minDeposit = document.getDouble("interest.min-deposit", interest.minDeposit);
        interest.maxDeposit = document.getDouble("interest.max-deposit", interest.maxDeposit);

        List<Map<String, Object>> parsedRates = new ArrayList<>();
        for (Map<?, ?> rawRate : document.getMapList("interest.rates")) {
            Map<String, Object> rate = new LinkedHashMap<>();
            rawRate.forEach((key, value) -> rate.put(String.valueOf(key), value));
            parsedRates.add(rate);
        }
        if (!parsedRates.isEmpty()) {
            interest.rates = parsedRates;
        }
    }

    /**
     * economy-item değerini Material olarak döndürür.
     * Geçersiz bir değer varsa GOLD_INGOT kullanılır.
     */
    public Material getEconomyMaterial() {
        try {
            return Material.valueOf(economyItem.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.GOLD_INGOT;
        }
    }

    public static class MySQLSettings {
        public String host = "localhost";
        public int port = 3306;
        public String database = "benthkese";
        public String username = "root";
        public String password = "password";
    }

    public static class Taxes {
        public TaxSettings deposit = new TaxSettings(true, 0.01);
        public TaxSettings withdraw = new TaxSettings(true, 0.02);
        public TaxSettings send = new TaxSettings(true, 0.05);
    }

    public static class TaxSettings {
        public boolean enabled;
        public double rate;

        public TaxSettings() {}
        public TaxSettings(boolean enabled, double rate) {
            this.enabled = enabled;
            this.rate = rate;
        }
    }

    public static class Interest {
        public boolean enabled = true;
        public int maxAccounts = 5;
        public double minDeposit = 1000.0;
        public double maxDeposit = 1000000.0;
        public List<Map<String, Object>> rates = List.of(
                createRate("1d", 0.05),
                createRate("7d", 0.12),
                createRate("30d", 0.35)
        );

        private static Map<String, Object> createRate(String time, double rate) {
            return Map.of("time", time, "rate", rate);
        }
    }
}
