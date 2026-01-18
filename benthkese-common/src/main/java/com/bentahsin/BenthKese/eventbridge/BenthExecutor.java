package com.bentahsin.BenthKese.eventbridge;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.invoke.MethodHandle;

/**
 * Bukkit'in reflection tabanlı executor'ı yerine geçen
 * MethodHandle tabanlı yüksek performanslı executor.
 */
public class BenthExecutor implements EventExecutor {

    private final MethodHandle handle;
    private final Class<? extends Event> eventClass;
    private final String requiredChannel;

    public BenthExecutor(MethodHandle handle, Class<? extends Event> eventClass, String requiredChannel) {
        this.handle = handle;
        this.eventClass = eventClass;
        this.requiredChannel = requiredChannel;
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (!eventClass.isAssignableFrom(event.getClass())) {
            return;
        }

        if (!requiredChannel.isEmpty()) {
            if (event instanceof BenthMessageEvent) {
                BenthMessageEvent messageEvent = (BenthMessageEvent) event;
                if (!messageEvent.getChannel().equals(requiredChannel)) {
                    return;
                }
            }
        }

        try {
            handle.invoke(event);
        } catch (Throwable t) {
            throw new EventException(t, "BenthEventBridge: Event işlenirken hata oluştu -> " + event.getEventName());
        }
    }
}