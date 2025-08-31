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
import com.bentahsin.BenthKese.data.TransactionData;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.PaginatedMenu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryGUI extends PaginatedMenu<TransactionData> {

    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final LimitManager limitManager;
    private final InterestService interestService;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public TransactionHistoryGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MessageManager messageManager, IStorageService storageService, EconomyService economyService, ConfigurationManager configManager, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.economyService = economyService;
        this.configManager = configManager;
        this.limitManager = limitManager;
        this.interestService = interestService;
    }

    @Override
    public String getMenuName() {
        return messageManager.getMessage("gui.history-menu.title");
    }

    @Override
    public int getSlots() {
        return 54; // 6 sıra, daha fazla item için
    }

    @Override
    public List<TransactionData> getAllItems() {
        // Son 50 işlemi getir
        return storageService.getTransactions(playerMenuUtility.getOwner().getUniqueId(), 50);
    }

    @Override
    public ItemStack getItemStack(TransactionData transaction) {
        Material material;
        String name;
        String formattedAmount = numberFormat.format(transaction.getAmount());

        switch (transaction.getType()) {
            case SEND:
                material = Material.RED_WOOL;
                name = messageManager.getMessage("gui.history-menu.format.send")
                        .replace("{miktar}", formattedAmount)
                        .replace("{hedef}", transaction.getDescription());
                break;
            case RECEIVE:
                material = Material.LIME_WOOL;
                name = messageManager.getMessage("gui.history-menu.format.receive")
                        .replace("{miktar}", formattedAmount)
                        .replace("{gonderen}", transaction.getDescription());
                break;
            case DEPOSIT:
                material = Material.GOLD_INGOT;
                name = messageManager.getMessage("gui.history-menu.format.deposit")
                        .replace("{miktar}", formattedAmount)
                        .replace("{birim}", transaction.getDescription());
                break;
            case WITHDRAW:
                material = Material.DIAMOND;
                name = messageManager.getMessage("gui.history-menu.format.withdraw")
                        .replace("{miktar}", formattedAmount)
                        .replace("{birim}", transaction.getDescription());
                break;
            case LEVEL_UP:
                material = Material.BEACON;
                name = messageManager.getMessage("gui.history-menu.format.level-up")
                        .replace("{maliyet}", formattedAmount)
                        .replace("{seviye}", transaction.getDescription());
                break;
            default:
                material = Material.PAPER;
                name = "Bilinmeyen İşlem";
        }

        String lore = messageManager.getMessage("gui.history-menu.format.lore")
                .replace("{tarih}", dateFormat.format(new Date(transaction.getTimestamp())));

        return createGuiItem(material, name, lore);
    }

    @Override
    public void onItemClick(TransactionData item) {
        // Tıklayınca bir şey yapmasına gerek yok
    }

    @Override
    public Menu getNavigationBackMenu() {
        return new KeseMainMenuGUI(playerMenuUtility, plugin, messageManager, economyService, configManager, storageService, limitManager, interestService);
    }
}