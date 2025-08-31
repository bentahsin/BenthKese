/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.listeners;

import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final IStorageService storageService;

    public PlayerConnectionListener(IStorageService storageService) {
        this.storageService = storageService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        storageService.loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        storageService.unloadPlayer(event.getPlayer().getUniqueId());
    }
}