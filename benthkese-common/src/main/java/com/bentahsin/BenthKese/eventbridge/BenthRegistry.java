package com.bentahsin.BenthKese.eventbridge;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BenthRegistry {

    private final Plugin plugin;
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public BenthRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register(Listener listener) {
        Class<?> clazz = listener.getClass();

        for (Method method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(Subscribe.class)) {
                continue;
            }

            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalStateException("BenthEventBridge HATASI: @Subscribe metodu STATIC olamaz! -> " + method.getName());
            }

            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("BenthEventBridge HATASI: Metot tam olarak 1 parametre almalı! -> " + method.getName());
            }

            Class<?> paramType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(paramType)) {
                throw new IllegalArgumentException("BenthEventBridge HATASI: Parametre Event türevi olmalı! -> " + method.getName());
            }

            Subscribe annotation = method.getAnnotation(Subscribe.class);
            String channelFilter = annotation.channel();

            if (!channelFilter.isEmpty() && !com.bentahsin.BenthKese.eventbridge.BenthMessageEvent.class.isAssignableFrom(paramType)) {
                throw new IllegalArgumentException("BenthEventBridge HATASI: 'channel' filtresi sadece BenthMessageEvent ile kullanılabilir! -> "
                        + clazz.getName() + "#" + method.getName());
            }

            Class<? extends Event> eventType = paramType.asSubclass(Event.class);

            try {
                method.setAccessible(true);
                MethodHandle handle = lookup.unreflect(method);
                MethodHandle boundHandle = handle.bindTo(listener);

                com.bentahsin.BenthKese.eventbridge.BenthExecutor executor = new com.bentahsin.BenthKese.eventbridge.BenthExecutor(boundHandle, eventType, channelFilter);

                Bukkit.getPluginManager().registerEvent(
                        eventType,
                        listener,
                        annotation.priority(),
                        executor,
                        plugin,
                        annotation.ignoreCancelled()
                );

            } catch (IllegalAccessException e) {
                plugin.getLogger().severe("Metoda erişim sağlanamadı: " + method.getName());
                plugin.getLogger().severe(e.getMessage());
            }
        }
    }
}