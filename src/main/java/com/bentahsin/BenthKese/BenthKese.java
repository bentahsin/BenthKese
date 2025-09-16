/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese;

import com.bentahsin.BenthKese.commands.CommandManager;
import com.bentahsin.BenthKese.commands.ISubCommand;
import com.bentahsin.BenthKese.commands.impl.*;
import com.bentahsin.BenthKese.commands.admin.AdminCommandManager;
import com.bentahsin.BenthKese.commands.admin.impl.*;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.expansion.BenthKeseExpansion;
import com.bentahsin.BenthKese.gui.Menu;
import com.bentahsin.BenthKese.gui.listener.MenuListener;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.BenthKese.listeners.PlayerConnectionListener;
import com.bentahsin.BenthKese.services.BalanceSyncTask;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.services.storage.YamlStorageService;
import com.bentahsin.BenthKese.services.storage.database.DatabaseManager;
import com.bentahsin.BenthKese.services.storage.database.MySQLStorageService;
import com.bentahsin.BenthKese.services.storage.database.SQLiteStorageService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;

public final class BenthKese extends JavaPlugin {

    private static Economy econ = null;

    private MessageManager messageManager;
    private ConfigurationManager configurationManager;
    private EconomyService economyService;
    private LimitManager limitManager;
    private IStorageService storageService;
    private InterestService interestService;
    private DatabaseManager databaseManager;
    private MenuManager menuManager;
    private final Map<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault bulunamadı veya bir ekonomi eklentisine bağlanamadı! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        this.configurationManager = new ConfigurationManager(this);
        this.menuManager = new MenuManager(this);
        this.messageManager = new MessageManager(this);
        this.limitManager = new LimitManager(this);

        setupStorage();

        this.economyService = new EconomyService(configurationManager);
        this.interestService = new InterestService(storageService, configurationManager, messageManager);

        registerCommands();
        registerListeners();

        if (Bukkit.getPluginManager().getPlugin("PlaceHolderAPI") != null) {
            new BenthKeseExpansion(this, storageService, limitManager, messageManager, configurationManager).register();
            getLogger().info("PlaceHolderAPI bulundu ve placeholder'lar başarıyla kaydedildi.");
        } else {
            getLogger().info("PlaceHolderAPI bulunamadı, placeholder'lar devre dışı bırakıldı.");
        }

        if (!(storageService instanceof YamlStorageService)) {
            new BalanceSyncTask(storageService).runTaskTimer(this, 100L, 6000L);
        }

        getLogger().info("BenthKese Eklentisi başarıyla etkinleştirildi!");
    }

    @Override
    public void onDisable() {
        if (storageService != null) {
            getLogger().info("Sunucu kapanıyor, online oyuncuların verileri kaydediliyor...");
            getServer().getOnlinePlayers().forEach(player -> storageService.unloadPlayer(player.getUniqueId()));
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("BenthKese Eklentisi devre dışı bırakıldı.");
    }

    public void reloadPlugin() {
        this.configurationManager.loadConfig();
        this.messageManager.loadMessages();
        this.limitManager.loadLimits();
        this.menuManager.loadMenus();
        getLogger().info("Konfigürasyon dosyaları yeniden yüklendi.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    private void setupStorage() {
        String storageType = Objects.requireNonNull(getConfig().getString("storage.type", "SQLITE")).toUpperCase();

        if (storageType.equals("MYSQL")) {
            this.databaseManager = new DatabaseManager(this);
            try {
                databaseManager.connect();
                this.storageService = new MySQLStorageService(this, this.databaseManager, this.limitManager);
                getLogger().info("MySQL depolama sistemi başarıyla yüklendi.");
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "MySQL veritabanına bağlanılamadı! YAML depolamaya geçiliyor.", e);
                this.storageService = new YamlStorageService(this);
            }
        } else if (storageType.equals("SQLITE")) {
            this.databaseManager = new DatabaseManager(this);
            try {
                databaseManager.connect();
                this.storageService = new SQLiteStorageService(this, this.databaseManager, this.limitManager);
                getLogger().info("SQLite depolama sistemi başarıyla yüklendi.");
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "SQLite veritabanına bağlanılamadı! YAML depolamaya geçiliyor.", e);
                this.storageService = new YamlStorageService(this);
            }
        } else {
            getLogger().info("YAML depolama sistemi kullanılıyor.");
            this.storageService = new YamlStorageService(this);
        }
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager(messageManager, this, economyService, configurationManager, storageService, limitManager, interestService, menuManager);

        commandManager.registerCommand(new KeseHelpCommand(messageManager));
        commandManager.registerCommand(new KeseBakiyeCommand(messageManager));
        commandManager.registerCommand(new KeseKoyCommand(messageManager, economyService, configurationManager, storageService));
        commandManager.registerCommand(new KeseAlCommand(messageManager, economyService, configurationManager, storageService));
        commandManager.registerCommand(new KeseGonderCommand( messageManager, storageService, limitManager, configurationManager));


        KeseFaizCommand faizManagerCommand = new KeseFaizCommand(messageManager);
        faizManagerCommand.registerSubCommand(new KeseFaizHelpCommand(messageManager));
        faizManagerCommand.registerSubCommand(new KeseFaizListeCommand(this, messageManager, storageService, economyService, configurationManager, limitManager, interestService, menuManager));
        faizManagerCommand.registerSubCommand(new KeseFaizKoyCommand(messageManager, interestService));
        commandManager.registerCommand(faizManagerCommand);

        KeseLimitCommand limitManagerCommand = new KeseLimitCommand(messageManager, limitManager);
        ISubCommand gorCommand = new KeseLimitGorCommand(messageManager, storageService, limitManager);
        ISubCommand yukseltCommand = new KeseLimitYukseltCommand(messageManager, storageService, limitManager);
        limitManagerCommand.registerSubCommand(gorCommand);
        limitManagerCommand.registerSubCommand(yukseltCommand);
        Objects.requireNonNull(this.getCommand("kese")).setExecutor(commandManager);
        Objects.requireNonNull(this.getCommand("kese")).setTabCompleter(commandManager);
        commandManager.registerCommand(limitManagerCommand);

        registerAdminCommands();
    }

    private void registerAdminCommands() {
        AdminCommandManager adminCommandManager = new AdminCommandManager(messageManager);

        adminCommandManager.registerCommand(new AdminReloadCommand(this, messageManager));
        adminCommandManager.registerCommand(new AdminLimitCommand(storageService, messageManager, limitManager));
        adminCommandManager.registerCommand(new AdminBakiyeCommand(messageManager));

        Objects.requireNonNull(this.getCommand("keseadmin")).setExecutor(adminCommandManager);
        Objects.requireNonNull(this.getCommand("keseadmin")).setTabCompleter(adminCommandManager);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(storageService), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onMenuClose(InventoryCloseEvent event) {
                if (event.getInventory().getHolder() instanceof Menu) {
                    Menu menu = (Menu) event.getInventory().getHolder();
                    menu.stopUpdateTask();
                }
            }
        }, this);
    }

    public PlayerMenuUtility getPlayerMenuUtility(Player p) {
        PlayerMenuUtility playerMenuUtility;
        if (playerMenuUtilityMap.containsKey(p)) {
            return playerMenuUtilityMap.get(p);
        } else {
            playerMenuUtility = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p, playerMenuUtility);
            return playerMenuUtility;
        }
    }
    public static Economy getEconomy() {
        return econ;
    }
    public MenuManager getMenuManager() {
        return menuManager;
    }
}