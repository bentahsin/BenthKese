/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.data.LimitLevel;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class LimitManager {

    private final BenthKese plugin;
    private final Map<Integer, LimitLevel> limitLevels = new TreeMap<>();

    public LimitManager(BenthKese plugin) {
        this.plugin = plugin;
        loadLimits();
    }

    public void loadLimits() {
        limitLevels.clear();
        File limitsFile = new File(plugin.getDataFolder(), "limits.yml");
        if (!limitsFile.exists()) {
            plugin.saveResource("limits.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(limitsFile);
        ConfigurationSection section = config.getConfigurationSection("limit-levels");

        if (section == null) {
            plugin.getLogger().severe("limits.yml dosyasında 'limit-levels' bölümü bulunamadı!");
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                String name = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(section.getString(key + ".name", "İsimsiz Seviye")));
                double cost = section.getDouble(key + ".cost", 0.0);
                double sendLimit = section.getDouble(key + ".send-limit", 1000.0);
                double receiveLimit = section.getDouble(key + ".receive-limit", 1000.0);

                limitLevels.put(level, new LimitLevel(level, name, cost, sendLimit, receiveLimit));
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("limits.yml'de geçersiz seviye ID'si: " + key);
            }
        }
    }

    public LimitLevel getLimitLevel(int level) {
        return limitLevels.get(level);
    }

    public LimitLevel getNextLevel(int currentLevel) {
        return limitLevels.get(currentLevel + 1);
    }

    public Map<Integer, LimitLevel> getAllLevels() {
        return Collections.unmodifiableMap(limitLevels);
    }
}