/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.commands.impl.KeseKoyCommand;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MenuItemConfig;
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.AnvilGUIHelper;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KeseYatirGUI extends Menu {

    private static final String MENU_KEY = "deposit-menu";

    private final BenthKese plugin;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final InterestService interestService;
    private final KeseKoyCommand koyCommandLogic;

    public KeseYatirGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MenuManager menuManager, MessageManager messageManager, EconomyService economyService, ConfigurationManager configManager, IStorageService storageService, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.configManager = configManager;
        this.storageService = storageService;
        this.economyService = economyService;
        this.limitManager = limitManager;
        this.interestService = interestService;
        this.koyCommandLogic = new KeseKoyCommand(messageManager, economyService, configManager, storageService);
    }

    @Override
    public String getMenuName() {
        return menuManager.getMenuTitle(MENU_KEY);
    }

    @Override
    public int getSlots() {
        return menuManager.getMenuSize(MENU_KEY);
    }

    @Override
    public void setMenuItems() {
        MenuItemConfig anvilConfig = menuManager.getMenuItem(MENU_KEY, "anvil-input");
        MenuItemConfig handConfig = menuManager.getMenuItem(MENU_KEY, "deposit-hand");
        MenuItemConfig inventoryConfig = menuManager.getMenuItem(MENU_KEY, "deposit-inventory");
        MenuItemConfig backConfig = menuManager.getMenuItem(MENU_KEY, "back-button");

        actions.put(anvilConfig.getSlot(), this::openDepositAnvil);
        actions.put(handConfig.getSlot(), () -> {
            koyCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{"el"});
            playerMenuUtility.getOwner().closeInventory();
        });
        actions.put(inventoryConfig.getSlot(), () -> {
            koyCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{"envanter"});
            playerMenuUtility.getOwner().closeInventory();
        });
        actions.put(backConfig.getSlot(), () -> new KeseMainMenuGUI(playerMenuUtility, plugin, menuManager, messageManager, economyService, configManager, storageService, limitManager, interestService).open());

        inventory.setItem(anvilConfig.getSlot(), createItemFromConfig(anvilConfig, Collections.emptyMap()));
        inventory.setItem(handConfig.getSlot(), createItemFromConfig(handConfig, Collections.emptyMap()));
        inventory.setItem(inventoryConfig.getSlot(), createItemFromConfig(inventoryConfig, Collections.emptyMap()));
        inventory.setItem(backConfig.getSlot(), createItemFromConfig(backConfig, Collections.emptyMap()));

        addDepositButton(21, 16);
        addDepositButton(22, 32);
        addDepositButton(23, 64);

        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }

    private void addDepositButton(int slot, int amount) {
        actions.put(slot, () -> {
            koyCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{String.valueOf(amount)});
            playerMenuUtility.getOwner().closeInventory();
        });

        MenuItemConfig quickDepositConfig = menuManager.getMenuItem(MENU_KEY, "quick-deposit");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{miktar}", String.valueOf(amount));

        inventory.setItem(slot, createItemFromConfig(quickDepositConfig, placeholders));
    }

    private void openDepositAnvil() {
        AnvilGUIHelper.createAmountInput(
                plugin,
                playerMenuUtility.getOwner(),
                messageManager,
                messageManager.getMessage("gui.deposit-menu.anvil-title"),
                new ItemStack(menuManager.getMenuItem(MENU_KEY, "anvil-input").getMaterial()),
                (amount) -> {
                    koyCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{String.valueOf(amount.intValue())});
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                }
        );
    }
}