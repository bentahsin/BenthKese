/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.impl;

import com.bentahsin.BenthKese.commands.ISubCommand;
import com.bentahsin.BenthKese.configuration.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeseFaizCommand implements ISubCommand {

    private final MessageManager messageManager;
    private final Map<String, ISubCommand> subCommands = new HashMap<>();

    public KeseFaizCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public void registerSubCommand(ISubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public String getName() {
        return "faiz";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.faiz";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            messageManager.sendMessageList(sender, "interest.help");
            return;
        }

        String subCommandName = args[0].toLowerCase();
        ISubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            messageManager.sendMessageList(sender, "interest.help");
            return;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            messageManager.sendMessage(sender, "no-permission");
            return;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subArgs);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        ISubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            return subCommand.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return Collections.emptyList();
    }
}