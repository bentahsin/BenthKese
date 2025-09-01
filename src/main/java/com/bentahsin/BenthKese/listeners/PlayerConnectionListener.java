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
        storageService.updatePlayerName(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        storageService.unloadPlayer(event.getPlayer().getUniqueId());
    }
}