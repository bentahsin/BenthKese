/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services.storage.database;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.api.IScheduler;
import com.bentahsin.BenthKese.services.LimitManager;

public class SQLiteStorageService extends AbstractSqlStorageService {

    public SQLiteStorageService(BenthKeseCore core, DatabaseManager databaseManager, LimitManager limitManager, IScheduler scheduler) {
        super(core, databaseManager, limitManager, scheduler);
    }

    @Override
    protected String getUpsertPlayerStatement() {
        return "INSERT OR REPLACE INTO benthkese_playerdata (uuid, limit_level, daily_sent, daily_received, last_reset_time, balance, total_transactions, total_sent, total_tax_paid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
    }
}