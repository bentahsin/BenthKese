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
    // Sayfa başına öğe sayısı ve navigasyon çubuğu gibi sabitler kaldırıldı,
    // çünkü artık her şey esnek bir şekilde ayarlanabilir.

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    // --- Soyut Metotlar (Alt Sınıflar Tarafından Uygulanmalı) ---

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

    // --- Yapılandırma Metotları (Alt Sınıflar menus.yml'den Veri Sağlar) ---

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
        // Navigasyon butonlarını ekle
        addNavigationControls();

        List<T> allItems = getAllItems();
        if (allItems == null || allItems.isEmpty()) {
            // Hiç öğe yoksa bile, boşlukları doldurarak menüyü temiz göster
            fillEmptySlots(getFillerItemConfig());
            return;
        }

        // Bu sayfada gösterilecek item'ları hesapla ve yerleştir
        // Slot 0'dan başla, envanterin sonuna kadar git.
        // Navigasyon butonlarının slotları zaten dolu olacağı için üzerine yazılmaz.
        int maxItemsPerPage = getSlots(); // Teorik maksimum
        int startIndex = page * maxItemsPerPage;
        int placedItems = 0;

        for (int i = 0; i < getSlots(); i++) {
            // Eğer mevcut slotta zaten bir item varsa (navigasyon butonu gibi), atla.
            if (inventory.getItem(i) != null) {
                continue;
            }

            int itemIndex = startIndex + placedItems;
            if (itemIndex >= allItems.size()) {
                break; // Listenin sonuna ulaşıldı.
            }

            T currentItem = allItems.get(itemIndex);
            ItemStack itemStack = getItemStack(currentItem);

            // Tıklama eylemini tanımla
            actions.put(i, () -> onItemClick(currentItem));
            inventory.setItem(i, itemStack);
            placedItems++;
        }

        // Kalan tüm boşlukları doldur
        fillEmptySlots(getFillerItemConfig());
    }

    /**
     * Navigasyon butonlarını menus.yml'den gelen yapılandırmalara göre envantere ekler.
     */
    protected void addNavigationControls() {
        int maxPages = getMaxPages();

        // Önceki Sayfa Butonu
        if (page > 0) {
            MenuItemConfig prevConfig = getPreviousPageButtonConfig();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{onceki_sayfa}", String.valueOf(page)); // Sayfa numarası 0'dan başlar, gösterim 1'den
            placeholders.put("{sayfa}", String.valueOf(page + 1));

            inventory.setItem(prevConfig.getSlot(), createItemFromConfig(prevConfig, placeholders));
            actions.put(prevConfig.getSlot(), () -> {
                page--;
                open();
            });
        }

        // Sonraki Sayfa Butonu
        if (page + 1 < maxPages) {
            MenuItemConfig nextConfig = getNextPageButtonConfig();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{sonraki_sayfa}", String.valueOf(page + 2));
            placeholders.put("{sayfa}", String.valueOf(page + 1));

            inventory.setItem(nextConfig.getSlot(), createItemFromConfig(nextConfig, placeholders));
            actions.put(nextConfig.getSlot(), () -> {
                page++;
                open();
            });
        }

        // Geri Butonu (her zaman görünür)
        MenuItemConfig backConfig = getBackButtonConfig();
        if (backConfig != null) {
            inventory.setItem(backConfig.getSlot(), createItemFromConfig(backConfig, Collections.emptyMap()));
            actions.put(backConfig.getSlot(), () -> getNavigationBackMenu().open());
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

        // Sayfa başına düşen item sayısını, navigasyon butonları hariç boş slot sayısına göre hesapla
        int availableSlots = getSlots();
        if (getBackButtonConfig() != null) availableSlots--;
        if (getPreviousPageButtonConfig() != null) availableSlots--;
        if (getNextPageButtonConfig() != null) availableSlots--;

        // En az 1 slot olduğundan emin ol
        availableSlots = Math.max(1, availableSlots);

        return (int) Math.ceil((double) allItems.size() / availableSlots);
    }
}