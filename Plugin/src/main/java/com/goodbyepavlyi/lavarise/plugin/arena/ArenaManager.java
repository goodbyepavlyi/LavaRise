package com.goodbyepavlyi.lavarise.plugin.arena;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ArenaManager {
    private final LavaRise instance;
    private final Set<Arena> arenaSet;

    public ArenaManager(LavaRise instance) {
        this.instance = instance;
        this.arenaSet = new HashSet<>();

        this.load();
    }

    public enum Result {
        SUCCESS, ARENA_ISNT_SETUP, ARENA_FULL, ARENA_IN_GAME, PLAYER_IN_ARENA, PLAYER_NOT_IN_ARENA
    }

    public void load() {
        this.instance.getArenasConfig().getConfig().getKeys(false).forEach(arenaName -> {
            Arena arena = new Arena(this.instance, arenaName);
            this.arenaSet.add(arena);
            this.instance.debug(Level.INFO, String.format("Loaded arena %s", arenaName));
        });
    }

    public void save() {
        if (!this.arenaSet.isEmpty())
            this.arenaSet.forEach(Arena::saveConfig);

        this.instance.getArenasConfig().save();
        this.instance.debug(Level.INFO, "Saved arenas");
    }

    public boolean exists(String arenaName) {
        return this.arenaSet.stream().anyMatch(arena -> arena.getName().equals(arenaName));
    }

    public Arena getArena(String arenaName) {
        return this.arenaSet.stream().filter(arena -> arena.getName().equals(arenaName)).findFirst().orElse(null);
    }

    public Arena getArena(UUID uuid) {
        return arenaSet.stream()
                .filter(arena -> arena.getPlayers().stream().anyMatch(gamePlayer -> gamePlayer.getPlayerUUID().equals(uuid)))
                .findFirst()
                .orElse(null);
    }

    public Set<Arena> getArenaList() {
        return Collections.unmodifiableSet(this.arenaSet);
    }

    public boolean create(String arenaName) {
        if (this.exists(arenaName)) return false;

        Arena arena = new Arena(this.instance, arenaName);
        this.arenaSet.add(arena);
        this.instance.debug(Level.INFO, String.format("Created arena %s", arenaName));
        return true;
    }

    public boolean delete(String arenaName) {
        if (!this.exists(arenaName)) return false;

        Arena arena = this.getArena(arenaName);
        arena.deleteConfig();
        this.arenaSet.remove(arena);
        this.instance.debug(Level.INFO, String.format("Deleted arena %s", arenaName));
        return true;
    }

    public Result join(Arena arena, Player player) {
        if (!arena.readyToUse()) return Result.ARENA_ISNT_SETUP;
        if (this.getArena(player.getUniqueId()) != null) return Result.PLAYER_IN_ARENA;
        if (arena.getQueue().isFull()) return Result.ARENA_FULL;
        if (arena.getState().equals(Arena.State.IN_GAME)) return Result.ARENA_IN_GAME;

        arena.getQueue().addPlayer(player);
        this.instance.debug(Level.INFO, String.format("Player %s joined arena %s", player.getName(), arena.getName()));
        return Result.SUCCESS;
    }

    public Result leave(Player player) {
        Arena arena = this.getArena(player.getUniqueId());
        if (arena == null) return Result.PLAYER_NOT_IN_ARENA;

        if (arena.getState().equals(Arena.State.IN_GAME)) arena.getGame().removePlayer(player);
        else arena.getQueue().removePlayer(player);

        this.instance.debug(Level.INFO, String.format("Player %s left arena %s", player.getName(), arena.getName()));
        return Result.SUCCESS;
    }
}
