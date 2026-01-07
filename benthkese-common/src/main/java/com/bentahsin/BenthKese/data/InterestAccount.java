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
 * Tek bir vadeli faiz hesabının verilerini tutan DTO (Data Transfer Object).
 * Bu sınıfın birincil amacı, depolama katmanı ve servis katmanı arasında
 * yapılandırılmış veri taşımaktır.
 */
public class InterestAccount {

    private int id;
    private int accountId;
    private UUID playerUuid;
    private double principal;
    private double interestRate;
    private long startTime;
    private long endTime;

    public InterestAccount() {}

    public InterestAccount(int id, int accountId, UUID playerUuid, double principal, double interestRate, long startTime, long endTime) {
        this.id = id;
        this.accountId = accountId;
        this.playerUuid = playerUuid;
        this.principal = principal;
        this.interestRate = interestRate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public double getPrincipal() {
        return principal;
    }

    public void setPrincipal(double principal) {
        this.principal = principal;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Vade sonunda elde edilecek toplam kazancı (anapara + faiz) hesaplar.
     * @return Toplam kazanç.
     */
    public double getFinalAmount() {
        return principal * (1 + interestRate);
    }

    /**
     * Hesabın vadesinin dolup dolmadığını kontrol eder.
     * @return Vade dolmamışsa (aktifse) true, dolmuşsa false döner.
     */
    public boolean isActive() {
        return System.currentTimeMillis() < this.endTime;
    }
}