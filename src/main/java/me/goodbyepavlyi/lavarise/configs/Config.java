package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.utils.Logger;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class Config extends YamlConfig {
    private final int CONFIG_VERSION = 2;

    public Config(LavaRiseInstance instance) {
        super(instance, "config.yml", true);
        this.migrateConfigVersion();
    }

    public void migrateConfigVersion() {
        Logger.debug(String.format("Checking config file version: %s", this.getFile().getName()));
        if (this.getConfigVersion() == this.CONFIG_VERSION) return;

        Logger.warning(String.format("The config file %s is outdated. Migrating to version %d...", this.getFile().getName(), this.CONFIG_VERSION));

        FileConfiguration resourceConfig = this.getResourceConfig();
        if (resourceConfig == null) return;

        if (this.getConfigVersion() == 1) {
            // Migrate deprecated %winner% placeholder to %player%
            this.getConfig().set("game.commands.winner", this.GameCommandsWinner()
                .stream()
                .map(command -> command.replace("%winner%", "%player%"))
                .collect(Collectors.toList()));
        }

        for (String key : resourceConfig.getKeys(true)) {
            if (this.getConfig().contains(key)) continue;
            Logger.debug(String.format("Migrating config key: %s", key));
            this.getConfig().set(key, resourceConfig.get(key));
        }

        this.setConfigVersion(this.CONFIG_VERSION);
        Logger.info(String.format("Config file %s migrated to version %d", this.getFile().getName(), this.CONFIG_VERSION));
        this.save();
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

    public Material QueueLeaveItemMaterial() {
        return Material.getMaterial(this.getConfig().getString("queue.leaveItem.material"));
    }

    public int QueueLeaveItemSlot() {
        return this.getConfig().getInt("queue.leaveItem.slot");
    }

    public boolean Metrics() {
        return this.getConfig().getBoolean("metrics");
    }
}
