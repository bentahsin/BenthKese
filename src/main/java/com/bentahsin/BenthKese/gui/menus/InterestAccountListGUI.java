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
import com.bentahsin.BenthKese.gui.PaginatedMenu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.TimeUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class InterestAccountListGUI extends PaginatedMenu<InterestAccount> {

    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final InterestService interestService;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public InterestAccountListGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MessageManager messageManager, IStorageService storageService, EconomyService economyService, ConfigurationManager configManager, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.economyService = economyService;
        this.configManager = configManager;
        this.interestService = interestService;
    }

    @Override
    public String getMenuName() {
        return messageManager.getMessage("gui.interest-list.title");
    }

    @Override
    public int getSlots() {
        return 27; // 3 sıra. 18 slot öğeler, 9 slot navigasyon için.
    }

    @Override
    public List<InterestAccount> getAllItems() {
        return storageService.getInterestAccounts(playerMenuUtility.getOwner().getUniqueId());
    }

    @Override
    public ItemStack getItemStack(InterestAccount account) {
        long remainingMillis = account.getEndTime() - System.currentTimeMillis();
        boolean isMature = remainingMillis <= 0;

        Material material = isMature ? Material.GOLD_BLOCK : Material.IRON_INGOT;
        String name = isMature ? messageManager.getMessage("gui.interest-list.item-mature.name")
                : messageManager.getMessage("gui.interest-list.item-active.name");

        List<String> loreTemplate = isMature ? messageManager.getMessageList("gui.interest-list.item-mature.lore")
                : messageManager.getMessageList("gui.interest-list.item-active.lore");

        return createGuiItem(material, name.replace("{id}", String.valueOf(account.getAccountId())), loreTemplate.stream()
                .map(line -> line
                        .replace("{id}", String.valueOf(account.getAccountId()))
                        .replace("{anapara}", numberFormat.format(account.getPrincipal()))
                        .replace("{kazanc}", numberFormat.format(account.getFinalAmount()))
                        .replace("{kalan_sure}", TimeUtil.formatDuration(remainingMillis))).toArray(String[]::new));
    }

    @Override
    public void onItemClick(InterestAccount item) {
        new InterestAccountDetailsGUI(
                playerMenuUtility,
                plugin,
                messageManager,
                interestService,
                item,
                storageService,
                economyService,
                configManager,
                limitManager
        ).open();
    }

    @Override
    public Menu getNavigationBackMenu() {
        // "Geri" butonu ana menüye dönecek.
        return new KeseMainMenuGUI(playerMenuUtility, plugin, messageManager,
                economyService, configManager,
                storageService, limitManager, interestService);
    }
}