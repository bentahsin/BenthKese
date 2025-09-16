/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.utils;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.configuration.MessageManager;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * AnvilGUI oluşturma işlemini merkezileştiren ve kod tekrarını önleyen yardımcı sınıf.
 */
public final class AnvilGUIHelper {

    /**
     * Oyuncudan sayısal bir miktar girmesini isteyen standart bir AnvilGUI oluşturur ve açar.
     *
     * @param plugin         Ana eklenti örneği.
     * @param player         GUI'nin açılacağı oyuncu.
     * @param messageManager Mesaj yöneticisi.
     * @param title          AnvilGUI başlığı.
     * @param displayItem    Solda gösterilecek item.
     * @param onComplete     Girdi geçerli olduğunda çalıştırılacak fonksiyon. Girdiyi alır ve bir ResponseAction listesi döndürür.
     */
    public static void createAmountInput(BenthKese plugin, Player player, MessageManager messageManager, String title, ItemStack displayItem, Function<Double, List<AnvilGUI.ResponseAction>> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(title)
                .itemLeft(displayItem)
                .text(messageManager.getMessage("gui.anvil.default-amount-text"))
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }
                    try {
                        double amount = Double.parseDouble(stateSnapshot.getText());
                        if (amount <= 0) {
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("invalid-amount")));
                        }
                        return onComplete.apply(amount);
                    } catch (NumberFormatException e) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("gui.anvil.not-a-number")));
                    }
                })
                .open(player);
    }

    /**
     * Oyuncudan başka bir oyuncunun adını girmesini isteyen standart bir AnvilGUI oluşturur ve açar.
     *
     * @param plugin         Ana eklenti örneği.
     * @param player         GUI'nin açılacağı oyuncu.
     * @param messageManager Mesaj yöneticisi.
     * @param title          AnvilGUI başlığı.
     * @param displayItem    Solda gösterilecek item.
     * @param onComplete     Hedef oyuncu bulunduğunda çalıştırılacak fonksiyon. Hedef oyuncuyu alır ve bir ResponseAction listesi döndürür.
     */
    public static void createPlayerInput(BenthKese plugin, Player player, MessageManager messageManager, String title, ItemStack displayItem, Function<Player, List<AnvilGUI.ResponseAction>> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(title)
                .itemLeft(displayItem)
                .text(messageManager.getMessage("gui.anvil.default-player-text"))
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }
                    Player target = Bukkit.getPlayer(stateSnapshot.getText());
                    if (target == null || !target.isOnline()) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("gui.anvil.player-not-found")));
                    }
                    if (target.equals(player)) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("gui.anvil.cant-send-self")));
                    }
                    return onComplete.apply(target);
                })
                .open(player);
    }
}