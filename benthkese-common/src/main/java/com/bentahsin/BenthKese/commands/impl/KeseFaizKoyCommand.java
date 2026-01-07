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
import com.bentahsin.BenthKese.services.InterestService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class KeseFaizKoyCommand implements ISubCommand {

    private final MessageManager messageManager;
    private final InterestService interestService;

    public KeseFaizKoyCommand(MessageManager messageManager, InterestService interestService) {
        this.messageManager = messageManager;
        this.interestService = interestService;
    }

    @Override
    public String getName() {
        return "koy";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.faiz.koy";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return;
        }
        if (args.length != 2) {
            messageManager.sendMessage(sender, "interest.usage-koy");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            messageManager.sendMessage(sender, "invalid-amount");
            return;
        }

        String durationString = args[1];
        interestService.createAccount(player, amount, durationString);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}