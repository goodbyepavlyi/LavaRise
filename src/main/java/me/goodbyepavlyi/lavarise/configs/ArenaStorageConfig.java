package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public class ArenaStorageConfig extends YamlConfig {
    private final int CONFIG_VERSION = 1;

    public ArenaStorageConfig(LavaRiseInstance instance) {
        super(instance, "arenas.yml", false);

        if (!this.getFile().exists()) {
            this.getConfig().set("version", this.CONFIG_VERSION);
            this.save();
        }
    }

    public Set<String> getArenas() {
        return this.getConfig().getKeys(false);
    }

    public ConfigurationSection getSection(String arenaName) {
        return this.getConfig().getConfigurationSection(arenaName);
    }

    public void createSection(String arenaName) {
        this.getConfig().createSection(arenaName);
    }
}
