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
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InterestAccountDetailsGUI extends Menu {

    private static final String MENU_KEY = "interest-details-menu";

    // Bağımlılıklar
    private final BenthKese plugin;
    private final MenuManager menuManager;
    private final MessageManager messageManager;
    private final InterestService interestService;
    private final IStorageService storageService;
    private final EconomyService economyService;
    private final ConfigurationManager configManager;
    private final LimitManager limitManager;

    // Görüntülenen Hesap
    private final InterestAccount account;

    // Formatlayıcılar
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public InterestAccountDetailsGUI(PlayerMenuUtility playerMenuUtility, BenthKese plugin, MenuManager menuManager, MessageManager messageManager, InterestService interestService, InterestAccount account, IStorageService storageService, EconomyService economyService, ConfigurationManager configManager, LimitManager limitManager) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.menuManager = menuManager;
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
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{id}", String.valueOf(account.getAccountId()));
        return menuManager.getMenuTitle(MENU_KEY, placeholders);
    }

    @Override
    public int getSlots() {
        return menuManager.getMenuSize(MENU_KEY);
    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();
        boolean isMature = System.currentTimeMillis() >= account.getEndTime();

        // --- Bilgi Paneli ---
        MenuItemConfig infoConfig = menuManager.getMenuItem(MENU_KEY, "info-item");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{id}", String.valueOf(account.getAccountId()));
        placeholders.put("{anapara}", numberFormat.format(account.getPrincipal()));
        placeholders.put("{oran}", numberFormat.format(account.getInterestRate() * 100));
        placeholders.put("{kazanc}", numberFormat.format(account.getFinalAmount()));
        placeholders.put("{baslangic}", dateFormat.format(new Date(account.getStartTime())));
        placeholders.put("{bitis}", dateFormat.format(new Date(account.getEndTime())));
        placeholders.put("{kalan_sure}", com.bentahsin.BenthKese.utils.TimeUtil.formatDuration(account.getEndTime() - System.currentTimeMillis()));
        inventory.setItem(infoConfig.getSlot(), createItemFromConfig(infoConfig, placeholders));


        // --- Aksiyon Butonları ---
        if (isMature) {
            // Vadesi dolmuşsa: Parayı Çek Butonu
            MenuItemConfig claimConfig = menuManager.getMenuItem(MENU_KEY, "claim-button");
            actions.put(claimConfig.getSlot(), () -> {
                interestService.processAccountAction(player, account.getAccountId());
                // İşlem sonrası YENİDEN OLUŞTURULMUŞ listeye dön
                openListMenu();
            });
            inventory.setItem(claimConfig.getSlot(), createItemFromConfig(claimConfig, placeholders));

        } else {
            // Vadesi dolmamışsa: Hesabı Boz Butonu
            MenuItemConfig breakConfig = menuManager.getMenuItem(MENU_KEY, "break-button");
            actions.put(breakConfig.getSlot(), () -> {
                // Onay menüsünü aç
                MenuItemConfig confirmItemConfig = menuManager.getMenuItem(MENU_KEY, "break-confirmation-item");
                ItemStack infoItem = createItemFromConfig(confirmItemConfig, placeholders);

                // *** ÇÖZÜM BURADA: Onay veya iptal sonrası YENİ BİR LİSTE MENÜSÜ açılır. ***
                Runnable onConfirm = () -> {
                    interestService.processAccountAction(player, account.getAccountId());
                    // İşlem sonrası YENİDEN OLUŞTURULMUŞ listeye dön
                    openListMenu();
                };

                // Oyuncu iptal ederse de YENİDEN OLUŞTURULMUŞ listeye dönsün.
                Runnable onCancel = this::openListMenu;

                new ConfirmationGUI(playerMenuUtility, menuManager, infoItem, onConfirm, onCancel).open();
            });
            inventory.setItem(breakConfig.getSlot(), createItemFromConfig(breakConfig, placeholders));
        }

        // --- Geri Butonu ---
        MenuItemConfig backConfig = menuManager.getMenuItem(MENU_KEY, "back-button");
        // Geri butonu her zaman YENİDEN OLUŞTURULMUŞ listeye döner.
        actions.put(backConfig.getSlot(), this::openListMenu);
        inventory.setItem(backConfig.getSlot(), createItemFromConfig(backConfig, new HashMap<>()));


        // Boşlukları doldur
        fillEmptySlots(menuManager.getMenuItem(MENU_KEY, "filler-item"));
    }

    /**
     * InterestAccountListGUI menüsünü yeniden oluşturarak açar ve listenin yenilenmesini sağlar.
     */
    private void openListMenu() {
        new InterestAccountListGUI(
                playerMenuUtility, plugin, menuManager,
                messageManager, storageService, economyService,
                configManager, limitManager, interestService
        ).open();
    }
}