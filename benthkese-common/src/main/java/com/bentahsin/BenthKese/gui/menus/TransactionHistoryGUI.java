/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.configuration.*;
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

    private final BenthKeseCore core;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final EconomyService economyService;
    private final BenthConfig config;
    private final LimitManager limitManager;
    private final InterestService interestService;

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public TransactionHistoryGUI(PlayerMenuUtility playerMenuUtility, BenthKeseCore core, MenuManager menuManager, MessageManager messageManager, IStorageService storageService, EconomyService economyService, BenthConfig config, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.core = core;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.economyService = economyService;
        this.config = config;
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
        String itemKey = transaction.getType().name();
        MenuItemConfig itemConfig = menuManager.getMenuItem(MENU_KEY, "item-templates." + itemKey);

        if (itemConfig.material() == org.bukkit.Material.BARRIER) {
            itemConfig = menuManager.getMenuItem(MENU_KEY, "item-templates.UNKNOWN");
        }

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
    }

    @Override
    public Menu getNavigationBackMenu() {
        return new KeseMainMenuGUI(playerMenuUtility, core, menuManager, messageManager, economyService, config, storageService, limitManager, interestService);
    }


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