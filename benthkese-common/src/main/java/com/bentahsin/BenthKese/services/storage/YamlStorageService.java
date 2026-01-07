/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services.storage;

import com.bentahsin.BenthKese.api.IScheduler;
import com.bentahsin.BenthKese.data.*;
import com.github.benmanes.caffeine.cache.*;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class YamlStorageService implements IStorageService {

    private final JavaPlugin plugin;
    private final IScheduler scheduler;
    private final File dataFolder;
    private static final int MAX_TRANSACTIONS_TO_KEEP = 50;

    private final LoadingCache<UUID, PlayerData> playerDataCache;
    private final Cache<UUID, List<InterestAccount>> interestAccountCache;
    private final Cache<UUID, List<TransactionData>> transactionCache;

    public YamlStorageService(JavaPlugin plugin, IScheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                plugin.getLogger().severe("Oyuncu veri klasörü oluşturulamadı! Eklenti düzgün çalışmayabilir.");
            }
        }

        this.playerDataCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .removalListener((UUID uuid, PlayerData data, RemovalCause cause) -> {
                    if (data != null) {
                        savePlayerDataToFile(data);
                    }
                })
                .build(this::loadPlayerDataFromFile);

        this.interestAccountCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();

        this.transactionCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }

    private YamlDocument getPlayerDocument(UUID uuid) throws IOException {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        return YamlDocument.create(playerFile,
                GeneralSettings.DEFAULT,
                LoaderSettings.DEFAULT,
                DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build(),
                UpdaterSettings.DEFAULT);
    }

    private PlayerData loadPlayerDataFromFile(UUID uuid) {
        try {
            YamlDocument config = getPlayerDocument(uuid);
            PlayerData data = new PlayerData(uuid);
            data.setLimitLevel(config.getInt("player-data.limit-level", 1));
            data.setDailySent(config.getDouble("player-data.daily-sent", 0.0));
            data.setDailyReceived(config.getDouble("player-data.daily-received", 0.0));
            data.setLastResetTime(config.getLong("player-data.last-reset-time", System.currentTimeMillis()));
            data.setTotalTransactions(config.getInt("player-data.total-transactions", 0));
            data.setTotalSent(config.getDouble("player-data.total-sent", 0.0));
            data.setTotalTaxPaid(config.getDouble("player-data.total-tax-paid", 0.0));
            return data;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu veri dosyası yüklenemedi: " + uuid, e);
            return new PlayerData(uuid);
        }
    }

    private void savePlayerDataToFile(PlayerData playerData) {
        if (playerData == null) return;
        try {
            YamlDocument config = getPlayerDocument(playerData.getUuid());
            config.set("player-data.limit-level", playerData.getLimitLevel());
            config.set("player-data.daily-sent", playerData.getDailySent());
            config.set("player-data.daily-received", playerData.getDailyReceived());
            config.set("player-data.last-reset-time", playerData.getLastResetTime());
            config.set("player-data.total-transactions", playerData.getTotalTransactions());
            config.set("player-data.total-sent", playerData.getTotalSent());
            config.set("player-data.total-tax-paid", playerData.getTotalTaxPaid());
            config.save();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu verisi (unload/evict) kaydedilemedi: " + playerData.getUuid(), e);
        }
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    @Override
    public void savePlayerData(PlayerData playerData) {
        playerDataCache.put(playerData.getUuid(), playerData);
        scheduler.runAsync(plugin, () -> savePlayerDataToFile(playerData));
    }

    @Override
    public void loadPlayer(UUID uuid) {
        scheduler.runAsync(plugin, () -> getPlayerData(uuid));
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        playerDataCache.invalidate(uuid);
        playerDataCache.cleanUp();
    }

    @Override
    public List<InterestAccount> getInterestAccounts(UUID playerUuid) {
        List<InterestAccount> cachedList = interestAccountCache.getIfPresent(playerUuid);
        if (cachedList != null) {
            return cachedList;
        }

        List<InterestAccount> accounts = new ArrayList<>();
        try {
            YamlDocument config = getPlayerDocument(playerUuid);
            Section accountsSection = config.getSection("interest-accounts");
            if (accountsSection == null) return accounts;

            for (Object key : accountsSection.getKeys()) {
                try {
                    int accountId = Integer.parseInt(String.valueOf(key));
                    Section accountSection = accountsSection.getSection(String.valueOf(key));
                    if (accountSection != null) {
                        InterestAccount account = new InterestAccount();
                        account.setPlayerUuid(playerUuid);
                        account.setAccountId(accountId);
                        account.setPrincipal(accountSection.getDouble("principal"));
                        account.setInterestRate(accountSection.getDouble("interest-rate"));
                        account.setStartTime(accountSection.getLong("start-time"));
                        account.setEndTime(accountSection.getLong("end-time"));
                        accounts.add(account);
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Oyuncu " + playerUuid + " için geçersiz faiz hesap ID'si bulundu: " + key);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu faiz hesapları yüklenemedi: " + playerUuid, e);
        }

        interestAccountCache.put(playerUuid, accounts);
        return accounts;
    }

    @Override
    public void saveInterestAccount(InterestAccount account) {
        interestAccountCache.invalidate(account.getPlayerUuid());
        scheduler.runAsync(plugin, () -> {
            try {
                YamlDocument config = getPlayerDocument(account.getPlayerUuid());
                String path = "interest-accounts." + account.getAccountId();
                config.set(path + ".principal", account.getPrincipal());
                config.set(path + ".interest-rate", account.getInterestRate());
                config.set(path + ".start-time", account.getStartTime());
                config.set(path + ".end-time", account.getEndTime());
                config.save();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Faiz hesabı (YAML) kaydedilemedi: " + account.getPlayerUuid(), e);
            }
        });
    }

    @Override
    public void deleteInterestAccount(UUID playerUuid, int accountId) {
        interestAccountCache.invalidate(playerUuid);
        scheduler.runAsync(plugin, () -> {
            try {
                YamlDocument config = getPlayerDocument(playerUuid);
                config.set("interest-accounts." + accountId, null);
                config.save();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Faiz hesabı (YAML) silinemedi: " + playerUuid, e);
            }
        });
    }

    @Override
    public List<TransactionData> getTransactions(UUID playerUuid, int limit) {
        List<TransactionData> cachedList = transactionCache.getIfPresent(playerUuid);
        if (cachedList != null) {
            return cachedList.stream().limit(limit).collect(Collectors.toList());
        }

        List<TransactionData> transactions = new ArrayList<>();
        try {
            YamlDocument config = getPlayerDocument(playerUuid);
            List<Map<?, ?>> history = config.getMapList("transaction-history", Collections.emptyList());

            for (int i = history.size() - 1; i >= 0; i--) {
                Map<?, ?> entry = history.get(i);
                try {
                    TransactionData data = new TransactionData(
                            playerUuid,
                            TransactionType.values()[(Integer) entry.get("type")],
                            ((Number) entry.get("amount")).doubleValue(),
                            (String) entry.get("description"),
                            ((Number) entry.get("timestamp")).longValue()
                    );
                    transactions.add(data);
                } catch (Exception e) {
                    plugin.getLogger().warning("Oyuncu " + playerUuid + " için bozuk bir işlem geçmişi kaydı atlandı.");
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu işlem geçmişi yüklenemedi: " + playerUuid, e);
        }

        transactionCache.put(playerUuid, transactions);
        return transactions.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public void logTransaction(TransactionData transaction) {
        transactionCache.invalidate(transaction.getPlayerUuid());

        scheduler.runAsync(plugin, () -> {
            try {
                YamlDocument config = getPlayerDocument(transaction.getPlayerUuid());
                List<Map<String, Object>> history = new ArrayList<>();
                for (Map<?, ?> rawMap : config.getMapList("transaction-history", Collections.emptyList())) {
                    if (rawMap != null) {
                        Map<String, Object> checkedMap = new HashMap<>();
                        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                            if (entry.getKey() instanceof String) {
                                checkedMap.put((String) entry.getKey(), entry.getValue());
                            }
                        }
                        history.add(checkedMap);
                    }
                }

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
                config.save();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "İşlem geçmişi (YAML) kaydedilemedi: " + transaction.getPlayerUuid(), e);
            }
        });
    }

    @Override
    public List<TopPlayerEntry> getTopPlayersByBalance(int limit) {
        plugin.getLogger().warning("getTopPlayersByBalance metodu YAML depolaması için desteklenmiyor.");
        return Collections.emptyList();
    }

    @Override
    public List<TopPlayerEntry> getTopPlayersByLimitLevel(int limit) {
        plugin.getLogger().warning("getTopPlayersByLimitLevel metodu YAML depolaması için desteklenmiyor.");
        return Collections.emptyList();
    }

    @Override
    public int getPlayerBalanceRank(UUID uuid) {
        plugin.getLogger().warning("getPlayerBalanceRank metodu YAML depolaması için desteklenmiyor.");
        return -1;
    }

    @Override
    public void updatePlayerName(UUID uuid, String name) {}

    @Override
    public void updatePlayerBalance(UUID uuid, double balance) {}
}