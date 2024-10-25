package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.game.Game;
import me.goodbyepavlyi.lavarise.utils.ChatUtils;
import me.goodbyepavlyi.lavarise.utils.Logger;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Messages extends YamlConfig {
    private final int CONFIG_VERSION = 2;

    public Messages(LavaRiseInstance instance) {
        super(instance, "messages.yml", true);
        this.migrateConfigVersion();
    }

    public void migrateConfigVersion() {
        Logger.debug(String.format("Checking config file version: %s", this.getFile().getName()));
        if (this.getConfigVersion() == this.CONFIG_VERSION) return;

        Logger.warning(String.format("The config file %s is outdated. Migrating to version %d...", this.getFile().getName(), this.CONFIG_VERSION));

        FileConfiguration resourceConfig = this.getResourceConfig();
        if (resourceConfig == null) return;


        for (String key : resourceConfig.getKeys(true)) {
            if (this.getConfig().contains(key)) continue;
            Logger.debug(String.format("Migrating config key: %s", key));
            this.getConfig().set(key, resourceConfig.get(key));
        }

        this.setConfigVersion(this.CONFIG_VERSION);
        Logger.info(String.format("Config file %s migrated to version %d", this.getFile().getName(), this.CONFIG_VERSION));
        this.save();
    }

    public String getString(String path) {
        return ChatUtils.color(this.getConfig().getString(path));
    }

    public List<String> getList(String path) {
        return this.getConfig().getStringList(path)
                .stream()
                .map(ChatUtils::color)
                .collect(Collectors.toList());
    }

    public List<String> COMMAND_USAGE() {
        return this.getList("command.usage");
    }

    public String COMMAND_NOPERMISSIONS() {
        return this.getString("command.noPermissions");
    }

    public String COMMAND_ONLYPLAYER() {
        return this.getString("command.onlyPlayer");
    }

    public String COMMAND_ARENANOTFOUND() {
        return this.getString("command.arenaNotFound");
    }

    public String COMMAND_EXPECTEDNUMBER() {
        return this.getString("command.expectedNumber");
    }

    public String COMMAND_ARENA_LIST_EMPTY() {
        return this.getString("command.arena.list.empty");
    }

    public String COMMAND_ARENA_LIST_SUCCESS(String arenas) {
        return this.getString("command.arena.list.success")
                .replaceAll("%arenas%", arenas);
    }

    public String COMMAND_ARENA_CREATE_SUCCESS(String arenaName) {
        return this.getString("command.arena.create.success")
                .replaceAll("%arenaName%", arenaName);
    }

    public String COMMAND_ARENA_CREATE_FAILED(String arenaName) {
        return this.getString("command.arena.create.failed")
                .replaceAll("%arenaName%", arenaName);
    }

    public String COMMAND_ARENA_DELETE_SUCCESS(String arenaName) {
        return this.getString("command.arena.delete.success")
                .replaceAll("%arenaName%", arenaName);
    }

    public String COMMAND_ARENA_DELETE_FAILED(String arenaName) {
        return this.getString("command.arena.delete.failed")
                .replaceAll("%arenaName%", arenaName);
    }

    public String COMMAND_ARENA_SET_MINPLAYERS_SUCCESS(int minimumPlayers) {
        return this.getString("command.arena.set.minplayers.success")
                .replaceAll("%minimumPlayers%", String.valueOf(minimumPlayers));
    }

    public String COMMAND_ARENA_SET_MINPLAYERS_LOW_VALUE() {
        return this.getString("command.arena.set.minplayers.lowValue");
    }

    public String COMMAND_ARENA_SET_MAXPLAYERS_SUCCESS(int maximumPlayers) {
        return this.getString("command.arena.set.maxplayers.success")
                .replaceAll("%maximumPlayers%", String.valueOf(maximumPlayers));
    }

    public String COMMAND_ARENA_SET_MAXPLAYERS_LOW_VALUE() {
        return this.getString("command.arena.set.maxplayers.lowValue");
    }

    public String COMMAND_ARENA_SET_LOBBY_SUCCESS(Location location) {
        return this.getString("command.arena.set.lobby.success")
                .replaceAll("%worldName%", location.getWorld().getName())
                .replaceAll("%blockX%", String.valueOf(location.getBlockX()))
                .replaceAll("%blockY%", String.valueOf(location.getBlockY()))
                .replaceAll("%blockZ%", String.valueOf(location.getBlockZ()));
    }

    public String COMMAND_ARENA_SET_GAMEAREA_INVALIDVALUE() {
        return this.getString("command.arena.set.gamearea.invalidValue")
                .replaceAll("%types%", Arrays.stream(ArenaConfig.GameArea.values())
                        .map(ArenaConfig.GameArea::toString)
                        .collect(Collectors.joining(", ")));
    }

    public String COMMAND_ARENA_SET_GAMEAREA_SUCCESS(ArenaConfig.GameArea gameArea, Location location) {
        return this.getString("command.arena.set.gamearea.success")
                .replaceAll("%type%", gameArea.toString())
                .replaceAll("%worldName%", location.getWorld().getName())
                .replaceAll("%blockX%", String.valueOf(location.getBlockX()))
                .replaceAll("%blockY%", String.valueOf(location.getBlockY()))
                .replaceAll("%blockZ%", String.valueOf(location.getBlockZ()));
    }

    public String COMMAND_ARENA_SET_PVP_INVALIDVALUE() {
        return this.getString("command.arena.set.pvp.invalidValue")
                .replaceAll("%types%", "true, false");
    }

    public String COMMAND_ARENA_SET_PVP_SUCCESS(boolean pvp) {
        return this.getString("command.arena.set.pvp.success")
                .replaceAll("%type%", pvp ? "true" : "false");
    }

    public String COMMAND_ARENA_SET_LAVALEVEL_NOGAMEAREA() {
        return this.getString("command.arena.set.lavalevel.noGameArea");
    }

    public String COMMAND_ARENA_SET_LAVALEVEL_OUTOFRANGE(int minY, int maxY) {
        return this.getString("command.arena.set.lavalevel.outOfRange")
                .replaceAll("%minY%", String.valueOf(minY))
                .replaceAll("%maxY%", String.valueOf(maxY));
    }

    public String COMMAND_ARENA_SET_LAVALEVEL_SUCCESS(int lavaLevel) {
        return this.getString("command.arena.set.lavalevel.success")
                .replaceAll("%lavaLevel%", String.valueOf(lavaLevel));
    }

    public String COMMAND_JOIN_ALREADYINARENA() {
        return this.getString("command.join.alreadyInArena");
    }

    public String COMMAND_JOIN_ARENAISNTSETUP() {
        return this.getString("command.join.arenaIsntSetup");
    }

    public String COMMAND_JOIN_ARENAFULL() {
        return this.getString("command.join.arenaFull");
    }

    public String COMMAND_JOIN_ARENAINGAME() {
        return this.getString("command.join.arenaInGame");
    }

    public String COMMAND_LEAVE_NOTINARENA() {
        return this.getString("command.leave.notInArena");
    }

    public String QUEUE_PLAYERJOIN(String playerName, int playerCount, int maximumPlayers) {
        return this.getString("queue.playerJoin")
                .replaceAll("%playerName%", playerName)
                .replaceAll("%playerCount%", String.valueOf(playerCount))
                .replaceAll("%maximumPlayers%", String.valueOf(maximumPlayers));
    }

    public String QUEUE_PLAYERLEAVE(String playerName, int playerCount, int maximumPlayers) {
        return this.getString("queue.playerLeave")
                .replaceAll("%playerName%", playerName)
                .replaceAll("%playerCount%", String.valueOf(playerCount))
                .replaceAll("%maximumPlayers%", String.valueOf(maximumPlayers));
    }

    public String QUEUE_CANCELLED() {
        return this.getString("queue.cancelled");
    }

    public String QUEUE_GAMESTARTINGIN(int timeLeft) {
        return this.getString("queue.gameStartingIn")
                .replaceAll("%timeLeft%", String.valueOf(timeLeft));
    }

    public String QUEUE_ITEMS_LEAVE_NAME() {
        return this.getString("queue.items.leave.name");
    }

    public String QUEUE_SCOREBOARD_TITLE() {
        return this.getString("queue.scoreboard.title");
    }

    public String QUEUE_SCOREBOARD_DURATIONWAITING() {
        return this.getString("queue.scoreboard.durationWaiting");
    }

    public List<String> QUEUE_SCOREBOARD_LINES(String duration, int currentPlayers, int maximumPlayers) {
        return this.getList("queue.scoreboard.lines")
                .stream()
                .map(line -> line
                        .replaceAll("%duration%", duration)
                        .replaceAll("%currentPlayers%", String.valueOf(currentPlayers))
                        .replaceAll("%maximumPlayers%", String.valueOf(maximumPlayers))
                )
                .collect(Collectors.toList());
    }

    public String GAME_SCOREBOARD_TITLE() {
        return this.getString("game.scoreboard.title");
    }

    public List<String> GAME_SCOREBOARD_LINES(int playersLeft, int lavaY, long gameTime, Game.GamePhase gamePhase) {
        String gameTimeFormatted = new SimpleDateFormat("mm:ss").format(new Date(gameTime));

        return this.getList("game.scoreboard.lines")
                .stream()
                .map(line -> line
                        .replaceAll("%playersLeft%", String.valueOf(playersLeft))
                        .replaceAll("%lavaY%", String.valueOf(lavaY))
                        .replaceAll("%gameTime%", gameTimeFormatted)
                        .replaceAll("%event%", gamePhase.toString())
                )
                .collect(Collectors.toList());
    }

    public String GAME_EVENTS_GAMESTART() {
        return this.getString("game.events.gameStart");
    }

    public String GAME_EVENTS_GAMEENDED() {
        return this.getString("game.events.gameEnded");
    }

    public String GAME_EVENTS_PLAYERDEATH(String playerName) {
        return this.getString("game.events.playerDeath")
                .replaceAll("%playerName%", playerName);
    }

    public String GAME_EVENTS_PLAYERKILLED(String playerName, String killerName) {
        return this.getString("game.events.playerKilled")
                .replaceAll("%playerName%", playerName)
                .replaceAll("%killerName%", killerName);
    }

    public String GAME_EVENTS_LAVAPHASESTART() {
        return this.getString("game.events.lavaPhaseStart");
    }

    public String GAME_EVENTS_LAVAPHASEEND() {
        return this.getString("game.events.lavaPhaseEnd");
    }

    public String GAME_EVENTS_PVPENABLED() {
        return this.getString("game.events.pvpEnabled");
    }
}
