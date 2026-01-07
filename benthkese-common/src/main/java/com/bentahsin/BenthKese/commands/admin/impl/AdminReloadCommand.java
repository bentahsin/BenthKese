/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.admin.impl;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.commands.admin.IAdminSubCommand;
import com.bentahsin.BenthKese.configuration.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class AdminReloadCommand implements IAdminSubCommand {

    private final BenthKeseCore core;
    private final MessageManager messageManager;

    public AdminReloadCommand(BenthKeseCore core, MessageManager messageManager) {
        this.core = core;
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
        core.reloadPlugin();
        messageManager.sendMessage(sender, "admin.reload.success");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}