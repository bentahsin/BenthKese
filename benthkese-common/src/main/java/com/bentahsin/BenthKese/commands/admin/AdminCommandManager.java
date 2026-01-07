/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.admin;

import com.bentahsin.BenthKese.configuration.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminCommandManager implements CommandExecutor, TabCompleter {
    private final MessageManager messageManager;
    private final Map<String, IAdminSubCommand> subCommands = new HashMap<>();

    public AdminCommandManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public void registerCommand(IAdminSubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            messageManager.sendMessage(sender, "admin.invalid-usage");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        IAdminSubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            messageManager.sendMessage(sender, "admin.invalid-usage");
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
                    .map(IAdminSubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            IAdminSubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))) {
                String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subCommandArgs);
            }
        }

        return Collections.emptyList();
    }
}