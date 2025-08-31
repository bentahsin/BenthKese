/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.commands.impl.KeseGonderCommand;
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

public class KeseGonderMiktarGUI {

    private final BenthKese plugin;
    private final PlayerMenuUtility playerMenuUtility;
    private final MessageManager messageManager;
    private final KeseGonderCommand gonderCommandLogic;

    public KeseGonderMiktarGUI(BenthKese plugin, PlayerMenuUtility playerMenuUtility, MessageManager messageManager, IStorageService storageService, LimitManager limitManager, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.playerMenuUtility = playerMenuUtility;
        this.messageManager = messageManager;
        this.gonderCommandLogic = new KeseGonderCommand(messageManager, storageService, limitManager, configManager);
    }

    public void open() {
        Player player = playerMenuUtility.getOwner();
        Player target = Bukkit.getPlayer(playerMenuUtility.getTargetPlayerUUID());
        if (target == null) return; // Hedef oyuncu bir şekilde çevrimdışı olduysa

        new AnvilGUI.Builder()
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }
                    String text = stateSnapshot.getText();
                    try {
                        double amount = Double.parseDouble(text);
                        if (amount <= 0) {
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("gui.anvil.invalid-amount")));
                        }
                        // Komut mantığını burada çağır
                        String[] args = {target.getName(), String.valueOf(amount)};
                        gonderCommandLogic.execute(player, args);

                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    } catch (NumberFormatException e) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("gui.anvil.not-a-number")));
                    }
                })
                .text(messageManager.getMessage("gui.anvil.default-amount-text"))
                .itemLeft(new ItemStack(Material.GOLD_INGOT))
                .title(messageManager.getMessage("gui.send-menu.amount-title").replace("{oyuncu}", target.getName()))
                .plugin(plugin)
                .open(player);
    }
}