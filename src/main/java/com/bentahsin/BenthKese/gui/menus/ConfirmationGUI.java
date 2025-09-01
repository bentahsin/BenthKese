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
import org.bukkit.configuration.ConfigurationSection;
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
        // Buton konfigürasyonlarını menus.yml'den al
        MenuItemConfig confirmConfig = menuManager.getMenuItem(MENU_KEY, "confirm-button");
        MenuItemConfig cancelConfig = menuManager.getMenuItem(MENU_KEY, "cancel-button");

        // Bilgi item'ının slotunu da menus.yml'den alalım.
        ConfigurationSection section = menuManager.getMenuSection(MENU_KEY + ".items");
        int infoSlot = section != null ? section.getInt("info-item-slot", 13) : 13;

        // Eylemleri ata
        actions.put(confirmConfig.getSlot(), onConfirm);
        actions.put(cancelConfig.getSlot(), onCancel);

        // Item'ları envantere yerleştir
        inventory.setItem(infoSlot, infoItem); // Dinamik olarak oluşturulan bilgi item'ı
        inventory.setItem(confirmConfig.getSlot(), createItemFromConfig(confirmConfig, Collections.emptyMap()));
        inventory.setItem(cancelConfig.getSlot(), createItemFromConfig(cancelConfig, Collections.emptyMap()));

        // Boşlukları doldur
        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }
}