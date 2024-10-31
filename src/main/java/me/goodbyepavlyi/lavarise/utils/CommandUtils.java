package me.goodbyepavlyi.lavarise.utils;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandUtils {
    public static void sendMessage(CommandSender commandSender, String message) {
        commandSender.sendMessage(ChatUtils.color(message));
    }

    public static void sendMessage(CommandSender commandSender, List<String> messages) {
        messages.forEach(message -> commandSender.sendMessage(ChatUtils.color(message)));
    }

    public static void sendMessage(Player player, List<TextComponent> textComponent) {
        for (TextComponent component : textComponent) {
            player.spigot().sendMessage(component);
        }
    }
}
