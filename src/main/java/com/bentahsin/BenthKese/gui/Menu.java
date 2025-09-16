package com.bentahsin.BenthKese.gui;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.configuration.MenuItemConfig;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    protected PlayerMenuUtility playerMenuUtility;
    protected Map<Integer, Runnable> actions = new HashMap<>();

    private BukkitTask updateTask;

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
     * @param plugin Ana plugin referansı.
     * @param delay Görevin başlamadan önceki gecikmesi (tick cinsinden).
     * @param period Görevin tekrarlanma periyodu (tick cinsinden, 20 tick = 1 saniye).
     */
    protected void startUpdateTask(BenthKese plugin, long delay, long period) {
        stopUpdateTask();
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::onUpdate, delay, period);
    }

    /**
     * Varsa, bu menüye ait güncelleme görevini durdurur.
     */
    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
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