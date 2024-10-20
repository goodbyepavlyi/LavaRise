package me.goodbyepavlyi.lavarise.arena.models;

import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ArenaPlayer {
    private final Player player;
    private final UUID playerUUID;
    private ArenaPlayerData arenaPlayerData;
    private boolean spectator;

    public ArenaPlayer(Player player) {
        this.player = player;
        this.playerUUID = player.getUniqueId();
        this.arenaPlayerData = new ArenaPlayerData(this);
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerData(ArenaPlayerData arenaPlayerData) {
        this.arenaPlayerData = arenaPlayerData;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public void saveData() {
        Logger.debug(String.format("Saving data for player %s", player.getName()));
        setPlayerData(new ArenaPlayerData(this, player));
    }

    public void restoreData() {
        Logger.debug(String.format("Restoring data for player %s", player.getName()));
        arenaPlayerData.restoreAll();
    }
}
