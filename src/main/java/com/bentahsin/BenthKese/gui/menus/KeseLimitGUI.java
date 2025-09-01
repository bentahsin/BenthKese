/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.commands.impl.KeseLimitYukseltCommand;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MenuItemConfig;
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KeseLimitGUI extends Menu {

    private static final String MENU_KEY = "limit-menu";

    private final BenthKese plugin;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final EconomyService economyService;
    private final ConfigurationManager configurationManager;
    private final InterestService interestService;
    private final KeseLimitYukseltCommand yukseltCommandLogic;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseLimitGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MenuManager menuManager, MessageManager messageManager, IStorageService storageService, LimitManager limitManager, EconomyService economyService, ConfigurationManager configManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.economyService = economyService;
        this.configurationManager = configManager;
        this.interestService = interestService;
        this.yukseltCommandLogic = new KeseLimitYukseltCommand(messageManager, storageService, limitManager);
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
        Player player = playerMenuUtility.getOwner();
        PlayerData playerData = storageService.getPlayerData(player.getUniqueId());

        if (System.currentTimeMillis() - playerData.getLastResetTime() > TimeUnit.DAYS.toMillis(1)) {
            playerData.resetDailyLimits();
            storageService.savePlayerData(playerData);
        }

        LimitLevel currentLevel = limitManager.getLimitLevel(playerData.getLimitLevel());
        if (currentLevel == null) {
            player.closeInventory();
            return;
        }

        // --- Durum Bilgisi Paneli ---
        String infiniteText = messageManager.getMessage("limit-info.infinite-text");
        String maxSend = currentLevel.getSendLimit() == -1 ? infiniteText : numberFormat.format(currentLevel.getSendLimit());
        double remainingSend = currentLevel.getSendLimit() == -1 ? Double.POSITIVE_INFINITY : currentLevel.getSendLimit() - playerData.getDailySent();
        String kalanSend = remainingSend == Double.POSITIVE_INFINITY ? infiniteText : numberFormat.format(Math.max(0, remainingSend));

        Map<String, String> statusPlaceholders = new HashMap<>();
        statusPlaceholders.put("{seviye_adi}", currentLevel.getName());
        statusPlaceholders.put("{kullanilan}", numberFormat.format(playerData.getDailySent()));
        statusPlaceholders.put("{max}", maxSend);
        statusPlaceholders.put("{kalan}", kalanSend);

        MenuItemConfig statusConfig = menuManager.getMenuItem(MENU_KEY, "status-display");
        inventory.setItem(statusConfig.getSlot(), createItemFromConfig(statusConfig, statusPlaceholders));

        // --- Seviye Yükseltme Paneli (Dinamik) ---
        LimitLevel nextLevel = limitManager.getNextLevel(playerData.getLimitLevel());
        if (nextLevel != null) {
            double cost = nextLevel.getCost();
            Map<String, String> upgradePlaceholders = new HashMap<>();
            upgradePlaceholders.put("{yeni_seviye}", nextLevel.getName());
            upgradePlaceholders.put("{maliyet}", numberFormat.format(cost));

            if (economy.has(player, cost)) {
                MenuItemConfig upgradeConfig = menuManager.getMenuItem(MENU_KEY, "upgrade-success");
                MenuItemConfig confirmItemConfig = menuManager.getMenuItem(MENU_KEY, "confirmation-item");

                ItemStack infoItem = createItemFromConfig(confirmItemConfig, upgradePlaceholders);

                Runnable onConfirm = () -> {
                    yukseltCommandLogic.execute(player, new String[0]);
                    this.open(); // Menüyü yenile
                };
                Runnable onCancel = this::open;

                actions.put(upgradeConfig.getSlot(), () -> new ConfirmationGUI(playerMenuUtility, menuManager, infoItem, onConfirm, onCancel).open());
                inventory.setItem(upgradeConfig.getSlot(), createItemFromConfig(upgradeConfig, upgradePlaceholders));

            } else {
                upgradePlaceholders.put("{bakiye}", numberFormat.format(economy.getBalance(player)));
                MenuItemConfig failConfig = menuManager.getMenuItem(MENU_KEY, "upgrade-fail");

                actions.put(failConfig.getSlot(), () -> player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f));
                inventory.setItem(failConfig.getSlot(), createItemFromConfig(failConfig, upgradePlaceholders));
            }
        } else {
            MenuItemConfig maxLevelConfig = menuManager.getMenuItem(MENU_KEY, "max-level");
            inventory.setItem(maxLevelConfig.getSlot(), createItemFromConfig(maxLevelConfig, new HashMap<>()));
        }

        // --- Geri Butonu ---
        MenuItemConfig backConfig = menuManager.getMenuItem(MENU_KEY, "back-button");
        actions.put(backConfig.getSlot(), () -> new KeseMainMenuGUI(playerMenuUtility, plugin, menuManager, messageManager, economyService, configurationManager, storageService, limitManager, interestService).open());
        inventory.setItem(backConfig.getSlot(), createItemFromConfig(backConfig, new HashMap<>()));

        // --- Boşlukları Doldur ---
        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }
}