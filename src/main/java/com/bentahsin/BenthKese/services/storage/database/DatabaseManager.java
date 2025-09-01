/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services.storage.database;

import com.bentahsin.BenthKese.BenthKese;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class DatabaseManager {

    private final BenthKese plugin;
    private HikariDataSource hikari;

    public DatabaseManager(BenthKese plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        String storageType = Objects.requireNonNull(plugin.getConfig().getString("storage.type", "SQLITE")).toUpperCase();

        if (storageType.equals("MYSQL")) {
            ConfigurationSection mysqlConfig = plugin.getConfig().getConfigurationSection("storage.mysql");
            if (mysqlConfig == null) {
                throw new SQLException("MySQL konfigürasyonu config.yml dosyasında bulunamadı.");
            }
            setupHikariMySql(mysqlConfig);
            plugin.getLogger().info("MySQL veritabanına başarıyla bağlanıldı.");
        } else { // Varsayılan olarak SQLite kullanılacak
            setupHikariSqlite();
            plugin.getLogger().info("SQLite veritabanına başarıyla bağlanıldı.");
        }

        setupTables();
    }

    private void setupHikariMySql(ConfigurationSection config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getString("host") + ":" + config.getInt("port") + "/" + config.getString("database") + "?useSSL=false");
        hikariConfig.setUsername(config.getString("username"));
        hikariConfig.setPassword(config.getString("password"));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(60000);
        this.hikari = new HikariDataSource(hikariConfig);
    }

    private void setupHikariSqlite() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "database.db");
        if (!dbFile.exists()) {
            try {
                File parentDir = dbFile.getParentFile();
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    throw new IOException("Veritabanı klasörü oluşturulamadı: " + parentDir.getAbsolutePath());
                }
                if (!dbFile.createNewFile()) {
                    throw new IOException("SQLite veritabanı dosyası oluşturulamadı.");
                }
            } catch (IOException e) {
                throw new SQLException("SQLite veritabanı dosyası oluşturulamadı: " + dbFile.getAbsolutePath(), e);
            }
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setMaxLifetime(60000);
        hikariConfig.setIdleTimeout(45000);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaxLifetime(60000);
        hikariConfig.setIdleTimeout(45000);
        this.hikari = new HikariDataSource(hikariConfig);
    }

    private void setupTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String createPlayerTableSQL = "CREATE TABLE IF NOT EXISTS benthkese_playerdata (" +
                    "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                    "limit_level INT NOT NULL DEFAULT 1," +
                    "daily_sent DOUBLE NOT NULL DEFAULT 0.0," +
                    "daily_received DOUBLE NOT NULL DEFAULT 0.0," +
                    "last_reset_time BIGINT NOT NULL DEFAULT 0," +
                    "balance DOUBLE NOT NULL DEFAULT 0.0," +
                    "total_transactions INT NOT NULL DEFAULT 0," +
                    "total_sent DOUBLE NOT NULL DEFAULT 0.0," +
                    "total_tax_paid DOUBLE NOT NULL DEFAULT 0.0" +
                    ");";
            stmt.execute(createPlayerTableSQL);

            if (isMySql()) {
                try {
                    stmt.execute("ALTER TABLE benthkese_playerdata ADD COLUMN balance DOUBLE NOT NULL DEFAULT 0.0;");
                } catch (SQLException ignored) {
                    // Sütun zaten varsa hata verir, görmezden gel.
                }
            }

            if (isMySql()) {
                addColumnIfNotExists(stmt, "benthkese_playerdata", "total_transactions", "INT NOT NULL DEFAULT 0");
                addColumnIfNotExists(stmt, "benthkese_playerdata", "total_sent", "DOUBLE NOT NULL DEFAULT 0.0");
                addColumnIfNotExists(stmt, "benthkese_playerdata", "total_tax_paid", "DOUBLE NOT NULL DEFAULT 0.0");
            }

            String createInterestTableSQL = "CREATE TABLE IF NOT EXISTS benthkese_interest_accounts (" +
                    (isMySql() ? "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," : "id INTEGER PRIMARY KEY AUTOINCREMENT,") +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "account_id INT NOT NULL," +
                    "principal DOUBLE NOT NULL," +
                    "interest_rate DOUBLE NOT NULL," +
                    "start_time BIGINT NOT NULL," +
                    "end_time BIGINT NOT NULL," +
                    "UNIQUE(player_uuid, account_id)" +
                    ");";
            stmt.execute(createInterestTableSQL);

            // YENİ TRANSACTION TABLOSU
            String createTransactionTableSQL = "CREATE TABLE IF NOT EXISTS benthkese_transactions (" +
                    (isMySql() ? "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," : "id INTEGER PRIMARY KEY AUTOINCREMENT,") +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "transaction_type INT NOT NULL," + // Enum ordinal'ı saklanacak
                    "amount DOUBLE NOT NULL," +
                    "description VARCHAR(255) NOT NULL," +
                    "timestamp BIGINT NOT NULL" +
                    ");";
            stmt.execute(createTransactionTableSQL);

            // Hızlı sorgular için index ekleyelim
            String createTransactionIndexSQL = "CREATE INDEX IF NOT EXISTS idx_benthkese_transactions_uuid_time ON benthkese_transactions(player_uuid, timestamp);";
            stmt.execute(createTransactionIndexSQL);

            String createNameTableSQL = "CREATE TABLE IF NOT EXISTS benthkese_playernames (" +
                    "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                    "last_known_name VARCHAR(16) NOT NULL" +
                    ");";
            stmt.execute(createNameTableSQL);

            plugin.getLogger().info("Veritabanı tabloları başarıyla oluşturuldu/kontrol edildi.");
        }
    }

    private boolean isMySql() {
        return hikari != null && hikari.getJdbcUrl() != null && hikari.getJdbcUrl().contains("mysql");
    }

    private void addColumnIfNotExists(Statement stmt, String tableName, String columnName, String columnDefinition) {
        try {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition + ";");
        } catch (SQLException ignored) {
            // Sütun zaten varsa hata verir, görmezden gel.
        }
    }

    public Connection getConnection() throws SQLException {
        if (hikari == null) {
            throw new SQLException("Veritabanı bağlantısı henüz kurulmadı.");
        }
        return hikari.getConnection();
    }

    public void close() {
        if (hikari != null && !hikari.isClosed()) {
            hikari.close();
            plugin.getLogger().info("Veritabanı bağlantısı başarıyla kapatıldı.");
        }
    }
}