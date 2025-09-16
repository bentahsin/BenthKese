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
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.AnvilGUIHelper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Objects;

public class KeseGonderOyuncuGUI {

    private final BenthKese plugin;
    private final PlayerMenuUtility playerMenuUtility;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final ConfigurationManager configManager;

    public KeseGonderOyuncuGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MenuManager menuManager, MessageManager messageManager, IStorageService storageService, LimitManager limitManager, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.playerMenuUtility = playerMenuUtility;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.configManager = configManager;
    }

    public void open() {
        Section section = menuManager.getMenuSection("send-menu.player-input");
        if (section == null) {
            playerMenuUtility.getOwner().sendMessage(ChatColor.RED + "Hata: send-menu.player-input yapılandırması bulunamadı!");
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(section.getString("title", "&8Oyuncu Seç")));
        Material displayMaterial = Material.matchMaterial(Objects.requireNonNull(section.getString("display-item", "NAME_TAG")));
        if (displayMaterial == null) displayMaterial = Material.NAME_TAG;

        AnvilGUIHelper.createPlayerInput(
                plugin,
                playerMenuUtility.getOwner(),
                messageManager,
                title,
                new ItemStack(displayMaterial),
                (targetPlayer) -> {
                    playerMenuUtility.setTargetPlayerUUID(targetPlayer.getUniqueId());
                    return Collections.singletonList(AnvilGUI.ResponseAction.run(() ->
                            new KeseGonderMiktarGUI(playerMenuUtility, plugin, menuManager, messageManager, storageService, limitManager, configManager).open()
                    ));
                }
        );
    }
}