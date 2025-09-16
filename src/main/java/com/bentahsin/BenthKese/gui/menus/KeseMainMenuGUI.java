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

    private final BenthKese plugin;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final InterestService interestService;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseMainMenuGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MenuManager menuManager, MessageManager messageManager, EconomyService economyService, ConfigurationManager configManager, IStorageService storageService, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.economyService = economyService;
        this.configManager = configManager;
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

        Runnable openDepositGUI = () -> new KeseYatirGUI(playerMenuUtility, plugin, menuManager, messageManager, economyService, configManager, storageService, limitManager, interestService).open();
        Runnable openWithdrawGUI = () -> new KeseCekGUI(playerMenuUtility, plugin, menuManager, messageManager, economyService, configManager, storageService, limitManager, interestService).open();
        Runnable openSendGUI = () -> new KeseGonderOyuncuGUI(playerMenuUtility, plugin, menuManager, messageManager, storageService, limitManager, configManager).open();
        Runnable openLimitGUI = () -> new KeseLimitGUI(playerMenuUtility, plugin, menuManager, messageManager, storageService, limitManager, economyService, configManager, interestService).open();
        Runnable openInterestGUI = () -> new InterestMainMenuGUI(playerMenuUtility, plugin, menuManager, messageManager, storageService, economyService, configManager, limitManager, interestService).open();
        Runnable openHistoryGUI = () -> new TransactionHistoryGUI(playerMenuUtility, plugin, menuManager, messageManager, storageService, economyService, configManager, limitManager, interestService).open();

        MenuItemConfig depositConfig = menuManager.getMenuItem(MENU_KEY, "deposit");
        actions.put(depositConfig.getSlot(), openDepositGUI);

        MenuItemConfig withdrawConfig = menuManager.getMenuItem(MENU_KEY, "withdraw");
        actions.put(withdrawConfig.getSlot(), openWithdrawGUI);

        MenuItemConfig sendConfig = menuManager.getMenuItem(MENU_KEY, "send");
        actions.put(sendConfig.getSlot(), openSendGUI);

        MenuItemConfig limitConfig = menuManager.getMenuItem(MENU_KEY, "limit");
        actions.put(limitConfig.getSlot(), openLimitGUI);

        MenuItemConfig interestConfig = menuManager.getMenuItem(MENU_KEY, "interest");
        actions.put(interestConfig.getSlot(), openInterestGUI);

        MenuItemConfig historyConfig = menuManager.getMenuItem(MENU_KEY, "history");
        actions.put(historyConfig.getSlot(), openHistoryGUI);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{bakiye}", numberFormat.format(economy.getBalance(p)));

        inventory.setItem(depositConfig.getSlot(), createItemFromConfig(depositConfig, placeholders));
        inventory.setItem(withdrawConfig.getSlot(), createItemFromConfig(withdrawConfig, placeholders));
        inventory.setItem(sendConfig.getSlot(), createItemFromConfig(sendConfig, placeholders));
        inventory.setItem(limitConfig.getSlot(), createItemFromConfig(limitConfig, placeholders));
        inventory.setItem(interestConfig.getSlot(), createItemFromConfig(interestConfig, placeholders));
        inventory.setItem(historyConfig.getSlot(), createItemFromConfig(historyConfig, placeholders));

        MenuItemConfig balanceConfig = menuManager.getMenuItem(MENU_KEY, "balance-display");
        inventory.setItem(balanceConfig.getSlot(), createItemFromConfig(balanceConfig, placeholders));

        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }
}