/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MenuItemConfig;
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.TransactionData;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.PaginatedMenu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionHistoryGUI extends PaginatedMenu<TransactionData> {

    private static final String MENU_KEY = "history-menu";

    // Bağımlılıklar
    private final BenthKese plugin;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final LimitManager limitManager;
    private final InterestService interestService;

    // Formatlayıcılar
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public TransactionHistoryGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MenuManager menuManager, MessageManager messageManager, IStorageService storageService, EconomyService economyService, ConfigurationManager configManager, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.economyService = economyService;
        this.configManager = configManager;
        this.limitManager = limitManager;
        this.interestService = interestService;
    }

    public String getMenuName() {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sayfa", String.valueOf(page + 1));
        placeholders.put("max_sayfa", String.valueOf(Math.max(1, getMaxPages())));

        return menuManager.getMenuTitle(MENU_KEY, placeholders);
    }

    @Override
    public int getSlots() {
        return menuManager.getMenuSize(MENU_KEY);
    }

    @Override
    public List<TransactionData> getAllItems() {
        return storageService.getTransactions(playerMenuUtility.getOwner().getUniqueId(), 100);
    }

    @Override
    public ItemStack getItemStack(TransactionData transaction) {
        // İşlem türüne göre uygun item şablonunu menus.yml'den al
        String itemKey = transaction.getType().name();
        MenuItemConfig itemConfig = menuManager.getMenuItem(MENU_KEY, "item-templates." + itemKey);

        // Eğer belirli bir tür için tanım yoksa, bilinmeyen (UNKNOWN) şablonunu kullan
        if (itemConfig.getMaterial() == org.bukkit.Material.BARRIER) { // getMenuItem hata durumunda BARRIER döner
            itemConfig = menuManager.getMenuItem(MENU_KEY, "item-templates.UNKNOWN");
        }

        // Placeholder'ları doldur
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{miktar}", numberFormat.format(transaction.getAmount()));
        placeholders.put("{tarih}", dateFormat.format(new Date(transaction.getTimestamp())));
        placeholders.put("{aciklama}", transaction.getDescription());

        switch (transaction.getType()) {
            case SEND:
                placeholders.put("{hedef}", transaction.getDescription());
                break;
            case RECEIVE:
                placeholders.put("{gonderen}", transaction.getDescription());
                break;
            case DEPOSIT:
            case WITHDRAW:
                placeholders.put("{birim}", transaction.getDescription());
                break;
            case LEVEL_UP:
                placeholders.put("{maliyet}", numberFormat.format(transaction.getAmount()));
                placeholders.put("{seviye}", transaction.getDescription());
                break;
            case INTEREST_CLAIM:
            case INTEREST_BREAK:
                placeholders.put("{hesap_id}", transaction.getDescription());
                break;
        }

        return createItemFromConfig(itemConfig, placeholders);
    }

    @Override
    public void onItemClick(TransactionData item) {
        // Tıklayınca bir şey yapmasına gerek yok
    }

    @Override
    public Menu getNavigationBackMenu() {
        return new KeseMainMenuGUI(playerMenuUtility, plugin, menuManager, messageManager, economyService, configManager, storageService, limitManager, interestService);
    }

    // --- PaginatedMenu için Gerekli Yapılandırmaları Sağlayan Metotlar ---

    @Override
    protected MenuItemConfig getBackButtonConfig() {
        return menuManager.getMenuItem(MENU_KEY, "back-button");
    }

    @Override
    protected MenuItemConfig getPreviousPageButtonConfig() {
        return menuManager.getMenuItem(MENU_KEY, "previous-page");
    }

    @Override
    protected MenuItemConfig getNextPageButtonConfig() {
        return menuManager.getMenuItem(MENU_KEY, "next-page");
    }

    @Override
    protected MenuItemConfig getFillerItemConfig() {
        return menuManager.getMenuItem(MENU_KEY, "filler-item");
    }
}