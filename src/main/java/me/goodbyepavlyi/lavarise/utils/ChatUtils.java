package me.goodbyepavlyi.lavarise.utils;

import org.bukkit.ChatColor;

import java.util.List;

public class ChatUtils {
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&' , message);
    }

    public static List<String> color(List<String> messages) {
        return messages.stream().map(ChatUtils::color).toList();
    }
}
