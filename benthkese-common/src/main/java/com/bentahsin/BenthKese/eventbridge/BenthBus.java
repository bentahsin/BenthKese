package com.bentahsin.BenthKese.eventbridge;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Mesaj yayınlamak için kullanılan basit API.
 */
public class BenthBus {

    /**
     * Belirtilen kanala bir veri paketi gönderir.
     *
     * @param sender  Gönderen eklenti (Genelde 'this')
     * @param channel Hedef kanal ismi (Örn: "faction-war-start")
     * @param payload Gönderilecek veri
     */
    public static void publish(Plugin sender, String channel, Object payload) {
        BenthMessageEvent event = new BenthMessageEvent(sender, channel, payload);
        Bukkit.getPluginManager().callEvent(event);
    }
}