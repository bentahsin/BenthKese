/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services.storage;

import com.bentahsin.BenthKese.data.InterestAccount;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.data.TransactionData;
import java.util.List;
import java.util.UUID;

public interface IStorageService {
    PlayerData getPlayerData(UUID uuid);
    void savePlayerData(PlayerData playerData);
    void loadPlayer(UUID uuid);
    void unloadPlayer(UUID uuid);

    List<InterestAccount> getInterestAccounts(UUID playerUuid);
    void saveInterestAccount(InterestAccount account);
    void deleteInterestAccount(UUID playerUuid, int accountId);

    void logTransaction(TransactionData transaction);
    List<TransactionData> getTransactions(UUID playerUuid, int limit);
}