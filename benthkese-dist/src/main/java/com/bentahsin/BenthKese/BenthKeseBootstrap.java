package com.bentahsin.BenthKese;

import com.bentahsin.BenthKese.api.IScheduler;
import com.bentahsin.BenthKese.bukkit.BukkitSchedulerImpl;
import com.bentahsin.BenthKese.folia.FoliaSchedulerImpl;
import org.bukkit.plugin.java.JavaPlugin;

public class BenthKeseBootstrap extends JavaPlugin {

    private BenthKeseCore core;

    @Override
    public void onEnable() {
        IScheduler scheduler;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            getLogger().info("Folia tespit edildi! FoliaScheduler yükleniyor...");
            scheduler = new FoliaSchedulerImpl();
        } catch (ClassNotFoundException e) {
            getLogger().info("Standart Bukkit ortamı tespit edildi. BukkitScheduler yükleniyor...");
            scheduler = new BukkitSchedulerImpl();
        }

        this.core = new BenthKeseCore(this, scheduler);
        this.core.enable();
    }

    @Override
    public void onDisable() {
        if (this.core != null) {
            this.core.disable();
        }
    }
}