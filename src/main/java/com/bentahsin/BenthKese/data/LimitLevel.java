/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.data;

// Bir limit seviyesinin özelliklerini tutan sınıf.
public class LimitLevel {

    private final int level;
    private final String name;
    private final double cost;
    private final double sendLimit;
    private final double receiveLimit;

    public LimitLevel(int level, String name, double cost, double sendLimit, double receiveLimit) {
        this.level = level;
        this.name = name;
        this.cost = cost;
        this.sendLimit = sendLimit;
        this.receiveLimit = receiveLimit;
    }

    // Getter'lar
    public int getLevel() { return level; }
    public String getName() { return name; }
    public double getCost() { return cost; }
    public double getSendLimit() { return sendLimit; }
    public double getReceiveLimit() { return receiveLimit; }
}