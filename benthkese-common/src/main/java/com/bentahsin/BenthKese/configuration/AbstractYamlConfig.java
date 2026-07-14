/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * resources/ altındaki bir .yml dosyasını yükleyip alanlara aktaran
 * konfigürasyon sınıfları için ortak taban. Yeni bir konfigürasyon eklemek
 * için bu sınıfı extend edip {@link #onLoad(YamlDocument)} metodunu doldurmak yeterlidir.
 */
public abstract class AbstractYamlConfig {

    protected final JavaPlugin plugin;
    private final String fileName;
    protected YamlDocument document;

    protected AbstractYamlConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public final void load() {
        try {
            document = YamlDocument.create(
                    new File(plugin.getDataFolder(), fileName),
                    Objects.requireNonNull(plugin.getResource(fileName)),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build(),
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build()
            );

            if (document.reload()) {
                plugin.getLogger().info(fileName + " dosyası güncellendi.");
            }

            onLoad(document);
        } catch (IOException | NullPointerException e) {
            plugin.getLogger().log(Level.SEVERE, fileName + " dosyası oluşturulamadı veya yüklenemedi!", e);
        }
    }

    public final void reload() {
        load();
    }

    /**
     * Dosya (yeniden) yüklendikten sonra çağrılır; alt sınıflar burada
     * kendi alanlarını {@code document} üzerinden doldurmalıdır.
     */
    protected abstract void onLoad(YamlDocument document);
}
