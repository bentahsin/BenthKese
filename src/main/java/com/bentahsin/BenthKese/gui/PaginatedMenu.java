/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui;

import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Herhangi bir öğe listesini sayfalara ayıran genel amaçlı, yeniden kullanılabilir bir menü sınıfı.
 * @param <T> Menüde listelenecek öğenin türü (örn: InterestAccount).
 */
public abstract class PaginatedMenu<T> extends Menu {

    protected int page = 0;
    protected int maxItemsPerPage;
    protected int navigationBarSlotStart;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    /**
     * Menüde listelenecek tüm öğelerin tam listesini döndürür.
     */
    public abstract List<T> getAllItems();

    /**
     * Tek bir öğeden, menüde gösterilecek olan ItemStack'i oluşturur.
     * @param item İşlenecek öğe.
     * @return Gösterilecek ItemStack.
     */
    public abstract ItemStack getItemStack(T item);

    /**
     * Menüdeki bir öğeye tıklandığında ne olacağını tanımlar.
     * @param item Tıklanan öğe.
     */
    public abstract void onItemClick(T item);

    /**
     * "Geri" butonuna basıldığında hangi menünün açılacağını belirtir.
     * @return Gidilecek olan menü nesnesi.
     */
    public abstract Menu getNavigationBackMenu();

    @Override
    public void setMenuItems() {
        // Navigasyon çubuğunu ve sayfa başına öğe sayısını ayarla
        this.maxItemsPerPage = getSlots() - 9;
        this.navigationBarSlotStart = getSlots() - 9;

        addNavigationControls();

        List<T> allItems = getAllItems();
        if (allItems == null || allItems.isEmpty()) {
            return; // Gösterilecek öğe yoksa bitir.
        }

        int startIndex = page * maxItemsPerPage;
        for (int i = 0; i < maxItemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex >= allItems.size()) {
                break; // Listenin sonuna ulaşıldı.
            }

            T currentItem = allItems.get(itemIndex);
            ItemStack itemStack = getItemStack(currentItem);

            // Tıklama eylemini tanımla
            actions.put(i, () -> onItemClick(currentItem)); // Tıklama olayını onItemClick'e yönlendir
            inventory.setItem(i, itemStack);
        }
    }

    protected void addNavigationControls() {
        List<T> allItems = getAllItems();
        int maxPages = (int) Math.ceil((double) allItems.size() / maxItemsPerPage);

        // Önceki Sayfa Butonu
        if (page > 0) {
            actions.put(navigationBarSlotStart, () -> {
                page--;
                open();
            });
            inventory.setItem(navigationBarSlotStart, createGuiItem(Material.ARROW, "&a« Önceki Sayfa", "&7Sayfa " + (page) + "'e git."));
        } else {
            inventory.setItem(navigationBarSlotStart, FILLER_GLASS);
        }

        // Sayfa Bilgisi & Geri Butonu
        actions.put(navigationBarSlotStart + 4, () -> getNavigationBackMenu().open());
        inventory.setItem(navigationBarSlotStart + 4, createGuiItem(Material.BARRIER, "&cGeri Dön", "&7Ana menüye dönmek için tıkla."));

        // Sonraki Sayfa Butonu
        if (page + 1 < maxPages) {
            actions.put(navigationBarSlotStart + 8, () -> {
                page++;
                open();
            });
            inventory.setItem(navigationBarSlotStart + 8, createGuiItem(Material.ARROW, "&aSonraki Sayfa »", "&7Sayfa " + (page + 2) + "'e git."));
        } else {
            inventory.setItem(navigationBarSlotStart + 8, FILLER_GLASS);
        }
    }
}