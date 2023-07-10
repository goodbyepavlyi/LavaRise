package com.goodbyepavlyi.lavarise.plugin.utils;

import org.bukkit.command.CommandSender;

import java.util.List;

public class Command {
    public void sendMessage(CommandSender commandSender, String message) {
        commandSender.sendMessage(ChatUtils.color(message));
    }

    public void sendMessage(CommandSender commandSender, List<String> messages) {
        messages.forEach(message -> commandSender.sendMessage(ChatUtils.color(message)));
    }
}
