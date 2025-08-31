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

import java.util.Collections;
import java.util.List;

public class KeseFaizHelpCommand implements ISubCommand {

    private final MessageManager messageManager;

    public KeseFaizHelpCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.faiz.help";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        messageManager.sendMessageList(sender, "interest.help");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}