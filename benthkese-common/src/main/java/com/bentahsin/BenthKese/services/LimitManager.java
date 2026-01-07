/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services;

import com.bentahsin.BenthKese.data.LimitLevel;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;

public class LimitManager {

    private final JavaPlugin plugin;
    private final Map<Integer, LimitLevel> limitLevels = new TreeMap<>();

    public LimitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadLimits();
    }

    public void loadLimits() {
        YamlDocument limitsConfig;
        try {
            limitsConfig = YamlDocument.create(
                    new File(plugin.getDataFolder(), "limits.yml"),
                    Objects.requireNonNull(plugin.getResource("limits.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build(),
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build()
            );

            if (limitsConfig.reload()) {
                plugin.getLogger().info("limits.yml dosyası güncellendi.");
            }
        } catch (IOException | NullPointerException e) {
            plugin.getLogger().log(Level.SEVERE, "limits.yml dosyası oluşturulamadı veya yüklenemedi!", e);
            return;
        }

        limitLevels.clear();
        dev.dejvokep.boostedyaml.block.implementation.Section section = limitsConfig.getSection("limit-levels");

        if (section == null) {
            plugin.getLogger().severe("limits.yml dosyasında 'limit-levels' bölümü bulunamadı!");
            return;
        }

        for (Object key : section.getKeys()) {
            try {
                int level = Integer.parseInt(String.valueOf(key));
                Route route = Route.from("limit-levels", key);
                String name = ChatColor.translateAlternateColorCodes('&', section.getString(Route.from(route, "name"), "İsimsiz Seviye"));
                double cost = section.getDouble(Route.from(route, "cost"), 0.0);
                double sendLimit = section.getDouble(Route.from(route, "send-limit"), 1000.0);
                double receiveLimit = section.getDouble(Route.from(route, "receive-limit"), 1000.0);

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