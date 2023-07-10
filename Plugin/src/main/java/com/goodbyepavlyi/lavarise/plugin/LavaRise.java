package com.goodbyepavlyi.lavarise.plugin;

import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import com.goodbyepavlyi.lavarise.plugin.arena.ArenaManager;
import com.goodbyepavlyi.lavarise.plugin.commands.LavaRiseCommand;
import com.goodbyepavlyi.lavarise.plugin.game.GameEvents;
import com.goodbyepavlyi.lavarise.plugin.handlers.ArenaConfig;
import com.goodbyepavlyi.lavarise.plugin.handlers.Config;
import com.goodbyepavlyi.lavarise.plugin.handlers.Messages;
import com.goodbyepavlyi.lavarise.plugin.queue.QueueEvents;
import com.goodbyepavlyi.lavarise.plugin.utils.ChatUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class LavaRise extends JavaPlugin {
    private Config config;
    private Messages messages;
    private ArenaConfig arenasConfig;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        this.config = new Config(this, "config.yml", true);
        this.messages = new Messages(this, "messages.yml", true);
        this.arenasConfig = new ArenaConfig(this, "arenas.yml", true);
        this.arenaManager = new ArenaManager(this);

        this.getServer().getPluginManager().registerEvents(new QueueEvents(this), this);
        this.getServer().getPluginManager().registerEvents(new GameEvents(this), this);
        this.getCommand("lavarise").setExecutor(new LavaRiseCommand(this));
        this.getCommand("lavarise").setTabCompleter(new LavaRiseCommand(this));

        this.log(Level.INFO, String.format("Enabling LavaRise v%s", this.getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
        this.arenaManager.getArenaList().forEach(arena -> {
            if (!arena.getState().equals(Arena.State.IN_GAME)) return;

            arena.getGame().stop();
        });

        this.arenaManager.save();
    }

    public void log(String message) {
        this.getServer().getConsoleSender().sendMessage(ChatUtils.color(message));
    }

    public void log(Level level, String message) {
        this.log(String.format("[%s] (%s) %s", this.getDescription().getName(), level, message));
    }

    public void debug(Level level, String message) {
        if (!this.config.DEBUG()) return;

        this.log(String.format("[%s] &4&lDEBUG &r(%s) %s", this.getDescription().getName(), level, message));
    }

    public Config getConfiguration() {
        return this.config;
    }

    public Messages getMessages() {
        return this.messages;
    }

    public ArenaConfig getArenasConfig() {
        return this.arenasConfig;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }
}
