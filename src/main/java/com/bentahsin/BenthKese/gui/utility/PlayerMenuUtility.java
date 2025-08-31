/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.utility;

import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerMenuUtility {
    private final Player owner;
    private UUID targetPlayerUUID;
    private double temporaryAmount;

    public PlayerMenuUtility(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public UUID getTargetPlayerUUID() {
        return targetPlayerUUID;
    }

    public void setTargetPlayerUUID(UUID targetPlayerUUID) {
        this.targetPlayerUUID = targetPlayerUUID;
    }

    public double getTemporaryAmount() {
        return temporaryAmount;
    }

    public void setTemporaryAmount(double temporaryAmount) {
        this.temporaryAmount = temporaryAmount;
    }
}