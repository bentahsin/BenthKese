/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.commands.impl;

import com.bentahsin.BenthKese.BenthKeseCore;
import com.bentahsin.BenthKese.commands.ISubCommand;
import com.bentahsin.BenthKese.configuration.BenthConfig;
import com.bentahsin.BenthKese.configuration.MenuManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.gui.menus.InterestAccountListGUI;
import com.bentahsin.BenthKese.services.EconomyService;
import com.bentahsin.BenthKese.services.InterestService;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class KeseFaizListeCommand implements ISubCommand {

    private final BenthKeseCore core;
    private final MessageManager messageManager;
    private final IStorageService storageService;
    private final EconomyService economyService;
    private final BenthConfig config;
    private final LimitManager limitManager;
    private final InterestService interestService;
    private final MenuManager menuManager;

    public KeseFaizListeCommand(BenthKeseCore core, MessageManager messageManager, IStorageService storageService, EconomyService economyService, BenthConfig config, LimitManager limitManager, InterestService interestService, MenuManager menuManager) {
        this.core = core;
        this.messageManager = messageManager;
        this.storageService = storageService;
        this.economyService = economyService;
        this.config = config;
        this.limitManager = limitManager;
        this.interestService = interestService;
        this.menuManager = menuManager;
    }

    @Override
    public String getName() {
        return "liste";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.faiz.liste";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return;
        }

        new InterestAccountListGUI(
                core.getPlayerMenuUtility(player),
                core,
                menuManager,
                messageManager,
                storageService,
                economyService,
                config,
                limitManager,
                interestService
        ).open();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}