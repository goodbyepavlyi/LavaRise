package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.models.ArenaPlayerStatistics;
import me.goodbyepavlyi.lavarise.utils.Logger;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.UUID;

public class ArenaPlayerStatisticsConfig extends YamlConfig {
    private final int CONFIG_VERSION = 1;
    
    public enum StatisticsType {
        WINS,
        LOSSES,
        KILLS,
        DEATHS
    }

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

    public Map.Entry<UUID, Integer> getTopPlayerByPosition(StatisticsType type, int position) {
        Logger.debug("Getting player at position " + position + " by " + type.toString());
        List<Map.Entry<UUID, Integer>> sortedPlayers = new ArrayList<>();

        for (String key : this.getConfig().getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(key);
                ConfigurationSection section = this.getSection(playerUUID);
                if (section == null) continue;

                int value = section.getInt(type.toString().toLowerCase());
                sortedPlayers.add(new AbstractMap.SimpleEntry<>(playerUUID, value));
            } catch (IllegalArgumentException ex) {
                // Invalid UUID
            }
        }

        sortedPlayers.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        if (position < 1 || position > sortedPlayers.size()) return null;
        return sortedPlayers.get(position - 1);
    }

}