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
import com.bentahsin.BenthKese.utils.AnvilGUIHelper;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
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
        AnvilGUIHelper.createPlayerInput(
                plugin,
                playerMenuUtility.getOwner(),
                messageManager,
                messageManager.getMessage("gui.send-menu.player-title"),
                new ItemStack(Material.NAME_TAG),
                (targetPlayer) -> {
                    // Oyuncu başarıyla bulundu, şimdi miktar GUI'sini aç
                    playerMenuUtility.setTargetPlayerUUID(targetPlayer.getUniqueId());
                    return Collections.singletonList(AnvilGUI.ResponseAction.run(() ->
                            new KeseGonderMiktarGUI(plugin, playerMenuUtility, messageManager, storageService, limitManager, configManager).open()
                    ));
                }
        );
    }
}