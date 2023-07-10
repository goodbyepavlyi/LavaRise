package com.goodbyepavlyi.lavarise.plugin.handlers;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.utils.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

public class ArenaConfig extends ConfigUtil {
    public ArenaConfig(LavaRise instance, String path, boolean save) {
        super(instance, path, save);
    }

    public ConfigurationSection getArena(String arenaName) {
        return this.getConfig().getConfigurationSection(arenaName);
    }

    public void createArena(String arenaName) {
        this.getConfig().createSection(arenaName);
    }
}
