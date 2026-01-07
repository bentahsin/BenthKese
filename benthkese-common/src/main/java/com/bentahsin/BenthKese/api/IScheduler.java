package com.bentahsin.BenthKese.api;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public interface IScheduler {

    void runAsync(Plugin plugin, Runnable task);

    void runGlobal(Plugin plugin, Runnable task);
    void runGlobalTimer(Plugin plugin, Runnable task, long delay, long period);

    void runEntity(Entity entity, Runnable task);
    void runEntityLater(Entity entity, Runnable task, long delayTicks);
    void runEntityTimer(Entity entity, Runnable task, long delayTicks, long periodTicks);

    void cancelAll(Plugin plugin);
}