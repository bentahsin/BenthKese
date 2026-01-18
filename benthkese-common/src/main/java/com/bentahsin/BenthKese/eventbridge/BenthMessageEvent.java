package com.bentahsin.BenthKese.eventbridge;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

/**
 * Eklentiler arası haberleşmeyi sağlayan genel taşıyıcı event.
 */
public class BenthMessageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Plugin sender;
    private final String channel;
    private final Object payload;

    /**
     * @param sender  Mesajı gönderen eklenti
     * @param channel Kanal adı (Örn: "eco-update", "player-rank-change")
     * @param payload Taşınan veri (String, Custom Object, Map vs.)
     */
    public BenthMessageEvent(Plugin sender, String channel, Object payload) {
        this.sender = sender;
        this.channel = channel;
        this.payload = payload;
    }

    public Plugin getSender() {
        return sender;
    }

    public String getChannel() {
        return channel;
    }

    /**
     * Gelen veriyi güvenli bir şekilde istenen tipe dönüştürür.
     */
    public <T> T getPayload(Class<T> type) {
        if (type.isInstance(payload)) {
            return type.cast(payload);
        }
        throw new ClassCastException("Payload tipi uyuşmuyor! Beklenen: " + type.getName() + ", Gelen: " + payload.getClass().getName());
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}