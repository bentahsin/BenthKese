package com.bentahsin.BenthKese.configuration;

import org.bukkit.Material;
import java.util.List;

/**
 * menus.yml dosyasındaki tek bir item'ın yapılandırmasını tutan veri sınıfı.
 */
public class MenuItemConfig {
    private final Material material;
    private final int slot;
    private final String name;
    private final List<String> lore;

    public MenuItemConfig(Material material, int slot, String name, List<String> lore) {
        this.material = material;
        this.slot = slot;
        this.name = name;
        this.lore = lore;
    }

    // Getter'lar
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public String getName() { return name; }
    public List<String> getLore() { return lore; }
}