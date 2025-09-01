package com.bentahsin.BenthKese.configuration;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.utils.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * menus.yml dosyasını yönetir ve GUI sınıfları için yapılandırma verileri sağlar.
 */
public class MenuManager {

    private final BenthKese plugin;
    private FileConfiguration menuConfig;

    public MenuManager(BenthKese plugin) {
        this.plugin = plugin;
        loadMenus();
    }

    public void loadMenus() {
        File menuFile = new File(plugin.getDataFolder(), "menus.yml");
        if (!menuFile.exists()) {
            plugin.saveResource("menus.yml", false);
        }
        menuConfig = YamlConfiguration.loadConfiguration(menuFile);
    }

    public String getMenuTitle(String menuKey) {
        String title = menuConfig.getString(menuKey + ".title", "&cMenu Başlığı Bulunamadı");
        assert title != null;
        return ChatColor.translateAlternateColorCodes('&', title);
    }
    /**
     * Verilen menü anahtarı için ham başlığı alır, placeholder'ları değiştirir ve renklendirir.
     * @param menuKey Menünün anahtarı (örn: "history-menu").
     * @param placeholders Değiştirilecek placeholder'lar.
     * @return İşlenmiş ve renklendirilmiş menü başlığı.
     */
    public String getMenuTitle(String menuKey, Map<String, String> placeholders) {
        String rawTitle = menuConfig.getString(menuKey + ".title", "&cMenu Başlığı Bulunamadı");
        String processedTitle = TextUtil.replacePlaceholders(rawTitle, placeholders);
        assert processedTitle != null;
        return ChatColor.translateAlternateColorCodes('&', processedTitle);
    }


    public int getMenuSize(String menuKey) {
        int size = menuConfig.getInt(menuKey + ".size", 27);
        // Boyutun 9'un katı olduğundan emin ol
        return (size > 0 && size % 9 == 0) ? size : 27;
    }

    public MenuItemConfig getMenuItem(String menuKey, String itemKey) {
        String path = menuKey + ".items." + itemKey;
        if (!menuConfig.contains(path)) {
            plugin.getLogger().warning(String.format("menus.yml'de '%s' yolu bulunamadı!", path));
            return new MenuItemConfig(Material.BARRIER, 0, "&cItem Hatası", Collections.singletonList("&7Lütfen menus.yml dosyanızı kontrol edin."));
        }

        Material material;
        try {
            material = Material.valueOf(Objects.requireNonNull(menuConfig.getString(path + ".material", "STONE")).toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(String.format("menus.yml'deki '%s' materyali geçersiz!", path));
            material = Material.BARRIER;
        }

        int slot = menuConfig.getInt(path + ".slot", 0);
        String name = menuConfig.getString(path + ".name", "");
        List<String> lore = menuConfig.getStringList(path + ".lore");

        return new MenuItemConfig(material, slot, name, lore);
    }

    public ConfigurationSection getMenuSection(String menuKey) {
        return menuConfig.getConfigurationSection(menuKey);
    }
}