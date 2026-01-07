package com.bentahsin.BenthKese.bukkit;

import com.bentahsin.BenthKese.api.IScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class BukkitSchedulerImpl implements IScheduler {

    @Override
    public void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runGlobal(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runGlobalTimer(Plugin plugin, Runnable task, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    @Override
    public void runEntity(Entity entity, Runnable task) {
        runGlobal(Bukkit.getPluginManager().getPlugin("BenthKese"), task);
    }

    @Override
    public void runEntityLater(Entity entity, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BenthKese")), task, delayTicks);
    }

    @Override
    public void runEntityTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BenthKese")), task, delayTicks, periodTicks);
    }

    @Override
    public void cancelAll(Plugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}