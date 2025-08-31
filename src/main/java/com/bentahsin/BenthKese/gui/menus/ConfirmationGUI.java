/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Kritik işlemler için genel amaçlı, yeniden kullanılabilir bir onay menüsü.
 */
public class ConfirmationGUI extends Menu {

    private final String title;
    private final ItemStack infoItem;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private final MessageManager messageManager;

    public ConfirmationGUI(PlayerMenuUtility playerMenuUtility, String title, ItemStack infoItem, Runnable onConfirm, Runnable onCancel, MessageManager messageManager) {
        super(playerMenuUtility);
        this.title = title;
        this.infoItem = infoItem;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.messageManager = messageManager;
    }

    @Override
    public String getMenuName() {
        return title;
    }

    @Override
    public int getSlots() {
        return 27; // 3 sıra basit bir menü
    }

    @Override
    public void setMenuItems() {
        // Ortadaki bilgi item'ı
        inventory.setItem(13, infoItem);

        // Onaylama Butonu
        actions.put(11, onConfirm);
        inventory.setItem(11, createGuiItem(Material.LIME_STAINED_GLASS_PANE,
                messageManager.getMessage("gui.confirmation.confirm-button.name"),
                messageManager.getMessageList("gui.confirmation.confirm-button.lore").toArray(new String[0]))
        );

        // İptal Etme Butonu
        actions.put(15, onCancel);
        inventory.setItem(15, createGuiItem(Material.RED_STAINED_GLASS_PANE,
                messageManager.getMessage("gui.confirmation.cancel-button.name"),
                messageManager.getMessageList("gui.confirmation.cancel-button.lore").toArray(new String[0]))
        );

        // Kalan boşlukları doldur
        fillEmptySlots();
    }
}