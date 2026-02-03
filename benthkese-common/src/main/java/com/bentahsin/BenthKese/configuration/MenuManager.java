package com.bentahsin.BenthKese.configuration;

import com.bentahsin.BenthKese.utils.TextUtil;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class MenuManager {

    private final JavaPlugin plugin;
    private YamlDocument menuConfig;
    private final MessageManager messageManager;

    public MenuManager(JavaPlugin plugin) {
        this(plugin, null);
    }

    public MenuManager(JavaPlugin plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        loadMenus();
    }

    public void loadMenus() {
        try {
            menuConfig = YamlDocument.create(
                    new File(plugin.getDataFolder(), "menus.yml"),
                    Objects.requireNonNull(plugin.getResource("menus.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build(),
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build()
            );

            if (menuConfig.reload()) {
                plugin.getLogger().info("menus.yml dosyası güncellendi.");
            }
        } catch (IOException | NullPointerException e) {
            plugin.getLogger().log(Level.SEVERE, "menus.yml dosyası oluşturulamadı veya yüklenemedi!", e);
        }
    }

    public String getMenuTitle(String menuKey) {
        String title = menuConfig.getString(menuKey + ".title", "&cMenu Başlığı Bulunamadı");
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public String getMenuTitle(String menuKey, Map<String, String> placeholders) {
        String rawTitle = menuConfig.getString(menuKey + ".title", "&cMenu Başlığı Bulunamadı");
        String processedTitle = TextUtil.replacePlaceholders(rawTitle, placeholders);
        return ChatColor.translateAlternateColorCodes('&', processedTitle);
    }

    public int getMenuSize(String menuKey) {
        int size = menuConfig.getInt(menuKey + ".size", 27);
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
            material = Material.valueOf(menuConfig.getString(path + ".material", "STONE").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(String.format("menus.yml'deki '%s' materyali geçersiz!", path));
            material = Material.BARRIER;
        }

        int slot = menuConfig.getInt(path + ".slot", 0);
        String name = menuConfig.getString(path + ".name", "");
        List<String> lore = menuConfig.getStringList(path + ".lore");

        return new MenuItemConfig(material, slot, name, lore);
    }

    public Section getMenuSection(String menuKey) {
        return menuConfig.getSection(menuKey);
    }

    /**
     * GUI ile ilgili (messages.yml içinde gui.*) metinleri MessageManager üzerinden alır.
     * Örnek: getGuiMessage("anvil.default-amount-text") -> messages.yml'deki gui.anvil.default-amount-text
     */
    @SuppressWarnings("unused")
    public String getGuiMessage(String key) {
        if (messageManager == null) {
            plugin.getLogger().warning("MenuManager.getGuiMessage çağrıldı ancak MessageManager kayıtlı değil. Key: " + key);
            String alt = menuConfig.getString(key, "&cMesaj bulunamadı: " + key);
            return ChatColor.translateAlternateColorCodes('&', alt);
        }
        return messageManager.getMessage("gui." + key);
    }
}