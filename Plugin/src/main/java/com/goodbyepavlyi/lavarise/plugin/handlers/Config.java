package com.goodbyepavlyi.lavarise.plugin.handlers;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.utils.ConfigUtil;
import org.bukkit.Material;

import java.util.List;
import java.util.stream.Collectors;

public class Config extends ConfigUtil {
    public Config(LavaRise instance, String path, boolean save) {
        super(instance, path, save);
    }

    public boolean DEBUG() {
        return this.getConfig().getBoolean("debug");
    }

    public int GAME_GRACEPHASETIME() {
        return this.getConfig().getInt("game.gracePhaseTime");
    }

    public int GAME_LAVARISINGTIME() {
        return this.getConfig().getInt("game.lavaRisingTime");
    }

    public List<Material> GAME_ITEMS() {
        List<String> materialNames = this.getConfig().getStringList("game.items");
        return materialNames.stream()
                .map(Material::getMaterial)
                .collect(Collectors.toList());
    }

    public List<String> GAME_COMMANDS_WINNER() {
        return this.getConfig().getStringList("game.commands.winner");
    }

    public int QUEUE_COUNTDOWN() {
        return this.getConfig().getInt("queue.countdown");
    }
}
