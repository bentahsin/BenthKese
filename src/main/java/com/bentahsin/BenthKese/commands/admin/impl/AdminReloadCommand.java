/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.admin.impl;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.commands.admin.IAdminSubCommand;
import com.bentahsin.BenthKese.configuration.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class AdminReloadCommand implements IAdminSubCommand {

    private final BenthKese plugin;
    private final MessageManager messageManager;

    public AdminReloadCommand(BenthKese plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "benthkese.admin.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reloadPlugin();
        messageManager.sendMessage(sender, "admin.reload.success");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}