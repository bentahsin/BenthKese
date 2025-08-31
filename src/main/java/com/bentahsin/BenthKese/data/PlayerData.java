/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.data;

import java.util.UUID;

// Oyuncunun BenthKese ile ilgili tüm verilerini tutan sınıf.
public class PlayerData {

    private final UUID uuid;
    private int limitLevel;
    private double dailySent;
    private double dailyReceived;
    private long lastResetTime;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.limitLevel = 1; // Varsayılan seviye
        this.dailySent = 0;
        this.dailyReceived = 0;
        this.lastResetTime = System.currentTimeMillis();
    }

    // Getter ve Setter'lar
    public UUID getUuid() { return uuid; }
    public int getLimitLevel() { return limitLevel; }
    public void setLimitLevel(int limitLevel) { this.limitLevel = limitLevel; }
    public double getDailySent() { return dailySent; }
    public void setDailySent(double dailySent) { this.dailySent = dailySent; }
    public void addDailySent(double amount) { this.dailySent += amount; }
    public double getDailyReceived() { return dailyReceived; }
    public void setDailyReceived(double dailyReceived) { this.dailyReceived = dailyReceived; }
    public void addDailyReceived(double amount) { this.dailyReceived += amount; }
    public long getLastResetTime() { return lastResetTime; }
    public void setLastResetTime(long lastResetTime) { this.lastResetTime = lastResetTime; }

    public void resetDailyLimits() {
        this.dailySent = 0;
        this.dailyReceived = 0;
        this.lastResetTime = System.currentTimeMillis();
    }
}