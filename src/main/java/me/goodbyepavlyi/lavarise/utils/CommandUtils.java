package me.goodbyepavlyi.lavarise.utils;

import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandUtils {
    public static void sendMessage(CommandSender commandSender, String message) {
        commandSender.sendMessage(ChatUtils.color(message));
    }

    public static void sendMessage(CommandSender commandSender, List<String> messages) {
        messages.forEach(message -> commandSender.sendMessage(ChatUtils.color(message)));
    }
}
