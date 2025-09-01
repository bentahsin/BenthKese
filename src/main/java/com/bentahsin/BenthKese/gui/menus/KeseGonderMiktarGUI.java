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
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.AnvilGUIHelper;
import com.bentahsin.BenthKese.utils.TextUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KeseGonderMiktarGUI {

    private final BenthKese plugin;
    private final PlayerMenuUtility playerMenuUtility;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final KeseGonderCommand gonderCommandLogic;

    public KeseGonderMiktarGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MenuManager menuManager, MessageManager messageManager, IStorageService storageService, LimitManager limitManager, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.playerMenuUtility = playerMenuUtility;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.gonderCommandLogic = new KeseGonderCommand(messageManager, storageService, limitManager, configManager);
    }

    public void open() {
        Player player = playerMenuUtility.getOwner();
        Player target = Bukkit.getPlayer(playerMenuUtility.getTargetPlayerUUID());
        if (target == null) return;

        ConfigurationSection section = menuManager.getMenuSection("send-menu.amount-input");
        if (section == null) {
            player.sendMessage(ChatColor.RED + "Hata: send-menu.amount-input yapılandırması bulunamadı!");
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{oyuncu}", target.getName());

        String title = TextUtil.replacePlaceholders(section.getString("title", "&8Miktar Gir"), placeholders);
        Material displayMaterial = Material.matchMaterial(Objects.requireNonNull(section.getString("display-item", "GOLD_INGOT")));
        if (displayMaterial == null) displayMaterial = Material.GOLD_INGOT;

        AnvilGUIHelper.createAmountInput(
                plugin,
                player,
                messageManager,
                title,
                new ItemStack(displayMaterial),
                (amount) -> {
                    String[] args = {target.getName(), String.valueOf(amount)};
                    gonderCommandLogic.execute(player, args);
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                }
        );
    }
}