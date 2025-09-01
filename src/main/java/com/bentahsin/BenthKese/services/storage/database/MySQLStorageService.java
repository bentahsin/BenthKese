/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services.storage.database;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.services.LimitManager;

public class MySQLStorageService extends AbstractSqlStorageService {

    public MySQLStorageService(BenthKese plugin, DatabaseManager databaseManager, LimitManager limitManager) {
        super(plugin, databaseManager, limitManager);
    }

    @Override
    protected String getUpsertPlayerStatement() {
        return "INSERT INTO benthkese_playerdata (uuid, limit_level, daily_sent, daily_received, last_reset_time, balance, total_transactions, total_sent, total_tax_paid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "limit_level = VALUES(limit_level), " +
                "daily_sent = VALUES(daily_sent), " +
                "daily_received = VALUES(daily_received), " +
                "last_reset_time = VALUES(last_reset_time), " +
                "balance = VALUES(balance), " +
                "total_transactions = VALUES(total_transactions), " +
                "total_sent = VALUES(total_sent), " +
                "total_tax_paid = VALUES(total_tax_paid);";
    }
}