/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.commands.impl.KeseAlCommand;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.AnvilGUIHelper;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class KeseCekGUI extends Menu {

    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final KeseAlCommand alCommandLogic;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final InterestService interestService;

    public KeseCekGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MessageManager messageManager, EconomyService economyService, ConfigurationManager configManager, IStorageService storageService, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.economyService = economyService;
        this.configManager = configManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.interestService = interestService;
        this.alCommandLogic = new KeseAlCommand(messageManager, economyService, configManager, storageService);
    }

    @Override
    public String getMenuName() {
        return messageManager.getMessage("gui.withdraw-menu.title");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        // Miktar girmek için AnvilGUI butonu
        actions.put(11, this::openWithdrawAnvil);
        inventory.setItem(11, createGuiItem(Material.ANVIL,
                messageManager.getMessage("gui.withdraw-menu.anvil.name"),
                messageManager.getMessageList("gui.withdraw-menu.anvil.lore").toArray(new String[0]))
        );

        // Hızlı çekme butonları
        addWithdrawButton(13, 16);
        addWithdrawButton(14, 32);
        addWithdrawButton(15, 64);

        // Geri butonu
        actions.put(26, () -> new KeseMainMenuGUI(playerMenuUtility, plugin, messageManager, economyService, configManager, storageService, limitManager, interestService).open());
        inventory.setItem(26, createGuiItem(Material.BARRIER, messageManager.getMessage("gui.general.back-button")));

        fillEmptySlots();
    }

    private void addWithdrawButton(int slot, int amount) {
        actions.put(slot, () -> {
            alCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{String.valueOf(amount)});
            playerMenuUtility.getOwner().closeInventory();
        });

        inventory.setItem(slot, createGuiItem(Material.GOLD_INGOT,
                messageManager.getMessage("gui.withdraw-menu.quick-withdraw.name").replace("{miktar}", String.valueOf(amount)),
                messageManager.getMessageList("gui.withdraw-menu.quick-withdraw.lore")
                        .stream()
                        .map(line -> line.replace("{miktar}", String.valueOf(amount))).toArray(String[]::new))
        );
    }

    private void openWithdrawAnvil() {
        AnvilGUIHelper.createAmountInput(
                plugin,
                playerMenuUtility.getOwner(),
                messageManager,
                messageManager.getMessage("gui.withdraw-menu.anvil-title"),
                new ItemStack(Material.PAPER),
                (amount) -> {
                    alCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{String.valueOf(amount.intValue())});
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                }
        );
    }
}