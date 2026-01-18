/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.configuration.MenuItemConfig;
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

/**
 * Kritik işlemler için menus.yml'den beslenen, genel amaçlı, yeniden kullanılabilir bir onay menüsü.
 */
public class ConfirmationGUI extends Menu {

    private static final String MENU_KEY = "confirmation-menu";

    private final MenuManager menuManager;
    private final ItemStack infoItem;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmationGUI(PlayerMenuUtility playerMenuUtility, MenuManager menuManager, ItemStack infoItem, Runnable onConfirm, Runnable onCancel) {
        super(playerMenuUtility);
        this.menuManager = menuManager;
        this.infoItem = infoItem;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
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
        MenuItemConfig confirmConfig = menuManager.getMenuItem(MENU_KEY, "confirm-button");
        MenuItemConfig cancelConfig = menuManager.getMenuItem(MENU_KEY, "cancel-button");

        Section section = menuManager.getMenuSection(MENU_KEY + ".items");
        int infoSlot = section != null ? section.getInt("info-item-slot", 13) : 13;

        actions.put(confirmConfig.slot(), onConfirm);
        actions.put(cancelConfig.slot(), onCancel);

        inventory.setItem(infoSlot, infoItem);
        inventory.setItem(confirmConfig.slot(), createItemFromConfig(confirmConfig, Collections.emptyMap()));
        inventory.setItem(cancelConfig.slot(), createItemFromConfig(cancelConfig, Collections.emptyMap()));

        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }
}