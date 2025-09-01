/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services.storage;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.data.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class YamlStorageService implements IStorageService {

    private final BenthKese plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private static final int MAX_TRANSACTIONS_TO_KEEP = 50; // YAML dosyasında saklanacak max işlem sayısı

    public YamlStorageService(BenthKese plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                plugin.getLogger().severe("Oyuncu veri klasörü oluşturulamadı! Eklenti düzgün çalışmayabilir.");
            }
        }
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.computeIfAbsent(uuid, this::loadPlayerDataFromFile);
    }

    @Override
    public void savePlayerData(PlayerData playerData) {
        playerDataCache.put(playerData.getUuid(), playerData);
    }

    @Override
    public void loadPlayer(UUID uuid) {
        getPlayerData(uuid);
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        if (playerDataCache.containsKey(uuid)) {
            File playerFile = new File(dataFolder, uuid + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            saveAllDataToConfig(config, uuid);
            try {
                config.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Oyuncu verisi (unload) kaydedilemedi: " + uuid, e);
            }
            playerDataCache.remove(uuid);
        }
    }

    // --- Faiz Hesapları ---
    @Override
    public List<InterestAccount> getInterestAccounts(UUID playerUuid) {
        List<InterestAccount> accounts = new ArrayList<>();
        File playerFile = new File(dataFolder, playerUuid.toString() + ".yml");
        if (!playerFile.exists()) return accounts;

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        ConfigurationSection accountsSection = config.getConfigurationSection("interest-accounts");
        if (accountsSection == null) return accounts;

        for (String key : accountsSection.getKeys(false)) {
            try {
                int accountId = Integer.parseInt(key);
                InterestAccount account = new InterestAccount();
                account.setPlayerUuid(playerUuid);
                account.setAccountId(accountId);
                account.setPrincipal(accountsSection.getDouble(key + ".principal"));
                account.setInterestRate(accountsSection.getDouble(key + ".interest-rate"));
                account.setStartTime(accountsSection.getLong(key + ".start-time"));
                account.setEndTime(accountsSection.getLong(key + ".end-time"));
                accounts.add(account);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Oyuncu " + playerUuid + " için geçersiz faiz hesap ID'si bulundu: " + key);
            }
        }
        return accounts;
    }

    @Override
    public void saveInterestAccount(InterestAccount account) {
        File playerFile = new File(dataFolder, account.getPlayerUuid().toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        String path = "interest-accounts." + account.getAccountId();
        config.set(path + ".principal", account.getPrincipal());
        config.set(path + ".interest-rate", account.getInterestRate());
        config.set(path + ".start-time", account.getStartTime());
        config.set(path + ".end-time", account.getEndTime());
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Faiz hesabı (YAML) kaydedilemedi: " + account.getPlayerUuid(), e);
        }
    }

    @Override
    public void deleteInterestAccount(UUID playerUuid, int accountId) {
        File playerFile = new File(dataFolder, playerUuid.toString() + ".yml");
        if (!playerFile.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set("interest-accounts." + accountId, null);
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Faiz hesabı (YAML) silinemedi: " + playerUuid, e);
        }
    }

    // --- İşlemler ---
    @Override
    public void logTransaction(TransactionData transaction) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File playerFile = new File(dataFolder, transaction.getPlayerUuid() + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            List<Map<?, ?>> rawHistory = config.getMapList("transaction-history");
            List<Map<String, Object>> history = rawHistory.stream()
                    .map(m -> (Map<String, Object>) m)
                    .collect(Collectors.toList());

            Map<String, Object> newTransactionMap = new HashMap<>();
            newTransactionMap.put("type", transaction.getType().ordinal());
            newTransactionMap.put("amount", transaction.getAmount());
            newTransactionMap.put("description", transaction.getDescription());
            newTransactionMap.put("timestamp", transaction.getTimestamp());
            history.add(newTransactionMap);

            while (history.size() > MAX_TRANSACTIONS_TO_KEEP) {
                history.remove(0);
            }

            config.set("transaction-history", history);
            try {
                config.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "İşlem geçmişi (YAML) kaydedilemedi: " + transaction.getPlayerUuid(), e);
            }
        });
    }

    @Override
    public List<TransactionData> getTransactions(UUID playerUuid, int limit) {
        File playerFile = new File(dataFolder, playerUuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return Collections.emptyList();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        List<Map<?, ?>> history = config.getMapList("transaction-history");
        if (history.isEmpty()) {
            return Collections.emptyList();
        }

        List<TransactionData> transactions = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0; i--) {
            if (transactions.size() >= limit) {
                break;
            }

            Map<?, ?> entry = history.get(i);
            try {
                TransactionData data = new TransactionData(
                        playerUuid,
                        TransactionType.values()[(Integer) entry.get("type")],
                        (Double) entry.get("amount"),
                        (String) entry.get("description"),
                        (Long) entry.get("timestamp")
                );
                transactions.add(data);
            } catch (Exception e) {
                plugin.getLogger().warning("Oyuncu " + playerUuid + " için bozuk bir işlem geçmişi kaydı atlandı.");
            }
        }
        return transactions;
    }

    // --- Private Helper Metotlar ---
    private PlayerData loadPlayerDataFromFile(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) return new PlayerData(uuid);

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        PlayerData data = new PlayerData(uuid);
        data.setLimitLevel(config.getInt("limit-level", 1));
        data.setDailySent(config.getDouble("daily-sent", 0.0));
        data.setDailyReceived(config.getDouble("daily-received", 0.0));
        data.setLastResetTime(config.getLong("last-reset-time", System.currentTimeMillis()));

        return data;
    }

    private void saveAllDataToConfig(FileConfiguration config, UUID uuid) {
        PlayerData playerData = playerDataCache.get(uuid);
        if (playerData != null) {
            config.set("limit-level", playerData.getLimitLevel());
            config.set("daily-sent", playerData.getDailySent());
            config.set("daily-received", playerData.getDailyReceived());
            config.set("last-reset-time", playerData.getLastResetTime());
        }
    }

    // --- IStorageService'den Gelen Diğer Metotlar (YAML için Verimsiz Olduğundan Boş Bırakıldı) ---

    @Override
    public List<TopPlayerEntry> getTopPlayersByBalance(int limit) {
        // YAML depolama için çok verimsiz bir işlemdir. Tüm dosyaları taramak gerekir.
        // Bu özellik için veritabanı (MySQL, SQLite) kullanılması önerilir.
        plugin.getLogger().warning("getTopPlayersByBalance metodu YAML depolaması için desteklenmiyor.");
        return Collections.emptyList();
    }

    @Override
    public List<TopPlayerEntry> getTopPlayersByLimitLevel(int limit) {
        // YAML depolama için çok verimsiz bir işlemdir.
        plugin.getLogger().warning("getTopPlayersByLimitLevel metodu YAML depolaması için desteklenmiyor.");
        return Collections.emptyList();
    }

    @Override
    public int getPlayerBalanceRank(UUID uuid) {
        // YAML depolama için çok verimsiz bir işlemdir.
        plugin.getLogger().warning("getPlayerBalanceRank metodu YAML depolaması için desteklenmiyor.");
        return -1; // -1 genellikle 'bulunamadı' veya 'desteklenmiyor' anlamına gelir.
    }

    @Override
    public void updatePlayerName(UUID uuid, String name) {
        // Oyuncu isimleri genellikle ana sunucu tarafından yönetilir ve YAML'da ayrıca saklanmaz.
        // Eğer saklanması gerekiyorsa, buraya bir kayıt mekanizması eklenebilir.
    }

    @Override
    public void updatePlayerBalance(UUID uuid, double balance) {
        // Bakiye, Vault ve ekonomi eklentisi tarafından yönetilir.
        // Bu metodun YAML depolamasında doğrudan bir karşılığı yoktur.
    }
}