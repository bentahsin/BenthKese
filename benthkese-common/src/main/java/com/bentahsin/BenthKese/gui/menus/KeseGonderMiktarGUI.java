/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.commands.impl.KeseGonderCommand;
import com.bentahsin.BenthKese.configuration.BenthConfig;
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.AnvilGUIHelper;
import com.bentahsin.BenthKese.utils.TextUtil;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KeseGonderMiktarGUI {

    private final BenthKeseCore core;
    private final PlayerMenuUtility playerMenuUtility;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final KeseGonderCommand gonderCommandLogic;

    public KeseGonderMiktarGUI(PlayerMenuUtility playerMenuUtility, BenthKeseCore core, MenuManager menuManager, MessageManager messageManager, IStorageService storageService, LimitManager limitManager, BenthConfig config) {
        this.core = core;
        this.playerMenuUtility = playerMenuUtility;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.gonderCommandLogic = new KeseGonderCommand(core, messageManager, storageService, limitManager, config);
    }

    public void open() {
        Player player = playerMenuUtility.getOwner();
        Player target = Bukkit.getPlayer(playerMenuUtility.getTargetPlayerUUID());
        if (target == null) return;

        Section section = menuManager.getMenuSection("send-menu.amount-input");
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
                core.getPlugin(),
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