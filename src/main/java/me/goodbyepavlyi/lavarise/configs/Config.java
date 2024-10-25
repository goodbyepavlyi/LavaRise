package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import org.bukkit.Material;

import java.util.List;
import java.util.stream.Collectors;

public class Config extends YamlConfig {
    public Config(LavaRiseInstance instance) {
        super(instance, "config.yml", true, 2);
    }

    public int GameGracePhaseTime() {
        return this.getConfig().getInt("game.gracePhaseTime");
    }

    public int GameLavaRisingTime() {
        return this.getConfig().getInt("game.lavaRisingTime");
    }

    public int GameSpectatorSpawnYLavaOffset() {
        return this.getConfig().getInt("game.spectatorSpawnYLavaOffset");
    }

    public List<Material> GameItems() {
        List<String> materialNames = this.getConfig().getStringList("game.items");
        return materialNames.stream()
                .map(Material::getMaterial)
                .collect(Collectors.toList());
    }

    public List<String> GameCommandsWinner() {
        return this.getConfig().getStringList("game.commands.winner");
    }

    public List<String> GameCommandsLosers() {
        return this.getConfig().getStringList("game.commands.losers");
    }

    public List<String> GameCommandsPlayers() {
        return this.getConfig().getStringList("game.commands.players");
    }

    public int QueueCountdown() {
        return this.getConfig().getInt("queue.countdown");
    }

    public boolean QueueHalfFullQueueCountdownEnabled() {
        return this.getConfig().getBoolean("queue.halfFullQueueCountdown.enabled");
    }

    public int QueueHalfFullQueueCountdownValue() {
        return this.getConfig().getInt("queue.halfFullQueueCountdown.value");
    }

    public boolean Metrics() {
        return this.getConfig().getBoolean("metrics");
    }
}
