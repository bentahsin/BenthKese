/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.gui.menus;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.commands.impl.KeseKoyCommand;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.AnvilGUIHelper;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class KeseYatirGUI extends Menu {
    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final LimitManager limitManager;
    private final IStorageService storageService;
    private final InterestService interestService;
    private final KeseKoyCommand koyCommandLogic;

    public KeseYatirGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MessageManager messageManager, EconomyService economyService, ConfigurationManager configManager, IStorageService storageService, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.configManager = configManager;
        this.limitManager = limitManager;
        this.storageService = storageService;
        this.economyService = economyService;
        this.interestService = interestService;
        this.koyCommandLogic = new KeseKoyCommand(messageManager, economyService, configManager, storageService);
    }

    @Override
    public String getMenuName() {
        return messageManager.getMessage("gui.deposit-menu.title");
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void setMenuItems() {
        // Miktar girmek için AnvilGUI butonu
        actions.put(11, this::openDepositAnvil);
        inventory.setItem(11, createGuiItem(Material.ANVIL,
                messageManager.getMessage("gui.deposit-menu.anvil.name"),
                messageManager.getMessageList("gui.deposit-menu.anvil.lore").toArray(new String[0]))
        );

        // Elindekini yatır butonu
        actions.put(13, () -> {
            koyCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{"el"});
            playerMenuUtility.getOwner().closeInventory();
        });
        inventory.setItem(13, createGuiItem(Material.ARMOR_STAND,
                messageManager.getMessage("gui.deposit-menu.hand.name"),
                messageManager.getMessageList("gui.deposit-menu.hand.lore").toArray(new String[0]))
        );

        // Envanterdekileri yatır butonu
        actions.put(15, () -> {
            koyCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{"envanter"});
            playerMenuUtility.getOwner().closeInventory();
        });
        inventory.setItem(15, createGuiItem(Material.CHEST,
                messageManager.getMessage("gui.deposit-menu.inventory.name"),
                messageManager.getMessageList("gui.deposit-menu.inventory.lore").toArray(new String[0]))
        );

        // Hızlı yatırma butonları
        addDepositButton(21, 16);
        addDepositButton(22, 32);
        addDepositButton(23, 64);

        // Geri butonu
        actions.put(35, () -> new KeseMainMenuGUI(playerMenuUtility, plugin, messageManager, economyService, configManager, storageService, limitManager, interestService).open());
        inventory.setItem(35, createGuiItem(Material.BARRIER, messageManager.getMessage("gui.general.back-button")));

        fillEmptySlots();
    }

    private void addDepositButton(int slot, int amount) {
        actions.put(slot, () -> {
            koyCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{String.valueOf(amount)});
            playerMenuUtility.getOwner().closeInventory();
        });

        inventory.setItem(slot, createGuiItem(Material.GOLD_NUGGET,
                messageManager.getMessage("gui.deposit-menu.quick-deposit.name").replace("{miktar}", String.valueOf(amount)),
                messageManager.getMessageList("gui.deposit-menu.quick-deposit.lore")
                        .stream()
                        .map(line -> line.replace("{miktar}", String.valueOf(amount))).toArray(String[]::new))
        );
    }

    private void openDepositAnvil() {
        AnvilGUIHelper.createAmountInput(
                plugin,
                playerMenuUtility.getOwner(),
                messageManager,
                messageManager.getMessage("gui.deposit-menu.anvil-title"),
                new ItemStack(Material.PAPER),
                (amount) -> {
                    koyCommandLogic.execute(playerMenuUtility.getOwner(), new String[]{String.valueOf(amount.intValue())});
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                }
        );
    }
}