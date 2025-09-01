/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.menus.KeseMainMenuGUI;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final BenthKese plugin;
    private final MessageManager messageManager;
    private final EconomyService economyService;
    private final ConfigurationManager configurationManager;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final InterestService interestService;
    private final MenuManager menuManager;
    private final Map<String, ISubCommand> subCommands = new HashMap<>();

    public CommandManager(MessageManager messageManager, BenthKese plugin, EconomyService economyService, ConfigurationManager configManager, IStorageService storageService, LimitManager limitManager, InterestService interestService, MenuManager menuManager) {
        this.menuManager = menuManager;
        this.messageManager = messageManager;
        this.economyService = economyService;
        this.configurationManager = configManager;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.interestService = interestService;
        this.plugin = plugin;
    }

    public void registerCommand(ISubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                new KeseMainMenuGUI(plugin.getPlayerMenuUtility(p), plugin, menuManager, messageManager, economyService, configurationManager, storageService, limitManager, interestService).open();
            } else {
                subCommands.get("help").execute(sender, new String[0]);
            }
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        ISubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            messageManager.sendMessage(sender, "invalid-command-usage");
            return true;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            messageManager.sendMessage(sender, "no-permission");
            return true;
        }

        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subCommandArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            String currentArg = args[0].toLowerCase();
            return subCommands.values().stream()
                    .filter(sub -> sub.getPermission() == null || sender.hasPermission(sub.getPermission()))
                    .map(ISubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            ISubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))) {
                String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subCommandArgs);
            }
        }

        return Collections.emptyList();
    }
}