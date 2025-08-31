/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.data;

import java.util.UUID;

/**
 * Tek bir finansal işlemi temsil eden veri nesnesi.
 */
public class TransactionData {

    private final UUID playerUuid;
    private final TransactionType type;
    private final double amount;
    private final String description; // İlgili oyuncu adı, "Limit Yükseltme" vb.
    private final long timestamp;

    public TransactionData(UUID playerUuid, TransactionType type, double amount, String description, long timestamp) {
        this.playerUuid = playerUuid;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getter metotları
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }
}