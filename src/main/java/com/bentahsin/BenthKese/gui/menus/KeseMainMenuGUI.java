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
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Locale;

public class KeseMainMenuGUI extends Menu {

    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final InterestService interestService;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseMainMenuGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MessageManager messageManager, EconomyService economyService, ConfigurationManager configManager, IStorageService storageService, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.economyService = economyService;
        this.configManager = configManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.interestService = interestService;
    }

    @Override
    public String getMenuName() {
        return messageManager.getMessage("gui.main-menu.title");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        Player p = playerMenuUtility.getOwner();

        // Para Yatır Butonu
        actions.put(10, () -> new KeseYatirGUI(playerMenuUtility, plugin, messageManager, economyService, configManager, storageService, limitManager, interestService).open());
        inventory.setItem(10, createGuiItem(Material.GOLD_INGOT,
                messageManager.getMessage("gui.main-menu.deposit.name"),
                messageManager.getMessageList("gui.main-menu.deposit.lore").toArray(new String[0]))
        );

        // Para Çek Butonu
        actions.put(12, () -> new KeseCekGUI(playerMenuUtility, plugin, messageManager, economyService, configManager, storageService, limitManager, interestService).open());
        inventory.setItem(12, createGuiItem(Material.DIAMOND,
                messageManager.getMessage("gui.main-menu.withdraw.name"),
                messageManager.getMessageList("gui.main-menu.withdraw.lore").toArray(new String[0]))
        );

        // Para Gönder Butonu
        actions.put(14, () -> new KeseGonderOyuncuGUI(plugin, playerMenuUtility, messageManager, storageService, limitManager, configManager).open());
        inventory.setItem(14, createGuiItem(Material.ENDER_PEARL,
                messageManager.getMessage("gui.main-menu.send.name"),
                messageManager.getMessageList("gui.main-menu.send.lore").toArray(new String[0]))
        );

        // Limit Bilgileri Butonu
        actions.put(16, () -> new KeseLimitGUI(playerMenuUtility, plugin, messageManager, storageService, limitManager, economyService, configManager, interestService).open());
        inventory.setItem(16, createGuiItem(Material.BEACON,
                messageManager.getMessage("gui.main-menu.limit.name"),
                messageManager.getMessageList("gui.main-menu.limit.lore").toArray(new String[0]))
        );

        // Faiz Sistemi Ana Menü Butonu
        actions.put(20, () -> new InterestMainMenuGUI(playerMenuUtility, plugin, messageManager, storageService, economyService, configManager, limitManager, interestService).open());
        inventory.setItem(20, createGuiItem(Material.KNOWLEDGE_BOOK,
                messageManager.getMessage("gui.main-menu.interest.name"),
                messageManager.getMessageList("gui.main-menu.interest.lore").toArray(new String[0]))
        );

        String balance = numberFormat.format(economy.getBalance(p));
        inventory.setItem(4, createGuiItem(Material.EMERALD,
                messageManager.getMessage("gui.main-menu.balance.name").replace("{bakiye}", balance),
                messageManager.getMessageList("gui.main-menu.balance.lore").toArray(new String[0]))
        );

        actions.put(24, () -> new TransactionHistoryGUI(playerMenuUtility, plugin, messageManager, storageService, economyService, configManager, limitManager, interestService).open());
        inventory.setItem(24, createGuiItem(Material.WRITABLE_BOOK,
                messageManager.getMessage("gui.history-menu.main-menu-button.name"),
                messageManager.getMessageList("gui.history-menu.main-menu-button.lore").toArray(new String[0]))
        );


        fillEmptySlots();
    }
}