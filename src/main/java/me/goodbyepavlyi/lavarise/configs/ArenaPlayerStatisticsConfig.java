package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.models.ArenaPlayerStatistics;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class ArenaPlayerStatisticsConfig extends YamlConfig {
    private final int CONFIG_VERSION = 1;

    public ArenaPlayerStatisticsConfig(LavaRiseInstance instance) {
        super(instance, "playerStatistics.yml", false);

        if (!this.getFile().exists()) {
            this.getConfig().set("version", this.CONFIG_VERSION);
            this.save();
        }
    }

    public ConfigurationSection getSection(UUID playerUUID) {
        return this.getConfig().getConfigurationSection(playerUUID.toString());
    }
    
    public void saveStatistics(UUID playerUUID, ArenaPlayerStatistics statistics) {
        ConfigurationSection section = this.getSection(playerUUID);
        if (section == null) section = this.getConfig().createSection(playerUUID.toString());
        
        section.set("wins", statistics.getWins());
        section.set("losses", statistics.getLosses());
        section.set("kills", statistics.getKills());
        section.set("deaths", statistics.getDeaths());
        
        this.save();
    }
}
