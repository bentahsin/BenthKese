package com.bentahsin.BenthKese.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Action bar mesajları göndermek için genel amaçlı ve sunucuyla uyumlu yardımcı metotlar içerir.
 * Bu versiyon, Spigot ve Paper üzerinde sorunsuz çalışır.
 */
public final class ActionBarUtil {

    private ActionBarUtil() {
    }

    /**
     * Belirtilen oyuncuya renk kodlarını destekleyen bir action bar mesajı gönderir.
     *
     * @param player  Mesajın gönderileceği oyuncu.
     * @param message Gönderilecek metin ('&' renk kodları ile).
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || !player.isOnline() || message == null || message.isEmpty()) {
            return;
        }

        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(coloredMessage));
    }
}