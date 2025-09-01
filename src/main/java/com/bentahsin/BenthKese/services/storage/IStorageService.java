package com.bentahsin.BenthKese.services.storage;

import com.bentahsin.BenthKese.data.InterestAccount;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.data.TopPlayerEntry;
import com.bentahsin.BenthKese.data.TransactionData;
import java.util.List;
import java.util.UUID;

public interface IStorageService {
    // PlayerData
    PlayerData getPlayerData(UUID uuid);
    void savePlayerData(PlayerData playerData);
    void loadPlayer(UUID uuid);
    void unloadPlayer(UUID uuid);

    // Interest
    List<InterestAccount> getInterestAccounts(UUID playerUuid);
    void saveInterestAccount(InterestAccount account);
    void deleteInterestAccount(UUID playerUuid, int accountId);

    // Transactions
    void logTransaction(TransactionData transaction);
    List<TransactionData> getTransactions(UUID playerUuid, int limit);

    // Top Lists & Ranks
    List<TopPlayerEntry> getTopPlayersByBalance(int limit);
    List<TopPlayerEntry> getTopPlayersByLimitLevel(int limit);
    int getPlayerBalanceRank(UUID uuid);

    // Utility
    void updatePlayerName(UUID uuid, String name);
    void updatePlayerBalance(UUID uuid, double balance);
}