package com.bentahsin.BenthKese.folia;

import com.bentahsin.BenthKese.api.IScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class FoliaSchedulerImpl implements IScheduler {

    @Override
    public void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, (scheduledTask) -> task.run());
    }

    @Override
    public void runGlobal(Plugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, (scheduledTask) -> task.run());
    }

    @Override
    public void runGlobalTimer(Plugin plugin, Runnable task, long delay, long period) {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (scheduledTask) -> task.run(), delay, period);
    }

    @Override
    public void runEntity(Entity entity, Runnable task) {
        entity.getScheduler().run(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BenthKese")), (scheduledTask) -> task.run(), null);
    }

    @Override
    public void runEntityLater(Entity entity, Runnable task, long delayTicks) {
        entity.getScheduler().runDelayed(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BenthKese")), (scheduledTask) -> task.run(), null, delayTicks);
    }

    @Override
    public void runEntityTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        entity.getScheduler().runAtFixedRate(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BenthKese")), (scheduledTask) -> task.run(), null, delayTicks, periodTicks);
    }

    @Override
    public void cancelAll(Plugin plugin) {
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
    }
}