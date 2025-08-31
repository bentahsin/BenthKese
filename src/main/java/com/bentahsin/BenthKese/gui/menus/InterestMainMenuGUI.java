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
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class InterestMainMenuGUI extends Menu {
    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final LimitManager limitManager;
    private final InterestService interestService;

    public InterestMainMenuGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MessageManager messageManager, IStorageService storageService, EconomyService economyService, ConfigurationManager configManager, LimitManager limitManager, InterestService interestService) {
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
        return messageManager.getMessage("gui.interest-main.title");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        // Hesapları Listele Butonu
        actions.put(11, () -> new InterestAccountListGUI(playerMenuUtility, plugin, messageManager, storageService, economyService, configManager, limitManager, interestService).open());
        inventory.setItem(11, createGuiItem(Material.BOOK,
                messageManager.getMessage("gui.interest-main.list-accounts.name"),
                messageManager.getMessageList("gui.interest-main.list-accounts.lore").toArray(new String[0]))
        );

        // Yeni Hesap Oluştur Butonu
        actions.put(15, this::openAmountAnvil);
        inventory.setItem(15, createGuiItem(Material.EMERALD,
                messageManager.getMessage("gui.interest-main.create-account.name"),
                messageManager.getMessageList("gui.interest-main.create-account.lore").toArray(new String[0]))
        );

        // Ana Menüye Geri Dön Butonu
        actions.put(26, () -> new KeseMainMenuGUI(playerMenuUtility, plugin, messageManager, economyService, configManager, storageService, limitManager, interestService).open());
        inventory.setItem(26, createGuiItem(Material.BARRIER, messageManager.getMessage("gui.general.back-button")));

        fillEmptySlots();
    }

    private void openAmountAnvil() {
        Player player = playerMenuUtility.getOwner();
        new AnvilGUI.Builder()
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) return Collections.emptyList();
                    try {
                        double amount = Double.parseDouble(stateSnapshot.getText());
                        if (amount <= 0) return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("invalid-amount")));

                        playerMenuUtility.setTemporaryAmount(amount); // Miktarı geçici olarak sakla
                        return Collections.singletonList(AnvilGUI.ResponseAction.run(this::openDurationAnvil)); // Süre Anvil'ini aç
                    } catch (NumberFormatException e) {
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(messageManager.getMessage("gui.anvil.not-a-number")));
                    }
                })
                .text(messageManager.getMessage("gui.anvil.default-amount-text"))
                .itemLeft(new ItemStack(Material.GOLD_INGOT))
                .title(messageManager.getMessage("gui.interest-main.anvil-amount-title"))
                .plugin(plugin)
                .open(player);
    }

    private void openDurationAnvil() {
        Player player = playerMenuUtility.getOwner();
        double amount = playerMenuUtility.getTemporaryAmount();

        new AnvilGUI.Builder()
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) return Collections.emptyList();

                    String durationString = stateSnapshot.getText();
                    interestService.createAccount(player, amount, durationString); // Servis metodunu çağır
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .onClose(state -> player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f))
                .text("7d") // Örnek metin
                .itemLeft(new ItemStack(Material.CLOCK))
                .title(messageManager.getMessage("gui.interest-main.anvil-duration-title"))
                .plugin(plugin)
                .open(player);
    }
}