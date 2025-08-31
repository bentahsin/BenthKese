/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.configuration;

import com.bentahsin.BenthKese.BenthKese;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MessageManager {

    private final BenthKese plugin;
    private FileConfiguration messagesConfig;

    public MessageManager(BenthKese plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /**
     * Konfigürasyondan tek bir mesaj satırını alır ve renk kodlarını çevirir.
     * @param path Mesajın yolu (key).
     * @return İşlenmiş mesaj.
     */
    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString(path, "&cMesaj bulunamadı: " + path)));
    }

    /**
     * Konfigürasyondan bir mesaj listesini alır ve her satırın renk kodunu çevirir.
     * @param path Mesaj listesinin yolu (key).
     * @return İşlenmiş mesajların listesi.
     */
    public List<String> getMessageList(String path) {
        return messagesConfig.getStringList(path).stream()
                .map(msg -> ChatColor.translateAlternateColorCodes('&', msg))
                .collect(Collectors.toList());
    }

    /**
     * Bir oyuncuya veya konsola tek bir mesaj gönderir.
     * @param sender Mesajın gönderileceği kişi.
     * @param path Mesajın yolu (key).
     */
    public void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }

    /**
     * Bir oyuncuya veya konsola mesaj listesi gönderir.
     * @param sender Mesajın gönderileceği kişi.
     * @param path Mesaj listesinin yolu (key).
     */
    public void sendMessageList(CommandSender sender, String path) {
        List<String> messages = getMessageList(path);
        if (messages.isEmpty()) {
            String singleMessage = messagesConfig.getString(path);
            if(singleMessage != null && !singleMessage.isEmpty()){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', singleMessage));
            }
            return;
        }
        messages.forEach(sender::sendMessage);
    }
}