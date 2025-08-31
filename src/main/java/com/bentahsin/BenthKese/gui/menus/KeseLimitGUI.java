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
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class KeseLimitGUI extends Menu {
    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final EconomyService economyService;
    private final ConfigurationManager configurationManager;
    private final InterestService interestService;
    private final KeseLimitYukseltCommand yukseltCommandLogic;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public KeseLimitGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MessageManager messageManager, IStorageService storageService, LimitManager limitManager, EconomyService economyService, ConfigurationManager configManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.economyService = economyService;
        this.configurationManager = configManager;
        this.yukseltCommandLogic = new KeseLimitYukseltCommand(messageManager, storageService, limitManager);
        this.interestService = interestService;
    }

    @Override
    public String getMenuName() {
        return messageManager.getMessage("gui.limit-menu.title");
    }

    @Override
    public int getSlots() {
        return 27;
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

        String infiniteText = messageManager.getMessage("limit-info.infinite-text");
        String maxSend = currentLevel.getSendLimit() == -1 ? infiniteText : numberFormat.format(currentLevel.getSendLimit());
        double remainingSend = currentLevel.getSendLimit() == -1 ? Double.POSITIVE_INFINITY : currentLevel.getSendLimit() - playerData.getDailySent();
        String kalanSend = remainingSend == Double.POSITIVE_INFINITY ? infiniteText : numberFormat.format(Math.max(0, remainingSend));

        inventory.setItem(11, createGuiItem(Material.BOOK,
                messageManager.getMessage("gui.limit-menu.status.name"),
                messageManager.getMessageList("gui.limit-menu.status.lore").stream()
                        .map(line -> line.replace("{seviye_adi}", currentLevel.getName())
                                .replace("{kullanilan}", numberFormat.format(playerData.getDailySent()))
                                .replace("{max}", maxSend)
                                .replace("{kalan}", kalanSend)).toArray(String[]::new))
        );

        LimitLevel nextLevel = limitManager.getNextLevel(playerData.getLimitLevel());
        if (nextLevel != null) {
            double cost = nextLevel.getCost();
            if (economy.has(player, cost)) {
                actions.put(15, () -> {
                    // Onay menüsü için bilgi item'ını oluştur
                    ItemStack infoItem = createGuiItem(Material.EMERALD,
                            messageManager.getMessage("gui.limit-menu.confirmation-item.name"),
                            messageManager.getMessageList("gui.limit-menu.confirmation-item.lore").stream()
                                    .map(line -> line.replace("{mevcut_seviye}", currentLevel.getName())
                                            .replace("{yeni_seviye}", nextLevel.getName())
                                            .replace("{maliyet}", numberFormat.format(cost)))
                                    .toArray(String[]::new)
                    );

                    // Onaylandığında yapılacak işlem
                    Runnable onConfirm = () -> {
                        yukseltCommandLogic.execute(player, new String[0]);
                        // İşlem sonrası menüyü yenile
                        new KeseLimitGUI(playerMenuUtility, plugin, messageManager, storageService, limitManager, economyService, configurationManager, interestService).open();
                    };

                    // İptal edildiğinde yapılacak işlem (bu menüyü tekrar aç)
                    Runnable onCancel = this::open;

                    // Onay menüsünü aç
                    new ConfirmationGUI(playerMenuUtility, messageManager.getMessage("gui.confirmation.title"), infoItem, onConfirm, onCancel, messageManager).open();
                });

                inventory.setItem(15, createGuiItem(Material.EMERALD,
                        messageManager.getMessage("gui.limit-menu.upgrade.name"),
                        messageManager.getMessageList("gui.limit-menu.upgrade.lore").stream()
                                .map(line -> line.replace("{yeni_seviye}", nextLevel.getName())
                                        .replace("{maliyet}", numberFormat.format(cost))).toArray(String[]::new))
                );
            } else {
                actions.put(15, () -> player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f));
                inventory.setItem(15, createGuiItem(Material.REDSTONE_BLOCK,
                        messageManager.getMessage("gui.limit-menu.upgrade-fail.name"),
                        messageManager.getMessageList("gui.limit-menu.upgrade-fail.lore").stream()
                                .map(line -> line.replace("{yeni_seviye}", nextLevel.getName())
                                        .replace("{maliyet}", numberFormat.format(cost))
                                        .replace("{bakiye}", numberFormat.format(economy.getBalance(player)))).toArray(String[]::new))
                );
            }
        } else {
            inventory.setItem(15, createGuiItem(Material.BARRIER,
                    messageManager.getMessage("gui.limit-menu.max-level.name"),
                    messageManager.getMessageList("gui.limit-menu.max-level.lore").toArray(new String[0]))
            );
        }

        actions.put(26, () -> new KeseMainMenuGUI(playerMenuUtility, plugin, messageManager, economyService, configurationManager, storageService, limitManager, interestService).open());
        inventory.setItem(26, createGuiItem(Material.BARRIER, messageManager.getMessage("gui.general.back-button")));

        fillEmptySlots();
    }
}