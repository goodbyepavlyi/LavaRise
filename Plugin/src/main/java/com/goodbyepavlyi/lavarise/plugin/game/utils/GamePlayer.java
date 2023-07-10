package com.goodbyepavlyi.lavarise.plugin.game.utils;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.utils.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GamePlayer {
    private final LavaRise instance;
    private final Player player;
    private final UUID playerUUID;
    private boolean isSpectator;
    private PlayerData playerData;

    public GamePlayer(LavaRise instance, Player player) {
        this.instance = instance;
        this.player = player;
        this.playerUUID = player.getUniqueId();
        this.playerData = new PlayerData(this);
    }

    public void setPlayerData(PlayerData playerData) {
        this.playerData = playerData;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public boolean isSpectator() {
        return isSpectator;
    }

    public void spectate() {
        this.isSpectator = true;

        this.player.setGameMode(GameMode.ADVENTURE);
        this.player.setAllowFlight(true);
    }

    public void saveData() {
        // Save player data
        setPlayerData(new PlayerData(
                this,
                player.getInventory().getContents(),
                player.getInventory().getArmorContents(),
                player.getLocation(),
                player.getHealth(),
                player.getFoodLevel(),
                player.getGameMode()
        ));
    }

    public void restoreData() {
        this.playerData.restoreAll();
        this.player.setFireTicks(0);
    }
}
