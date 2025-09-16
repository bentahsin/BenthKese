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
import com.bentahsin.BenthKese.data.InterestAccount;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.PaginatedMenu;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.*;

public class InterestAccountListGUI extends PaginatedMenu<InterestAccount> {

    private static final String MENU_KEY = "interest-list-menu";

    private final BenthKese plugin;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final InterestService interestService;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    private final Map<Integer, InterestAccount> displayedAccounts = new HashMap<>();

    public InterestAccountListGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MenuManager menuManager, MessageManager messageManager, IStorageService storageService, EconomyService economyService, ConfigurationManager configManager, LimitManager limitManager, InterestService interestService) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.economyService = economyService;
        this.configManager = configManager;
        this.interestService = interestService;
    }

    @Override
    public void open() {
        super.open();
        startUpdateTask(plugin, 20L, 20L);
    }

    @Override
    public void setMenuItems() {
        displayedAccounts.clear();

        addNavigationControls();

        List<InterestAccount> allItems = getAllItems();
        if (allItems == null || allItems.isEmpty()) {
            fillEmptySlots(getFillerItemConfig());
            return;
        }

        int maxItemsPerPage = getSlots() - 9;
        int startIndex = page * maxItemsPerPage;

        for (int i = 0; i < maxItemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex >= allItems.size()) break;

            InterestAccount currentItem = allItems.get(itemIndex);

            displayedAccounts.put(i, currentItem);

            inventory.setItem(i, getItemStack(currentItem));
            actions.put(i, () -> onItemClick(currentItem));
        }

        fillEmptySlots(getFillerItemConfig());
    }

    @Override
    public void onUpdate() {
        for (Map.Entry<Integer, InterestAccount> entry : displayedAccounts.entrySet()) {
            int slot = entry.getKey();
            InterestAccount account = entry.getValue();

            ItemStack item = inventory.getItem(slot);
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null || meta.getLore() == null) continue;

            if (!account.isActive()) {
                open();
                return;
            }

            long remainingMillis = account.getEndTime() - System.currentTimeMillis();
            String newFormattedTime = TimeUtil.formatDuration(remainingMillis);

            List<String> newLore = new ArrayList<>();
            for (String line : meta.getLore()) {
                if (ChatColor.stripColor(line).startsWith("Kalan Süre:")) {
                    newLore.add(ChatColor.translateAlternateColorCodes('&', "&7Kalan Süre: &d" + newFormattedTime));
                } else {
                    newLore.add(line);
                }
            }

            meta.setLore(newLore);
            item.setItemMeta(meta);
        }
    }

    @Override
    public String getMenuName() {
        String titleTemplate = menuManager.getMenuTitle(MENU_KEY);
        return titleTemplate.replace("{sayfa}", String.valueOf(page + 1))
                .replace("{max_sayfa}", String.valueOf(Math.max(1, getMaxPages())));
    }

    @Override
    public int getSlots() {
        return menuManager.getMenuSize(MENU_KEY);
    }

    @Override
    public List<InterestAccount> getAllItems() {
        List<InterestAccount> accounts = storageService.getInterestAccounts(playerMenuUtility.getOwner().getUniqueId());
        accounts.sort(Comparator.comparingLong(InterestAccount::getStartTime));
        return accounts;
    }

    @Override
    public ItemStack getItemStack(InterestAccount account) {
        boolean isMature = !account.isActive();

        MenuItemConfig itemConfig = isMature
                ? menuManager.getMenuItem(MENU_KEY, "item-mature")
                : menuManager.getMenuItem(MENU_KEY, "item-active");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{id}", String.valueOf(account.getAccountId()));
        placeholders.put("{anapara}", numberFormat.format(account.getPrincipal()));
        placeholders.put("{oran}", numberFormat.format(account.getInterestRate() * 100));
        placeholders.put("{vade_sonu_tutar}", numberFormat.format(account.getFinalAmount()));
        placeholders.put("{kazanc}", numberFormat.format(account.getFinalAmount()));
        placeholders.put("{kalan_sure}", TimeUtil.formatDuration(account.getEndTime() - System.currentTimeMillis()));

        return createItemFromConfig(itemConfig, placeholders);
    }

    @Override
    public void onItemClick(InterestAccount item) {
        new InterestAccountDetailsGUI(
                playerMenuUtility, plugin, menuManager,
                messageManager, interestService, item,
                storageService, economyService, configManager, limitManager
        ).open();
    }

    @Override
    public Menu getNavigationBackMenu() {
        return new InterestMainMenuGUI(
                playerMenuUtility, plugin, menuManager,
                messageManager, storageService, economyService,
                configManager, limitManager, interestService
        );
    }

    @Override
    protected MenuItemConfig getBackButtonConfig() {
        return menuManager.getMenuItem(MENU_KEY, "back-button");
    }

    @Override
    protected MenuItemConfig getPreviousPageButtonConfig() {
        return menuManager.getMenuItem(MENU_KEY, "previous-page");
    }

    @Override
    protected MenuItemConfig getNextPageButtonConfig() {
        return menuManager.getMenuItem(MENU_KEY, "next-page");
    }

    @Override
    protected MenuItemConfig getFillerItemConfig() {
        return menuManager.getMenuItem(MENU_KEY, "filler-item");
    }
}