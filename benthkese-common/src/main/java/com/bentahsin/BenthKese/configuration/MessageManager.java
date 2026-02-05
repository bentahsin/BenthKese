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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MessageManager {

    private final JavaPlugin plugin;
    private YamlDocument messagesConfig;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        try {
            UpdaterSettings updaterSettings = UpdaterSettings.builder()
                    .setVersioning(new BasicVersioning("config-version"))
                    .build();

            messagesConfig = YamlDocument.create(
                    new File(plugin.getDataFolder(), "messages.yml"),
                    Objects.requireNonNull(plugin.getResource("messages.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build(),
                    updaterSettings
            );

            if (messagesConfig.reload()) {
                plugin.getLogger().info("messages.yml dosyası güncellendi.");
            }

        } catch (IOException | NullPointerException e) {
            plugin.getLogger().log(Level.SEVERE, "messages.yml dosyası oluşturulamadı veya yüklenemedi!", e);
        }
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString(path, "&cMesaj bulunamadı: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public List<String> getMessageList(String path) {
        if (!messagesConfig.contains(path)) {
            return Collections.emptyList();
        }
        return messagesConfig.getStringList(path).stream()
                .map(msg -> ChatColor.translateAlternateColorCodes('&', msg))
                .collect(Collectors.toList());
    }

    public void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }

    public void sendMessageList(CommandSender sender, String path) {
        List<String> messages = getMessageList(path);
        if (messages.isEmpty()) {
            String singleMessage = messagesConfig.getString(path);
            if (singleMessage != null && !singleMessage.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', singleMessage));
            }
            return;
        }
        messages.forEach(sender::sendMessage);
    }
}