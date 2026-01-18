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
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class KeseMainMenuGUI extends Menu {

    private static final String MENU_KEY = "main-menu";

    private final BenthKeseCore core;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final BenthConfig config;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final InterestService interestService;
    private final Economy economy = BenthKeseCore.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseMainMenuGUI(PlayerMenuUtility playerMenuUtility, BenthKeseCore core, MenuManager menuManager, MessageManager messageManager, EconomyService economyService, BenthConfig config, IStorageService storageService, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.core = core;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.economyService = economyService;
        this.config = config;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.interestService = interestService;
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
        Player p = playerMenuUtility.getOwner();

        Runnable openDepositGUI = () -> new KeseYatirGUI(playerMenuUtility, core, menuManager, messageManager, economyService, config, storageService, limitManager, interestService).open();
        Runnable openWithdrawGUI = () -> new KeseCekGUI(playerMenuUtility, core, menuManager, messageManager, economyService, config, storageService, limitManager, interestService).open();
        Runnable openSendGUI = () -> new KeseGonderOyuncuGUI(playerMenuUtility, core, menuManager, messageManager, storageService, limitManager, config).open();
        Runnable openLimitGUI = () -> new KeseLimitGUI(playerMenuUtility, core, menuManager, messageManager, storageService, limitManager, economyService, config, interestService).open();
        Runnable openInterestGUI = () -> new InterestMainMenuGUI(playerMenuUtility, core, menuManager, messageManager, storageService, economyService, config, limitManager, interestService).open();
        Runnable openHistoryGUI = () -> new TransactionHistoryGUI(playerMenuUtility, core, menuManager, messageManager, storageService, economyService, config, limitManager, interestService).open();

        MenuItemConfig depositConfig = menuManager.getMenuItem(MENU_KEY, "deposit");
        actions.put(depositConfig.slot(), openDepositGUI);

        MenuItemConfig withdrawConfig = menuManager.getMenuItem(MENU_KEY, "withdraw");
        actions.put(withdrawConfig.slot(), openWithdrawGUI);

        MenuItemConfig sendConfig = menuManager.getMenuItem(MENU_KEY, "send");
        actions.put(sendConfig.slot(), openSendGUI);

        MenuItemConfig limitConfig = menuManager.getMenuItem(MENU_KEY, "limit");
        actions.put(limitConfig.slot(), openLimitGUI);

        MenuItemConfig interestConfig = menuManager.getMenuItem(MENU_KEY, "interest");
        actions.put(interestConfig.slot(), openInterestGUI);

        MenuItemConfig historyConfig = menuManager.getMenuItem(MENU_KEY, "history");
        actions.put(historyConfig.slot(), openHistoryGUI);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{bakiye}", numberFormat.format(economy.getBalance(p)));

        inventory.setItem(depositConfig.slot(), createItemFromConfig(depositConfig, placeholders));
        inventory.setItem(withdrawConfig.slot(), createItemFromConfig(withdrawConfig, placeholders));
        inventory.setItem(sendConfig.slot(), createItemFromConfig(sendConfig, placeholders));
        inventory.setItem(limitConfig.slot(), createItemFromConfig(limitConfig, placeholders));
        inventory.setItem(interestConfig.slot(), createItemFromConfig(interestConfig, placeholders));
        inventory.setItem(historyConfig.slot(), createItemFromConfig(historyConfig, placeholders));

        MenuItemConfig balanceConfig = menuManager.getMenuItem(MENU_KEY, "balance-display");
        inventory.setItem(balanceConfig.slot(), createItemFromConfig(balanceConfig, placeholders));

        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }
}