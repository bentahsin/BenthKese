/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.listener;

import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.eventbridge.Subscribe;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @Subscribe
    public void onMenuClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof Menu) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) {
                return;
            }
            ((Menu) e.getInventory().getHolder()).handleMenu(e);
        }
    }
}