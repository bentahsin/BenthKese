/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.commands.impl.KeseAlCommand;
import com.bentahsin.BenthKese.configuration.*;
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

public class KeseCekGUI extends Menu {

    private static final String MENU_KEY = "withdraw-menu";

    private final BenthKeseCore core;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final BenthConfig config;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final InterestService interestService;
    private final KeseAlCommand alCommandLogic;

    public KeseCekGUI(PlayerMenuUtility playerMenuUtility, BenthKeseCore core, MenuManager menuManager, MessageManager messageManager, EconomyService economyService, BenthConfig config, IStorageService storageService, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.core = core;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.economyService = economyService;
        this.config = config;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.interestService = interestService;
        this.alCommandLogic = new KeseAlCommand(core, messageManager, economyService, config, storageService);
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
        MenuItemConfig backConfig = menuManager.getMenuItem(MENU_KEY, "back-button");

        actions.put(anvilConfig.slot(), this::openWithdrawAnvil);
        actions.put(backConfig.slot(), () -> new KeseMainMenuGUI(playerMenuUtility, core, menuManager, messageManager, economyService, config, storageService, limitManager, interestService).open());

        inventory.setItem(anvilConfig.slot(), createItemFromConfig(anvilConfig, Collections.emptyMap()));
        inventory.setItem(backConfig.slot(), createItemFromConfig(backConfig, Collections.emptyMap()));

        addWithdrawButton(13, 16);
        addWithdrawButton(14, 32);
        addWithdrawButton(15, 64);

        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }

    private void addWithdrawButton(int slot, int amount) {
        actions.put(slot, () -> {
            alCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{String.valueOf(amount)});
            playerMenuUtility.getOwner().closeInventory();
        });

        MenuItemConfig quickWithdrawConfig = menuManager.getMenuItem(MENU_KEY, "quick-withdraw");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{miktar}", String.valueOf(amount));

        inventory.setItem(slot, createItemFromConfig(quickWithdrawConfig, placeholders));
    }

    private void openWithdrawAnvil() {
        AnvilGUIHelper.createAmountInput(
                core.getPlugin(),
                playerMenuUtility.getOwner(),
                messageManager,
                messageManager.getMessage("gui.withdraw-menu.anvil-title"),
                new ItemStack(menuManager.getMenuItem(MENU_KEY, "anvil-input").material()),
                (amount) -> {
                    alCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{String.valueOf(amount.intValue())});
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                }
        );
    }
}