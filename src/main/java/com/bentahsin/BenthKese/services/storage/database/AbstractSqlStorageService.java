/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services.storage.database;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.data.*;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.github.benmanes.caffeine.cache.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public abstract class AbstractSqlStorageService implements IStorageService {

    protected final BenthKese plugin;
    protected final DatabaseManager databaseManager;
    private final Economy economy = BenthKese.getEconomy();
    private final LimitManager limitManager;

    private final LoadingCache<UUID, PlayerData> playerDataCache;
    private final Cache<String, List<TopPlayerEntry>> topListCache;


    private final String SELECT_PLAYER = "SELECT * FROM benthkese_playerdata WHERE uuid = ?;";
    private final String SELECT_ACCOUNTS = "SELECT * FROM benthkese_interest_accounts WHERE player_uuid = ?;";
    private final String INSERT_ACCOUNT = "INSERT INTO benthkese_interest_accounts (player_uuid, account_id, principal, interest_rate, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?);";
    private final String DELETE_ACCOUNT = "DELETE FROM benthkese_interest_accounts WHERE player_uuid = ? AND account_id = ?;";
    private final String INSERT_TRANSACTION = "INSERT INTO benthkese_transactions (player_uuid, transaction_type, amount, description, timestamp) VALUES (?, ?, ?, ?, ?);";
    private final String SELECT_TRANSACTIONS = "SELECT * FROM benthkese_transactions WHERE player_uuid = ? ORDER BY timestamp DESC LIMIT ?;";
    private final String UPSERT_PLAYER_NAME;
    private final String SELECT_TOP_BALANCE = "SELECT pn.last_known_name, pd.balance FROM benthkese_playerdata pd JOIN benthkese_playernames pn ON pd.uuid = pn.uuid ORDER BY pd.balance DESC, pn.last_known_name ASC LIMIT ?;";
    private final String SELECT_TOP_LEVEL = "SELECT pd.limit_level, pn.last_known_name FROM benthkese_playerdata pd JOIN benthkese_playernames pn ON pd.uuid = pn.uuid ORDER BY pd.limit_level DESC, pn.last_known_name ASC LIMIT ?;";
    private final String GET_BALANCE_RANK = "SELECT COUNT(1) + 1 AS rank FROM benthkese_playerdata WHERE balance > (SELECT balance FROM benthkese_playerdata WHERE uuid = ?);";
    private final String UPDATE_BALANCE;

    protected abstract String getUpsertPlayerStatement();

    public AbstractSqlStorageService(BenthKese plugin, DatabaseManager databaseManager, LimitManager limitManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.limitManager = limitManager;

        boolean isMysql = getUpsertPlayerStatement().contains("ON DUPLICATE KEY UPDATE");

        UPSERT_PLAYER_NAME = isMysql
                ? "INSERT INTO benthkese_playernames (uuid, last_known_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE last_known_name = VALUES(last_known_name);"
                : "INSERT OR REPLACE INTO benthkese_playernames (uuid, last_known_name) VALUES (?, ?);";

        UPDATE_BALANCE = "UPDATE benthkese_playerdata SET balance = ? WHERE uuid = ?;";

        this.playerDataCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .removalListener((UUID uuid, PlayerData data, RemovalCause cause) -> {
                    if (data != null) {
                        savePlayerDataToDatabase(data);
                    }
                })
                .build(this::loadPlayerDataFromDatabase);

        this.topListCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10)
                .build();
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    @Override
    public void savePlayerData(PlayerData playerData) {
        playerDataCache.put(playerData.getUuid(), playerData);
        savePlayerDataToDatabaseAsync(playerData);
    }

    @Override
    public void loadPlayer(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> getPlayerData(uuid));
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        playerDataCache.invalidate(uuid);
        playerDataCache.cleanUp();
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
                data.setBalance(rs.getDouble("balance"));
                data.setTotalTransactions(rs.getInt("total_transactions"));
                data.setTotalSent(rs.getDouble("total_sent"));
                data.setTotalTaxPaid(rs.getDouble("total_tax_paid"));
                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu verisi veritabanından yüklenemedi: " + uuid, e);
        }
        return new PlayerData(uuid);
    }

    private void savePlayerDataToDatabase(PlayerData data) {
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(getUpsertPlayerStatement())) {
            ps.setString(1, data.getUuid().toString());
            ps.setInt(2, data.getLimitLevel());
            ps.setDouble(3, data.getDailySent());
            ps.setDouble(4, data.getDailyReceived());
            ps.setLong(5, data.getLastResetTime());
            ps.setDouble(6, data.getBalance());
            ps.setInt(7, data.getTotalTransactions());
            ps.setDouble(8, data.getTotalSent());
            ps.setDouble(9, data.getTotalTaxPaid());
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_ACCOUNT)) {
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
        });
    }

    @Override
    public void deleteInterestAccount(UUID playerUuid, int accountId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
                ps.setInt(2, transaction.getType().ordinal());
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
                        TransactionType.values()[rs.getInt("transaction_type")],
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

    @Override
    public List<TopPlayerEntry> getTopPlayersByBalance(int limit) {
        String cacheKey = "balance_" + limit;
        List<TopPlayerEntry> cachedList = topListCache.getIfPresent(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        List<TopPlayerEntry> topPlayers = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_TOP_BALANCE)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                topPlayers.add(new TopPlayerEntry(
                        rs.getString("last_known_name"),
                        rs.getDouble("balance")
                ));
            }
            topListCache.put(cacheKey, topPlayers);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Bakiye liderlik tablosu yüklenemedi", e);
        }
        return topPlayers;
    }

    @Override
    public List<TopPlayerEntry> getTopPlayersByLimitLevel(int limit) {
        String cacheKey = "level_" + limit;
        List<TopPlayerEntry> cachedList = topListCache.getIfPresent(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        List<TopPlayerEntry> topPlayers = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_TOP_LEVEL)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LimitLevel level = limitManager.getLimitLevel(rs.getInt("limit_level"));
                String levelName = level != null ? level.getName() : "Bilinmiyor";
                topPlayers.add(new TopPlayerEntry(
                        rs.getString("last_known_name"),
                        levelName
                ));
            }
            topListCache.put(cacheKey, topPlayers);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Seviye liderlik tablosu yüklenemedi", e);
        }
        return topPlayers;
    }

    @Override
    public int getPlayerBalanceRank(UUID uuid) {
        try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(GET_BALANCE_RANK)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("rank");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu sıralaması alınamadı: " + uuid, e);
        }
        return 0;
    }

    @Override
    public void updatePlayerBalance(UUID uuid, double balance) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(UPDATE_BALANCE)) {
                ps.setDouble(1, balance);
                ps.setString(2, uuid.toString());
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    PlayerData newPlayerData = new PlayerData(uuid);
                    newPlayerData.setBalance(economy.getBalance(player));
                    savePlayerData(newPlayerData);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Oyuncu bakiyesi güncellenemedi: " + uuid, e);
            }
        });
    }

    @Override
    public void updatePlayerName(UUID uuid, String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = databaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(UPSERT_PLAYER_NAME)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Oyuncu adı güncellenemedi: " + uuid, e);
            }
        });
    }
}