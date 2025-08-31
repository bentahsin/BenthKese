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
import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.services.LimitManager;
import org.bukkit.command.CommandSender;

import java.text.NumberFormat;
import java.util.*;

public class KeseLimitCommand implements ISubCommand {

    private final MessageManager messageManager;
    private final LimitManager limitManager;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    private final Map<String, ISubCommand> subCommands = new HashMap<>();

    public KeseLimitCommand(MessageManager messageManager, LimitManager limitManager) {
        this.messageManager = messageManager;
        this.limitManager = limitManager;
    }

    public void registerSubCommand(ISubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public String getName() {
        return "limit";
    }

    @Override
    public String getPermission() {
        return "benthkese.command.limit"; // Ana limit komutu için genel bir yetki
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Eğer argüman yoksa (/kese limit), varsayılan eylemi (listelemeyi) yap.
        if (args.length == 0) {
            handleListLimits(sender);
            return;
        }

        // Eğer argüman varsa, bunu bir alt komut olarak ele al.
        String subCommandName = args[0].toLowerCase();
        ISubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            messageManager.sendMessage(sender, "invalid-command-usage");
            return;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            messageManager.sendMessage(sender, "no-permission");
            return;
        }

        // Argümanları alt komuta doğru şekilde ilet.
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subArgs);
    }

    private void handleListLimits(CommandSender sender) {
        // Bu, bu sınıfın eski `execute` metodunun içeriğidir.
        messageManager.sendMessageList(sender, "limit-info.header");

        Map<Integer, LimitLevel> levels = limitManager.getAllLevels();
        String entryFormat = messageManager.getMessage("limit-info.entry-format");
        String infiniteText = messageManager.getMessage("limit-info.infinite-text");

        for (LimitLevel level : levels.values()) {
            String sendLimit = level.getSendLimit() == -1 ? infiniteText : numberFormat.format(level.getSendLimit());
            String receiveLimit = level.getReceiveLimit() == -1 ? infiniteText : numberFormat.format(level.getReceiveLimit());

            String message = entryFormat
                    .replace("{seviye_adi}", level.getName())
                    .replace("{maliyet}", numberFormat.format(level.getCost()))
                    .replace("{gonderme_limiti}", sendLimit)
                    .replace("{alma_limiti}", receiveLimit);
            sender.sendMessage(message);
        }

        messageManager.sendMessageList(sender, "limit-info.footer");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Kendi alt komutları için tab tamamlama yap.
            return subCommands.keySet().stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }

        // Tab tamamlamayı ilgili alt komuta devret.
        ISubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            return subCommand.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return Collections.emptyList();
    }
}