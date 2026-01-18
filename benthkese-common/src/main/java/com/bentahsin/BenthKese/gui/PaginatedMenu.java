/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui;

import com.bentahsin.BenthKese.configuration.MenuItemConfig;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Herhangi bir öğe listesini sayfalara ayıran, menus.yml ile tamamen yapılandırılabilir,
 * genel amaçlı bir menü sınıfı.
 * @param <T> Menüde listelenecek öğenin türü.
 */
public abstract class PaginatedMenu<T> extends Menu {

    protected int page = 0;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    /**
     * Menüde listelenecek tüm öğelerin tam listesini döndürür.
     */
    public abstract List<T> getAllItems();

    /**
     * Tek bir öğeden, menüde gösterilecek olan ItemStack'i oluşturur.
     */
    public abstract ItemStack getItemStack(T item);

    /**
     * Menüdeki bir öğeye tıklandığında ne olacağını tanımlar.
     */
    public abstract void onItemClick(T item);

    /**
     * "Geri" butonuna basıldığında hangi menünün açılacağını belirtir.
     */
    public abstract Menu getNavigationBackMenu();


    /**
     * Geri butonunun yapılandırmasını döndürür.
     */
    protected abstract MenuItemConfig getBackButtonConfig();

    /**
     * Önceki sayfa butonunun yapılandırmasını döndürür.
     */
    protected abstract MenuItemConfig getPreviousPageButtonConfig();

    /**
     * Sonraki sayfa butonunun yapılandırmasını döndürür.
     */
    protected abstract MenuItemConfig getNextPageButtonConfig();

    /**
     * Boşlukları dolduracak olan item'ın yapılandırmasını döndürür.
     */
    protected abstract MenuItemConfig getFillerItemConfig();


    @Override
    public void setMenuItems() {
        addNavigationControls();

        List<T> allItems = getAllItems();
        if (allItems == null || allItems.isEmpty()) {
            fillEmptySlots(getFillerItemConfig());
            return;
        }

        int maxItemsPerPage = getSlots();
        int startIndex = page * maxItemsPerPage;
        int placedItems = 0;

        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) != null) {
                continue;
            }

            int itemIndex = startIndex + placedItems;
            if (itemIndex >= allItems.size()) {
                break;
            }

            T currentItem = allItems.get(itemIndex);
            ItemStack itemStack = getItemStack(currentItem);

            actions.put(i, () -> onItemClick(currentItem));
            inventory.setItem(i, itemStack);
            placedItems++;
        }

        fillEmptySlots(getFillerItemConfig());
    }

    /**
     * Navigasyon butonlarını menus.yml'den gelen yapılandırmalara göre envantere ekler.
     */
    protected void addNavigationControls() {
        int maxPages = getMaxPages();

        if (page > 0) {
            MenuItemConfig prevConfig = getPreviousPageButtonConfig();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{onceki_sayfa}", String.valueOf(page));
            placeholders.put("{sayfa}", String.valueOf(page + 1));

            inventory.setItem(prevConfig.slot(), createItemFromConfig(prevConfig, placeholders));
            actions.put(prevConfig.slot(), () -> {
                page--;
                open();
            });
        }

        if (page + 1 < maxPages) {
            MenuItemConfig nextConfig = getNextPageButtonConfig();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{sonraki_sayfa}", String.valueOf(page + 2));
            placeholders.put("{sayfa}", String.valueOf(page + 1));

            inventory.setItem(nextConfig.slot(), createItemFromConfig(nextConfig, placeholders));
            actions.put(nextConfig.slot(), () -> {
                page++;
                open();
            });
        }

        MenuItemConfig backConfig = getBackButtonConfig();
        if (backConfig != null) {
            inventory.setItem(backConfig.slot(), createItemFromConfig(backConfig, Collections.emptyMap()));
            actions.put(backConfig.slot(), () -> getNavigationBackMenu().open());
        }
    }

    /**
     * Toplam sayfa sayısını hesaplar.
     */
    protected int getMaxPages() {
        List<T> allItems = getAllItems();
        if (allItems == null || allItems.isEmpty()) {
            return 1;
        }

        int availableSlots = getSlots();
        if (getBackButtonConfig() != null) availableSlots--;
        if (getPreviousPageButtonConfig() != null) availableSlots--;
        if (getNextPageButtonConfig() != null) availableSlots--;

        availableSlots = Math.max(1, availableSlots);

        return (int) Math.ceil((double) allItems.size() / availableSlots);
    }
}