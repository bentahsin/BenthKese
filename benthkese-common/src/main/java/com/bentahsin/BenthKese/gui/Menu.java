package com.bentahsin.BenthKese.gui;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.configuration.MenuItemConfig;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    protected PlayerMenuUtility playerMenuUtility;
    protected Map<Integer, Runnable> actions = new HashMap<>();

    private boolean isUpdating = false;

    public Menu(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
    }

    public abstract String getMenuName();
    public abstract int getSlots();
    public abstract void setMenuItems();

    public final void handleMenu(InventoryClickEvent e) {
        if (actions.containsKey(e.getSlot())) {
            actions.get(e.getSlot()).run();
        }
    }

    public void open() {
        stopUpdateTask();

        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        this.setMenuItems();
        playerMenuUtility.getOwner().openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Bu menü için periyodik bir güncelleme görevi başlatır.
     * Görev, her 'period' tick'te bir onUpdate() metodunu çağırır.
     * Folia uyumluluğu için Entity Scheduler kullanılır.
     *
     * @param core Core referansı (Scheduler erişimi için).
     * @param delay Görevin başlamadan önceki gecikmesi (tick cinsinden).
     * @param period Görevin tekrarlanma periyodu (tick cinsinden).
     */
    protected void startUpdateTask(BenthKeseCore core, long delay, long period) {
        if (isUpdating) return;
        isUpdating = true;

        Player player = playerMenuUtility.getOwner();

        core.getScheduler().runEntityTimer(player, () -> {
            if (!isUpdating) return;

            if (player == null || !player.isOnline()) {
                isUpdating = false;
                return;
            }

            InventoryView openInv = player.getOpenInventory();
            if (openInv.getTopInventory().getHolder() != this) {
                isUpdating = false;
                return;
            }

            onUpdate();

        }, delay, period);
    }

    /**
     * Varsa, bu menüye ait güncelleme görevini mantıksal olarak durdurur.
     */
    public void stopUpdateTask() {
        isUpdating = false;
    }

    /**
     * startUpdateTask ile başlatılan görev tarafından periyodik olarak çağrılır.
     * Dinamik olarak güncellenmesi gereken menüler bu metodu override etmelidir.
     */
    public void onUpdate() {
    }

    public ItemStack createItemFromConfig(MenuItemConfig config, Map<String, String> placeholders) {
        ItemStack item = new ItemStack(config.getMaterial(), 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String processedName = TextUtil.replacePlaceholders(config.getName(), placeholders);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', processedName));

            List<String> processedLore = TextUtil.replacePlaceholders(config.getLore(), placeholders);
            meta.setLore(processedLore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList()));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void fillEmptySlots(MenuItemConfig fillerConfig) {
        if (fillerConfig == null || fillerConfig.getMaterial() == Material.AIR) return;
        ItemStack fillerItem = createItemFromConfig(fillerConfig, Collections.emptyMap());
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem);
            }
        }
    }
}