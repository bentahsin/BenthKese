/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class KeseGonderOyuncuGUI {

    private final BenthKese plugin;
    private final PlayerMenuUtility playerMenuUtility;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final ConfigurationManager configManager;

    public KeseGonderOyuncuGUI(BenthKese plugin, PlayerMenuUtility playerMenuUtility, MessageManager messageManager, IStorageService storageService, LimitManager limitManager, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.playerMenuUtility = playerMenuUtility;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.configManager = configManager;
    }

    public void open() {
        Player player = playerMenuUtility.getOwner();
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> {}) // İsteğe bağlı: Kapatıldığında bir şey yap
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }
                    Player target = Bukkit.getPlayer(stateSnapshot.getText());
                    if (target == null || !target.isOnline()) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("gui.anvil.player-not-found")));
                    }
                    if (target.equals(player)) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("gui.anvil.cant-send-self")));
                    }
                    // Oyuncu bulundu, şimdi miktar girmesi için ikinci AnvilGUI'yi aç
                    playerMenuUtility.setTargetPlayerUUID(target.getUniqueId());
                    return Collections.singletonList(AnvilGUI.ResponseAction.run(() ->
                            new KeseGonderMiktarGUI(plugin, playerMenuUtility, messageManager, storageService, limitManager, configManager).open()
                    ));
                })
                .text(messageManager.getMessage("gui.anvil.default-player-text"))
                .itemLeft(new ItemStack(Material.NAME_TAG))
                .title(messageManager.getMessage("gui.send-menu.player-title"))
                .plugin(plugin)
                .open(player);
    }
}