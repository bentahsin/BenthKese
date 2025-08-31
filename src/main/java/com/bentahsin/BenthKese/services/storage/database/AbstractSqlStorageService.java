/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services.storage.database;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.data.InterestAccount;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.data.TransactionData;
import com.bentahsin.BenthKese.data.TransactionType;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public abstract class AbstractSqlStorageService implements IStorageService {

    private final BenthKese plugin;
    protected final DatabaseManager databaseManager;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();

    private final String SELECT_PLAYER = "SELECT * FROM benthkese_playerdata WHERE uuid = ?;";
    private final String SELECT_ACCOUNTS = "SELECT * FROM benthkese_interest_accounts WHERE player_uuid = ?;";
    private final String INSERT_ACCOUNT = "INSERT INTO benthkese_interest_accounts (player_uuid, account_id, principal, interest_rate, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?);";
    private final String DELETE_ACCOUNT = "DELETE FROM benthkese_interest_accounts WHERE player_uuid = ? AND account_id = ?;";
    private final String INSERT_TRANSACTION = "INSERT INTO benthkese_transactions (player_uuid, transaction_type, amount, description, timestamp) VALUES (?, ?, ?, ?, ?);";
    private final String SELECT_TRANSACTIONS = "SELECT * FROM benthkese_transactions WHERE player_uuid = ? ORDER BY timestamp DESC LIMIT ?;";
    protected abstract String getUpsertPlayerStatement();

    public AbstractSqlStorageService(BenthKese plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        // Önce cache'i kontrol et, yoksa veritabanından yükle
        return playerDataCache.computeIfAbsent(uuid, this::loadPlayerDataFromDatabase);
    }

    @Override
    public void savePlayerData(PlayerData playerData) {
        // Önce cache'i güncelle
        playerDataCache.put(playerData.getUuid(), playerData);
        // Sonra veritabanına asenkron olarak kaydet
        savePlayerDataToDatabaseAsync(playerData);
    }

    @Override
    public void loadPlayer(UUID uuid) {
        // Cache'e yüklemek için asenkron bir görev başlat
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> getPlayerData(uuid));
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            // Oyuncu çıkarken senkron kaydetmek daha güvenlidir
            savePlayerDataToDatabase(data);
            playerDataCache.remove(uuid);
        }
    }

    private PlayerData loadPlayerDataFromDatabase(UUID uuid) {
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_PLAYER)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PlayerData data = new PlayerData(uuid);
                data.setLimitLevel(rs.getInt("limit_level"));
                data.setDailySent(rs.getDouble("daily_sent"));
                data.setDailyReceived(rs.getDouble("daily_received"));
                data.setLastResetTime(rs.getLong("last_reset_time"));
                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu verisi veritabanından yüklenemedi: " + uuid, e);
        }
        // Oyuncu bulunamazsa veya hata olursa, yeni bir PlayerData oluştur
        return new PlayerData(uuid);
    }

    private void savePlayerDataToDatabase(PlayerData data) {
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(getUpsertPlayerStatement())) {
            ps.setString(1, data.getUuid().toString());
            ps.setInt(2, data.getLimitLevel());
            ps.setDouble(3, data.getDailySent());
            ps.setDouble(4, data.getDailyReceived());
            ps.setLong(5, data.getLastResetTime());
            // MySQL için UPSERT'in tekrar parametreye ihtiyacı var
            if (getUpsertPlayerStatement().contains("ON DUPLICATE KEY UPDATE")) {
                ps.setInt(6, data.getLimitLevel());
                ps.setDouble(7, data.getDailySent());
                ps.setDouble(8, data.getDailyReceived());
                ps.setLong(9, data.getLastResetTime());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu verisi veritabanına kaydedilemedi: " + data.getUuid(), e);
        }
    }

    private void savePlayerDataToDatabaseAsync(PlayerData data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlayerDataToDatabase(data));
    }

    @Override
    public List<InterestAccount> getInterestAccounts(UUID playerUuid) {
        List<InterestAccount> accounts = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_ACCOUNTS)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                accounts.add(new InterestAccount(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        playerUuid,
                        rs.getDouble("principal"),
                        rs.getDouble("interest_rate"),
                        rs.getLong("start_time"),
                        rs.getLong("end_time")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncunun faiz hesapları yüklenemedi: " + playerUuid, e);
        }
        return accounts;
    }

    @Override
    public void saveInterestAccount(InterestAccount account) {
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_ACCOUNT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getPlayerUuid().toString());
            ps.setInt(2, account.getAccountId());
            ps.setDouble(3, account.getPrincipal());
            ps.setDouble(4, account.getInterestRate());
            ps.setLong(5, account.getStartTime());
            ps.setLong(6, account.getEndTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Faiz hesabı kaydedilemedi: " + account.getPlayerUuid(), e);
        }
    }

    @Override
    public void deleteInterestAccount(UUID playerUuid, int accountId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> { // Silme işlemi de asenkron olabilir
            try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(DELETE_ACCOUNT)) {
                ps.setString(1, playerUuid.toString());
                ps.setInt(2, accountId);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Faiz hesabı silinemedi: " + playerUuid + " (ID: " + accountId + ")", e);
            }
        });
    }

    @Override
    public void logTransaction(TransactionData transaction) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_TRANSACTION)) {
                ps.setString(1, transaction.getPlayerUuid().toString());
                ps.setInt(2, transaction.getType().ordinal()); // Enum'ın sırasını (integer) kaydet
                ps.setDouble(3, transaction.getAmount());
                ps.setString(4, transaction.getDescription());
                ps.setLong(5, transaction.getTimestamp());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "İşlem geçmişi kaydedilemedi: " + transaction.getPlayerUuid(), e);
            }
        });
    }

    @Override
    public List<TransactionData> getTransactions(UUID playerUuid, int limit) {
        List<TransactionData> transactions = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_TRANSACTIONS)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                transactions.add(new TransactionData(
                        playerUuid,
                        TransactionType.values()[rs.getInt("transaction_type")], // Integer'ı tekrar Enum'a çevir
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "İşlem geçmişi yüklenemedi: " + playerUuid, e);
        }
        return transactions;
    }
}