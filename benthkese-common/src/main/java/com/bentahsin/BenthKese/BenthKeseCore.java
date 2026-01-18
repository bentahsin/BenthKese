package com.bentahsin.BenthKese;

import com.bentahsin.BenthKese.api.IScheduler;
import com.bentahsin.BenthKese.commands.CommandManager;
import com.bentahsin.BenthKese.commands.admin.AdminCommandManager;
import com.bentahsin.BenthKese.commands.admin.impl.*;
import com.bentahsin.BenthKese.commands.impl.*;
import com.bentahsin.BenthKese.configuration.*;
import com.bentahsin.BenthKese.eventbridge.BenthRegistry;
import com.bentahsin.BenthKese.eventbridge.Subscribe;
import com.bentahsin.BenthKese.expansion.BenthKeseExpansion;
import com.bentahsin.BenthKese.gui.listener.MenuListener;
import com.bentahsin.BenthKese.listeners.PlayerConnectionListener;
import com.bentahsin.BenthKese.listeners.TransactionBridgeListener;
import com.bentahsin.BenthKese.services.BalanceSyncTask;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.services.storage.YamlStorageService;
import com.bentahsin.BenthKese.services.storage.database.DatabaseManager;
import com.bentahsin.BenthKese.services.storage.database.MySQLStorageService;
import com.bentahsin.BenthKese.services.storage.database.SQLiteStorageService;
import com.bentahsin.BenthKese.gui.utility.PlayerMenuUtility;
import com.bentahsin.configuration.Configuration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class BenthKeseCore {

    private final JavaPlugin plugin;
    private final IScheduler scheduler;

    private static Economy econ = null;
    private BenthRegistry eventRegistry;
    private MessageManager messageManager;
    private EconomyService economyService;
    private Configuration configLoader;
    private BenthConfig mainConfig;
    private LimitsConfig limitsConfig;
    private LimitManager limitManager;
    private IStorageService storageService;
    private InterestService interestService;
    private DatabaseManager databaseManager;
    private MenuManager menuManager;
    private final Map<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    public BenthKeseCore(JavaPlugin plugin, IScheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    public void enable() {
        if (!setupEconomy()) {
            plugin.getLogger().warning("Vault bulundu fakat aktif bir Ekonomi eklentisi (Essentials, CMI vb.) henüz hazır değil!");
            plugin.getLogger().warning("Eklenti çalışmaya devam edecek ve Ekonomi servisi geldiğinde otomatik bağlanacak.");
        } else {
            plugin.getLogger().info("Ekonomi sistemine başarıyla bağlanıldı.");
        }

        plugin.saveDefaultConfig();
        this.eventRegistry = new BenthRegistry(plugin);
        this.configLoader = new Configuration(plugin);
        this.mainConfig = new BenthConfig();
        this.configLoader.init(mainConfig, "config.yml");
        this.limitsConfig = new LimitsConfig();
        this.configLoader.init(limitsConfig, "limits.yml");
        this.limitManager = new LimitManager(this.limitsConfig);

        this.messageManager = new MessageManager(plugin);
        this.menuManager = new MenuManager(plugin);

        setupStorage();

        this.economyService = new EconomyService(mainConfig);
        this.interestService = new InterestService(storageService, mainConfig, messageManager);

        registerCommands();
        registerListeners();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BenthKeseExpansion(plugin, storageService, limitManager, messageManager, mainConfig).register();
            plugin.getLogger().info("PlaceHolderAPI bulundu ve placeholder'lar başarıyla kaydedildi.");
        }

        if (!(storageService instanceof YamlStorageService)) {
            long delay = 100L;
            long period = 6000L;

            scheduler.runGlobalTimer(plugin, new BalanceSyncTask(storageService), delay, period);
        }

        plugin.getLogger().info("BenthKese Core başarıyla etkinleştirildi!");
    }

    public void disable() {
        if (scheduler != null) {
            scheduler.cancelAll(plugin);
        }

        if (storageService != null) {
            plugin.getLogger().info("Sunucu kapanıyor, online oyuncuların verileri kaydediliyor...");
            for (Player player : Bukkit.getOnlinePlayers()) {
                storageService.unloadPlayer(player.getUniqueId());
            }
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        plugin.getLogger().info("BenthKese Core devre dışı bırakıldı.");
    }

    public void reloadPlugin() {
        this.configLoader.reload(mainConfig, "config.yml");
        this.configLoader.reload(limitsConfig, "limits.yml");
        this.messageManager.loadMessages();
        this.menuManager.loadMenus();
        plugin.getLogger().info("Konfigürasyonlar (Annotasyon sistemi ile) yeniden yüklendi.");
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void setupStorage() {
        String storageType = Objects.requireNonNull(plugin.getConfig().getString("storage.type", "SQLITE")).toUpperCase();

        if (storageType.equals("MYSQL")) {
            this.databaseManager = new DatabaseManager(plugin);
            try {
                databaseManager.connect();
                this.storageService = new MySQLStorageService(this, this.databaseManager, this.limitManager, scheduler);
                plugin.getLogger().info("MySQL depolama sistemi başarıyla yüklendi.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "MySQL hatası! YAML'a geçiliyor.", e);
                this.storageService = new YamlStorageService(plugin, scheduler);
            }
        } else if (storageType.equals("SQLITE")) {
            this.databaseManager = new DatabaseManager(plugin);
            try {
                databaseManager.connect();
                this.storageService = new SQLiteStorageService(this, this.databaseManager, this.limitManager, scheduler);
                plugin.getLogger().info("SQLite depolama sistemi başarıyla yüklendi.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite hatası! YAML'a geçiliyor.", e);
                this.storageService = new YamlStorageService(plugin, scheduler);
            }
        } else {
            plugin.getLogger().info("YAML depolama sistemi kullanılıyor.");
            this.storageService = new YamlStorageService(plugin, scheduler);
        }
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager(messageManager, this, economyService, mainConfig, storageService, limitManager, interestService, menuManager);

        commandManager.registerCommand(new KeseHelpCommand(messageManager));
        commandManager.registerCommand(new KeseBakiyeCommand(messageManager));
        commandManager.registerCommand(new KeseKoyCommand(this, messageManager, economyService, mainConfig, storageService));
        commandManager.registerCommand(new KeseAlCommand(this, messageManager, economyService, mainConfig, storageService));
        commandManager.registerCommand(new KeseGonderCommand(this, messageManager, storageService, limitManager, mainConfig));

        KeseFaizCommand faizCommand = new KeseFaizCommand(messageManager);
        faizCommand.registerSubCommand(new KeseFaizHelpCommand(messageManager));
        faizCommand.registerSubCommand(new KeseFaizListeCommand(this, messageManager, storageService, economyService, mainConfig, limitManager, interestService, menuManager));
        faizCommand.registerSubCommand(new KeseFaizKoyCommand(messageManager, interestService));
        commandManager.registerCommand(faizCommand);

        KeseLimitCommand limitCommand = new KeseLimitCommand(messageManager, limitManager);
        limitCommand.registerSubCommand(new KeseLimitGorCommand(messageManager, storageService, limitManager));
        limitCommand.registerSubCommand(new KeseLimitYukseltCommand(this, messageManager, storageService, limitManager));
        commandManager.registerCommand(limitCommand);

        Objects.requireNonNull(plugin.getCommand("kese")).setExecutor(commandManager);
        Objects.requireNonNull(plugin.getCommand("kese")).setTabCompleter(commandManager);

        registerAdminCommands();
    }

    private void registerAdminCommands() {
        AdminCommandManager adminManager = new AdminCommandManager(messageManager);
        adminManager.registerCommand(new AdminReloadCommand(this, messageManager));
        adminManager.registerCommand(new AdminLimitCommand(storageService, messageManager, limitManager));
        adminManager.registerCommand(new AdminBakiyeCommand(messageManager));

        Objects.requireNonNull(plugin.getCommand("keseadmin")).setExecutor(adminManager);
        Objects.requireNonNull(plugin.getCommand("keseadmin")).setTabCompleter(adminManager);
    }

    private void registerListeners() {
        this.eventRegistry = new BenthRegistry(plugin);
        eventRegistry.register(new TransactionBridgeListener(storageService));
        eventRegistry.register(new PlayerConnectionListener(storageService));
        eventRegistry.register(new MenuListener());
        eventRegistry.register(new Listener() {

            @Subscribe
            public void onServiceRegister(ServiceRegisterEvent event) {
                if (event.getProvider().getService() == Economy.class && econ == null) {
                    if (setupEconomy()) {
                        plugin.getLogger().info("Ekonomi eklentisi sonradan tespit edildi ve başarıyla bağlandı!");
                    }
                }
            }

            @Subscribe
            public void onMenuClose(InventoryCloseEvent event) {
                if (event.getInventory().getHolder() instanceof com.bentahsin.BenthKese.gui.Menu) {
                    ((com.bentahsin.BenthKese.gui.Menu) event.getInventory().getHolder()).stopUpdateTask();
                }
            }
        });
    }

    public PlayerMenuUtility getPlayerMenuUtility(Player p) {
        if (playerMenuUtilityMap.containsKey(p)) {
            return playerMenuUtilityMap.get(p);
        } else {
            PlayerMenuUtility util = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p, util);
            return util;
        }
    }

    public static Economy getEconomy() { return econ; }
    public JavaPlugin getPlugin() { return plugin; }
    public IScheduler getScheduler() { return scheduler; }
    public MenuManager getMenuManager() { return menuManager; }
    public BenthConfig getConfig() { return mainConfig; }
    public LimitsConfig getLimits() { return limitsConfig; }
}