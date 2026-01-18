package com.bentahsin.BenthKese.configuration;

import org.bukkit.Material;
import java.util.List;

/**
 * menus.yml dosyasındaki tek bir item'ın yapılandırmasını tutan veri sınıfı.
 */
public record MenuItemConfig(Material material, int slot, String name, List<String> lore) {
}