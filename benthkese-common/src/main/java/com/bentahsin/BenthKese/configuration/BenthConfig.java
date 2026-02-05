package com.bentahsin.BenthKese.configuration;

import com.bentahsin.configuration.annotation.*;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ConfigHeader({"BenthKese Ana Ayarlar", "Sürüm 1.0"})
@ConfigVersion(1)
@Backup(enabled = true, onFailure = true, onMigration = true)
public class BenthConfig {

    @Comment("Veri Depolama Ayarları (YAML, SQLITE, MYSQL)")
    @ConfigPath("storage.type")
    public String storageType = "SQLITE";

    @ConfigPath("storage.mysql")
    public MySQLSettings mysql = new MySQLSettings();

    @Comment({"Ekonomi için kullanılacak materyal.", "Örnek: GOLD_INGOT, DIAMOND, EMERALD"})
    @ConfigPath("economy-item")
    public String economyItem = "GOLD_INGOT";

    @ConfigPath("taxes")
    public Taxes taxes = new Taxes();

    @ConfigPath("interest")
    public Interest interest = new Interest();

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
        @Comment("Faiz sistemi aktif mi?")
        public boolean enabled = true;

        @ConfigPath("max-accounts-per-player")
        public int maxAccounts = 5;

        @ConfigPath("min-deposit")
        public double minDeposit = 1000.0;

        @ConfigPath("max-deposit")
        public double maxDeposit = 1000000.0;

        @Comment({"Faiz oranları listesi.", "Format: {time: '7d', rate: 0.10}"})
        public List<Map<String, Object>> rates = Arrays.asList(
                createRate("1d", 0.05),
                createRate("7d", 0.12),
                createRate("30d", 0.35)
        );

        private static Map<String, Object> createRate(String time, double rate) {
            return Map.of("time", time, "rate", rate);
        }
    }
}