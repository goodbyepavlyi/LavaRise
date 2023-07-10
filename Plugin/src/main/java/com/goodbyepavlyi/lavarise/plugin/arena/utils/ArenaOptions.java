package com.goodbyepavlyi.lavarise.plugin.arena.utils;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class ArenaOptions {
    private final LavaRise instance;
    private final ConfigurationSection config;

    public ArenaOptions(LavaRise instance, Arena arena) {
        this.instance = instance;

        if (this.instance.getArenasConfig().getArena(arena.getName()) == null)
            this.instance.getArenasConfig().createArena(arena.getName());

        this.config = this.instance.getArenasConfig().getArena(arena.getName());

        this.instance.debug(Level.INFO, String.format("Initialized options for arena %s", arena.getName()));
    }

    public enum GameArea {
        TOP("Top"),
        BOTTOM("Bottom");

        private final String name;

        GameArea(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public void setMinimumPlayers(int minimumPlayers) {
        this.config.set("minimumPlayers", minimumPlayers);
    }

    public int getMinimumPlayers() {
        return this.config.getInt("minimumPlayers");
    }

    public void setMaximumPlayers(int maximumPlayers) {
        this.config.set("maximumPlayers", maximumPlayers);
    }

    public int getMaximumPlayers() {
        return this.config.getInt("maximumPlayers");
    }

    public void setLobby(Location lobby) {
        this.config.set("lobby.world", lobby.getWorld().getName());
        this.config.set("lobby.blockX", lobby.getBlockX());
        this.config.set("lobby.blockY", lobby.getBlockY());
        this.config.set("lobby.blockZ", lobby.getBlockZ());
        this.config.set("lobby.yaw", lobby.getYaw());
        this.config.set("lobby.pitch", lobby.getPitch());
    }

    public Location getLobby() {
        String worldName = this.config.getString("lobby.world");
        if (worldName == null) return null;

        World world = this.instance.getServer().getWorld(worldName);
        int blockX = this.config.getInt("lobby.blockX");
        int blockY = this.config.getInt("lobby.blockY");
        int blockZ = this.config.getInt("lobby.blockZ");
        float yaw = this.config.getInt("lobby.yaw");
        float pitch = this.config.getInt("lobby.pitch");

        return new Location(world, blockX, blockY, blockZ, yaw, pitch);
    }

    public void setGameAreaLocation(GameArea gameArea, Location gameAreaLocation) {
        this.config.set("gameArea.world", gameAreaLocation.getWorld().getName());
        this.config.set("gameArea." + gameArea + ".blockX", gameAreaLocation.getBlockX());
        this.config.set("gameArea." + gameArea + ".blockY", gameAreaLocation.getBlockY());
        this.config.set("gameArea." + gameArea + ".blockZ", gameAreaLocation.getBlockZ());
    }

    public World getGameWorld() {
        String worldName = this.config.getString("gameArea.world");
        if (worldName == null) return null;

        return this.instance.getServer().getWorld(worldName);
    }

    public Location getGameArea(GameArea gameArea) {
        String worldName = this.config.getString("gameArea.world");
        if (worldName == null) return null;

        World world = this.instance.getServer().getWorld(worldName);
        int blockX = this.config.getInt("gameArea." + gameArea + ".blockX");
        int blockY = this.config.getInt("gameArea." + gameArea + ".blockY");
        int blockZ = this.config.getInt("gameArea." + gameArea + ".blockZ");

        return new Location(world, blockX, blockY, blockZ);
    }
}
