package me.goodbyepavlyi.lavarise;

import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.ArenaManager;
import me.goodbyepavlyi.lavarise.commands.lavarise.LavaRiseCommand;
import me.goodbyepavlyi.lavarise.game.listeners.GameEventListener;
import me.goodbyepavlyi.lavarise.configs.Config;
import me.goodbyepavlyi.lavarise.configs.Messages;
import me.goodbyepavlyi.lavarise.game.listeners.GameGracePhaseEventListener;
import me.goodbyepavlyi.lavarise.papi.PlaceholderAPIExpansion;
import me.goodbyepavlyi.lavarise.queue.listeners.QueueEventListener;
import me.goodbyepavlyi.lavarise.updater.UpdateChecker;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.ManagementFactory;

public class LavaRiseInstance extends JavaPlugin {
    private final String SPIGOT_RESOURCE_ID = "111135";
    private final int BSTATS_METRICS_ID = 23679;
    private boolean DEBUG;

    private Config config;
    private Messages messages;
    private ArenaManager arenaManager;

    public LavaRiseInstance() {
        this.DEBUG = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
    }

    @Override
    public void onEnable() {
        this.DEBUG = this.DEBUG || this.getDescription().getVersion().contains("-dev");
        new Logger(this, this.DEBUG);

        this.config = new Config(this);
        this.messages = new Messages(this);
        this.arenaManager = new ArenaManager(this);

        this.getServer().getPluginManager().registerEvents(new QueueEventListener(this), this);
        this.getServer().getPluginManager().registerEvents(new GameEventListener(this), this);
        this.getServer().getPluginManager().registerEvents(new GameGracePhaseEventListener(this), this);
        this.getCommand("lavarise").setExecutor(new LavaRiseCommand(this));
        this.getCommand("lavarise").setTabCompleter(new LavaRiseCommand(this));

        if (this.config.Metrics() && !this.DEBUG) {
            Logger.debug("Enabling bStats metrics");
            new Metrics(this, BSTATS_METRICS_ID);
        } else Logger.debug("bStats metrics are disabled, skipping");

        new UpdateChecker(this, SPIGOT_RESOURCE_ID)
            .checkEveryXHours(24)
            .setNotifyByPermissionOnJoin("lavarise.updatechecker")
            .checkNow();

        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Logger.info("Hooking into PlaceholderAPI");
            new PlaceholderAPIExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        this.arenaManager.getArenaList().forEach(arena -> {
            if (!arena.getState().equals(Arena.State.IN_GAME)) return;
            arena.getGame().stop();
        });

        this.arenaManager.saveArenas();
    }

    public void reload(){
        this.config.load();
        this.messages.load();
    }

    public Config getConfiguration() {
        return this.config;
    }

    public Messages getMessages() {
        return this.messages;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }
}
