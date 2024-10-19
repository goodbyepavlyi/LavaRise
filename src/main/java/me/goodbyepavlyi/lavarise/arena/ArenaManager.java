package me.goodbyepavlyi.lavarise.arena;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.configs.ArenaStorageConfig;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArenaManager {
    private final LavaRiseInstance instance;
    private final Set<Arena> arenas;
    private final ArenaStorageConfig storageConfig;

    public enum ArenaStateResult {
        SUCCESS, ARENA_IS_NOT_SETUP, ARENA_FULL, ARENA_IN_GAME, PLAYER_IN_ARENA, PLAYER_NOT_IN_ARENA
    }

    public ArenaManager(LavaRiseInstance instance) {
        this.instance = instance;
        this.storageConfig = new ArenaStorageConfig(instance);
        this.arenas = new HashSet<>();

        this.loadArenas();
    }

    public LavaRiseInstance getInstance() {
        return this.instance;
    }

    public ArenaStorageConfig getStorageConfig() {
        return this.storageConfig;
    }

    public void loadArenas() {
        this.storageConfig.getArenas().forEach(arenaName -> {
            Arena arena = new Arena(this, arenaName);
            this.arenas.add(arena);
            Logger.debug(String.format("Loaded arena %s", arenaName));
        });
    }

    public void saveArenas() {
        if (this.arenas.isEmpty()) return;

        this.arenas.forEach(arena -> arena.getConfig().saveConfig());
        this.storageConfig.save();
        Logger.debug("Saved arenas");
    }

    public Set<Arena> getArenaList() {
        return Collections.unmodifiableSet(this.arenas);
    }

    public Arena getArena(String arenaName) {
        return this.arenas.stream()
            .filter(arena -> arena.getName().equals(arenaName))
            .findFirst()
            .orElse(null);
    }

    public Arena getArenaByPlayer(UUID uuid) {
        return arenas.stream()
                .filter(arena -> arena.getPlayers().stream().anyMatch(gamePlayer -> gamePlayer.getPlayerUUID() == uuid))
                .findFirst()
                .orElse(null);
    }

    public boolean createArena(String arenaName) {
        if (this.getArena(arenaName) != null) return false;

        Arena arena = new Arena(this, arenaName);
        this.arenas.add(arena);
        Logger.debug(String.format("Created arena %s", arenaName));
        return true;
    }

    public boolean removeArena(String arenaName) {
        Arena arena = this.getArena(arenaName);
        if (arena == null) return false;

        arena.getConfig().removeConfig();
        this.arenas.remove(arena);
        Logger.debug(String.format("Deleted arena %s", arenaName));
        return true;
    }

    public ArenaStateResult joinArena(Arena arena, Player player) {
        if (!arena.checkSetup().isEmpty()) return ArenaStateResult.ARENA_IS_NOT_SETUP;
        if (this.getArenaByPlayer(player.getUniqueId()) != null) return ArenaStateResult.PLAYER_IN_ARENA;
        if (arena.getQueue().isFull()) return ArenaStateResult.ARENA_FULL;
        if (arena.getState().equals(Arena.State.IN_GAME)) return ArenaStateResult.ARENA_IN_GAME;

        arena.getQueue().addPlayer(player);
        return ArenaStateResult.SUCCESS;
    }

    public ArenaStateResult leaveArena(Player player) {
        Arena arena = this.getArenaByPlayer(player.getUniqueId());
        if (arena == null) return ArenaStateResult.PLAYER_NOT_IN_ARENA;

        arena.removePlayer(player);
        return ArenaStateResult.SUCCESS;
    }
}
