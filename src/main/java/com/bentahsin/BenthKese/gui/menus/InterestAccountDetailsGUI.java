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
import com.bentahsin.BenthKese.data.InterestAccount;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.TimeUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InterestAccountDetailsGUI extends Menu {

    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final InterestService interestService;
    private final IStorageService storageService;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final LimitManager limitManager;
    private final InterestAccount account; // Görüntülenen hesap
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public InterestAccountDetailsGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MessageManager messageManager, InterestService interestService, InterestAccount account, IStorageService storageService, EconomyService economyService, ConfigurationManager configManager, LimitManager limitManager) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.interestService = interestService;
        this.account = account;
        this.storageService = storageService;
        this.economyService = economyService;
        this.configManager = configManager;
        this.limitManager = limitManager;
    }

    @Override
    public String getMenuName() {
        return messageManager.getMessage("gui.interest-details.title").replace("{id}", String.valueOf(account.getAccountId()));
    }

    @Override
    public int getSlots() {
        return 27; // 3 sıra
    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();

        // --- Bilgi Paneli ---

        inventory.setItem(13, createGuiItem(Material.PAPER,
                messageManager.getMessage("gui.interest-details.info-item.name"),
                messageManager.getMessageList("gui.interest-details.info-item.lore").stream()
                        .map(line -> line
                                .replace("{anapara}", numberFormat.format(account.getPrincipal()))
                                .replace("{oran}", String.format("%.2f", account.getInterestRate() * 100))
                                .replace("{kazanc}", numberFormat.format(account.getFinalAmount()))
                                .replace("{baslangic}", dateFormat.format(new Date(account.getStartTime())))
                                .replace("{bitis}", dateFormat.format(new Date(account.getEndTime())))
                                .replace("{kalan_sure}", TimeUtil.formatDuration(account.getEndTime() - System.currentTimeMillis()))).toArray(String[]::new)));

        // --- İşlem Butonu (Dinamik) ---
        long remainingMillis = account.getEndTime() - System.currentTimeMillis();
        boolean isMature = remainingMillis <= 0;

        if (isMature) {
            // Vadesi dolmuş, parayı çekme butonu
            actions.put(11, () -> {
                interestService.processAccountAction(player, account.getAccountId());
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                // İşlem bitince liste menüsüne geri dön
                new InterestAccountListGUI(playerMenuUtility, plugin, messageManager, storageService, economyService, configManager, limitManager, interestService).open();
            });
            inventory.setItem(11, createGuiItem(Material.GOLD_BLOCK,
                    messageManager.getMessage("gui.interest-details.claim-button.name"),
                    messageManager.getMessageList("gui.interest-details.claim-button.lore").toArray(new String[0]))
            );
        } else {
            actions.put(11, () -> {
                ItemStack infoItem = createGuiItem(Material.REDSTONE_BLOCK,
                        messageManager.getMessage("gui.interest-details.break-confirmation-item.name"),
                        messageManager.getMessageList("gui.interest-details.break-confirmation-item.lore").stream()
                                .map(line -> line.replace("{id}", String.valueOf(account.getAccountId()))
                                        .replace("{anapara}", numberFormat.format(account.getPrincipal())))
                                .toArray(String[]::new)
                );

                Runnable onConfirm = () -> {
                    interestService.processAccountAction(player, account.getAccountId());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                    new InterestAccountListGUI(playerMenuUtility, plugin, messageManager, storageService, economyService, configManager, limitManager, interestService).open();
                };

                Runnable onCancel = this::open;

                new ConfirmationGUI(playerMenuUtility, messageManager.getMessage("gui.confirmation.title"), infoItem, onConfirm, onCancel, messageManager).open();
            });
            inventory.setItem(11, createGuiItem(Material.REDSTONE_BLOCK,
                    messageManager.getMessage("gui.interest-details.break-button.name"),
                    messageManager.getMessageList("gui.interest-details.break-button.lore").toArray(new String[0]))
            );
        }

        // Geri butonu (Liste menüsüne döner)
        new InterestAccountListGUI(playerMenuUtility, plugin, messageManager, storageService, economyService, configManager, limitManager, interestService).open();
        inventory.setItem(26, createGuiItem(Material.BARRIER, messageManager.getMessage("gui.general.back-button")));

        fillEmptySlots();
    }
}