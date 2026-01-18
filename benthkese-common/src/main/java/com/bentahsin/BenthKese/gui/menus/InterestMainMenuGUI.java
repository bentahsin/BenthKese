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
import com.bentahsin.BenthKese.utils.AnvilGUIHelper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Objects;

public class InterestMainMenuGUI extends Menu {

    private static final String MENU_KEY = "interest-main-menu";

    private final BenthKeseCore core;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final EconomyService economyService;
    private final BenthConfig config;
    private final LimitManager limitManager;
    private final InterestService interestService;

    public InterestMainMenuGUI(PlayerMenuUtility playerMenuUtility, BenthKeseCore core, MenuManager menuManager, MessageManager messageManager, IStorageService storageService, EconomyService economyService, BenthConfig config, LimitManager limitManager, InterestService interestService) {
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
        MenuItemConfig listConfig = menuManager.getMenuItem(MENU_KEY, "list-accounts");
        MenuItemConfig createConfig = menuManager.getMenuItem(MENU_KEY, "create-account");
        MenuItemConfig backConfig = menuManager.getMenuItem(MENU_KEY, "back-button");

        actions.put(listConfig.slot(), () -> new InterestAccountListGUI(playerMenuUtility, core, menuManager, messageManager, storageService, economyService, config, limitManager, interestService).open());
        actions.put(createConfig.slot(), this::openAmountAnvil);
        actions.put(backConfig.slot(), () -> new KeseMainMenuGUI(playerMenuUtility, core, menuManager, messageManager, economyService, config, storageService, limitManager, interestService).open());

        inventory.setItem(listConfig.slot(), createItemFromConfig(listConfig, Collections.emptyMap()));
        inventory.setItem(createConfig.slot(), createItemFromConfig(createConfig, Collections.emptyMap()));
        inventory.setItem(backConfig.slot(), createItemFromConfig(backConfig, Collections.emptyMap()));

        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }

    private void openAmountAnvil() {
        Player player = playerMenuUtility.getOwner();
        Section section = menuManager.getMenuSection(MENU_KEY + ".anvil-amount");
        if (section == null) {
            player.sendMessage(ChatColor.RED + "Hata: " + MENU_KEY + ".anvil-amount yapılandırması bulunamadı!");
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(section.getString("title", "&8Miktar Gir")));
        Material displayMaterial = Material.matchMaterial(Objects.requireNonNull(section.getString("display-item", "GOLD_INGOT")));
        if (displayMaterial == null) displayMaterial = Material.GOLD_INGOT;

        AnvilGUIHelper.createAmountInput(
                core.getPlugin(),
                player,
                messageManager,
                title,
                new ItemStack(displayMaterial),
                (amount) -> {
                    playerMenuUtility.setTemporaryAmount(amount);
                    return Collections.singletonList(AnvilGUI.ResponseAction.run(this::openDurationAnvil));
                }
        );
    }

    private void openDurationAnvil() {
        Player player = playerMenuUtility.getOwner();
        double amount = playerMenuUtility.getTemporaryAmount();

        Section section = menuManager.getMenuSection(MENU_KEY + ".anvil-duration");
        if (section == null) {
            player.sendMessage(ChatColor.RED + "Hata: " + MENU_KEY + ".anvil-duration yapılandırması bulunamadı!");
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(section.getString("title", "&8Süre Gir")));
        Material displayMaterial = Material.matchMaterial(Objects.requireNonNull(section.getString("display-item", "CLOCK")));
        if (displayMaterial == null) displayMaterial = Material.CLOCK;

        new AnvilGUI.Builder()
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) return Collections.emptyList();
                    String durationString = stateSnapshot.getText();
                    interestService.createAccount(player, amount, durationString);
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .onClose(state -> player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f))
                .text("7d")
                .itemLeft(new ItemStack(displayMaterial))
                .title(title)
                .plugin(core.getPlugin())
                .open(player);
    }
}